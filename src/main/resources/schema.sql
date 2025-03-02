CREATE TABLE IF NOT EXISTS processed_files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_id UUID NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    file_hash VARCHAR(255),
    file_path VARCHAR(255),
    llm_content CLOB,
    app_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_file_hash ON processed_files (file_hash);