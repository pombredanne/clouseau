cat create_table_changes.sql | sqlite3 changes.db
mv changes.db ../

