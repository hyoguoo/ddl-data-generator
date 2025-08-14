package hyoguoo.ddl_data_generator.executor;


import java.util.Map;

record InsertRequest(
        String table,
        long rowCount,
        Integer batchSize,
        Integer concurrency,
        Map<String, Map<String, Object>> spec
) {

}
