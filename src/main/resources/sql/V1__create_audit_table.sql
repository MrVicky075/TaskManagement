-- Audit / Activity Log table (SQL Server)
-- Hibernate ddl-auto=update will also create this; run manually if needed.

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'tbl_audit')
BEGIN
    CREATE TABLE tbl_audit (
        audit_id     BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id      BIGINT NULL,
        user_name    NVARCHAR(100) NULL,
        action       NVARCHAR(30) NOT NULL,
        entity_type  NVARCHAR(20) NOT NULL,
        entity_id    BIGINT NULL,
        description  NVARCHAR(1000) NULL,
        old_value    NVARCHAR(MAX) NULL,
        new_value    NVARCHAR(MAX) NULL,
        ip_address   NVARCHAR(45) NULL,
        created_at   DATETIME2 NOT NULL DEFAULT SYSDATETIME()
    );

    CREATE INDEX idx_audit_user_id ON tbl_audit (user_id);
    CREATE INDEX idx_audit_entity_type ON tbl_audit (entity_type);
    CREATE INDEX idx_audit_entity_id ON tbl_audit (entity_id);
    CREATE INDEX idx_audit_action ON tbl_audit (action);
    CREATE INDEX idx_audit_created_at ON tbl_audit (created_at DESC);
END;
