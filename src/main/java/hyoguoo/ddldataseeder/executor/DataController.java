package hyoguoo.ddldataseeder.executor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/data")
public class DataController {

    private final DataSource ds;
    private final GeneratorRegistry registry;

    @PostMapping("/insert")
    public Map<String, Object> insert(@RequestBody InsertRequest req) throws Exception {
        int batchSize = Optional.ofNullable(req.batchSize()).orElse(5000);
        int concurrency = Optional.ofNullable(req.concurrency()).orElse(1);

        // Build column list and generators
        List<String> allColumns = new ArrayList<>(req.spec().keySet());
        List<String> insertColumns = allColumns.stream()
                .filter(c -> !"auto".equalsIgnoreCase(String.valueOf(req.spec().get(c).get("gen"))))
                .toList();

        List<GeneratorRegistry.ColumnGenerator> gens = insertColumns.stream()
                .map(c -> {
                    Map<String, Object> args = req.spec().get(c);
                    return registry.build(String.valueOf(args.get("gen")), args);
                }).toList();

        long start = System.currentTimeMillis();

        doJdbcBatch(req.table(), insertColumns, gens, req.rowCount(), batchSize, concurrency);

        long end = System.currentTimeMillis();
        long ms = end - start;
        double rps = (req.rowCount() * 1000.0) / Math.max(ms, 1);
        return Map.of("inserted", req.rowCount(), "elapsedMs", ms, "rowsPerSec", rps);
    }

    // Pure JDBC batch insert
    void doJdbcBatch(String table, List<String> cols, List<GeneratorRegistry.ColumnGenerator> gens,
            long total, int batchSize, int concurrency) throws Exception {
        String placeholders = cols.stream().map(c -> "?").collect(java.util.stream.Collectors.joining(","));
        String sql = "INSERT INTO " + table + " (" + String.join(",", cols) + ") VALUES (" + placeholders + ")";

        // Single thread is fast enough, but partitioning is possible if desired
        long perThread = total / concurrency;
        long remainder = total % concurrency;

        ExecutorService es = Executors.newFixedThreadPool(concurrency);
        List<Future<?>> futures = new ArrayList<>();
        for (int t = 0; t < concurrency; t++) {
            long count = perThread + (t == 0 ? remainder : 0);
            if (count == 0) {
                continue;
            }

            futures.add(es.submit(() -> {
                try (Connection c = ds.getConnection()) {
                    c.setAutoCommit(false);
                    try (PreparedStatement ps = c.prepareStatement(sql)) {
                        int inBatch = 0;
                        for (long i = 0; i < count; i++) {
                            for (int k = 0; k < gens.size(); k++) {
                                Object v = gens.get(k).next();
                                ps.setObject(k + 1, v);
                            }
                            ps.addBatch();
                            if (++inBatch >= batchSize) {
                                ps.executeBatch();
                                c.commit();
                                inBatch = 0;
                            }
                        }
                        if (inBatch > 0) {
                            ps.executeBatch();
                            c.commit();
                        }
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        for (Future<?> f : futures) {
            f.get();
        }
        es.shutdown();
    }
}
