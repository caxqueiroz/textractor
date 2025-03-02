-- Schema for H2 in-memory database for tests

-- Create processed_files table
CREATE TABLE IF NOT EXISTS processed_files (
    file_id UUID PRIMARY KEY,
    file_hash VARCHAR(255) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(255),
    file_size BIGINT NOT NULL,
    app_id UUID NOT NULL,
    ocr_content CLOB,
    llm_content CLOB,
    processing_status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create app_profiles table
CREATE TABLE IF NOT EXISTS app_profiles (
    id UUID PRIMARY KEY,
    profile_name VARCHAR(255) NOT NULL UNIQUE,
    profile_description CLOB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for faster lookups
CREATE INDEX IF NOT EXISTS idx_processed_files_file_hash ON processed_files(file_hash);
CREATE INDEX IF NOT EXISTS idx_processed_files_app_id ON processed_files(app_id);
