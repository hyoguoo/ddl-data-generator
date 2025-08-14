package hyoguoo.ddl_data_generator.executor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ddl")
public class DdlController {

    private final JdbcTemplate jdbc;

    @PostMapping("/execute")
    public Map<String, Object> execute(@RequestBody Map<String, String> body) {
        String sql = body.getOrDefault("sql", "");
        if (sql.isBlank()) {
            throw new IllegalArgumentException("sql is blank");
        }

        int executed = 0;
        try (Connection c = Objects.requireNonNull(jdbc.getDataSource()).getConnection()) {
            c.setAutoCommit(false);
            for (String stmt : sql.split(";")) {
                String s = stmt.strip();
                if (!s.isEmpty()) {
                    try (Statement st = c.createStatement()) {
                        st.execute(s);
                        executed++;
                    }
                }
            }
            c.commit();
        } catch (SQLException e) {
            System.out.println("SQL Execution Error: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return Map.of("ok", true, "executedStatements", executed);
    }
}
