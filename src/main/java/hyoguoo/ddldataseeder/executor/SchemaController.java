package hyoguoo.ddldataseeder.executor;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/schema")
public class SchemaController {

    private final DataSource ds;

    @GetMapping("/tables")
    public List<String> tables() throws SQLException {
        try (Connection c = ds.getConnection()) {
            DatabaseMetaData md = c.getMetaData();
            try (ResultSet rs = md.getTables(c.getCatalog(), c.getSchema(), "%", new String[]{"TABLE"})) {
                List<String> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(rs.getString("TABLE_NAME"));
                }
                return list;
            }
        }
    }

    @GetMapping("/{table}")
    public List<Map<String, Object>> columns(@PathVariable String table) throws SQLException {
        try (Connection c = ds.getConnection()) {
            DatabaseMetaData md = c.getMetaData();

            Set<String> pk = new HashSet<>();
            try (ResultSet rs = md.getPrimaryKeys(c.getCatalog(), c.getSchema(), table)) {
                while (rs.next()) {
                    pk.add(rs.getString("COLUMN_NAME"));
                }
            }

            List<Map<String, Object>> cols = new ArrayList<>();
            try (ResultSet rs = md.getColumns(c.getCatalog(), c.getSchema(), table, "%")) {
                while (rs.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("name", rs.getString("COLUMN_NAME"));
                    m.put("typeName", rs.getString("TYPE_NAME"));
                    m.put("size", rs.getInt("COLUMN_SIZE"));
                    m.put("nullable", rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                    m.put("defaultValue", rs.getString("COLUMN_DEF"));
                    m.put("primaryKey", pk.contains(rs.getString("COLUMN_NAME")));
                    m.put("autoIncrement", "YES".equalsIgnoreCase(rs.getString("IS_AUTOINCREMENT")));
                    cols.add(m);
                }
            }
            return cols;
        }
    }
}
