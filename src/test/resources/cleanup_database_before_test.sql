CREATE OR REPLACE FUNCTION truncate_all_tables_except(exclusions text[])
    RETURNS VOID
AS
'
    DECLARE
        table_record RECORD;
    BEGIN
        FOR table_record IN SELECT table_name
                            FROM information_schema.tables
                            WHERE table_schema = ''public''
                              AND table_type = ''BASE TABLE''
                              AND table_name != ALL (exclusions)
            LOOP
                EXECUTE format(''TRUNCATE TABLE %I RESTART IDENTITY CASCADE'', table_record.table_name);
            END LOOP;
    END;
'
    LANGUAGE plpgsql;

SELECT truncate_all_tables_except(ARRAY ['flyway_schema_history']);
