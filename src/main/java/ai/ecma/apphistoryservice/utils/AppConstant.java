package ai.ecma.apphistoryservice.utils;

public interface AppConstant {
    String ID = "id";
    String INDEXES = "DROP INDEX if exists idx_entity_name_and_row_id;\n" +
            "                    CREATE INDEX idx_entity_name_and_row_id\n" +
            "                    ON  action_history (entity_name,row_id);\n";
    String PREFIX_ID = "Id";
}
