-- Clean up existing test data
DELETE FROM processed_files WHERE file_id IN (
    '00000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000002',
    '00000000-0000-0000-0000-000000000003'
);

-- Insert test data
INSERT INTO processed_files (
    file_id,
    file_hash,
    file_name,
    file_path,
    file_size,
    app_id,
    ocr_content,
    llm_content,
    created_at
) VALUES (
    '00000000-0000-0000-0000-000000000001',
    'test-hash-123',
    'test-file.pdf',
    '/path/to/test-file.pdf',
    2048,
    '11111111-1111-1111-1111-111111111111',
    '{"text":"This is a test document"}',
    '{"text":"This is a test document"}',
    NOW()
);

INSERT INTO processed_files (
    file_id,
    file_hash,
    file_name,
    file_path,
    file_size,
    app_id,
    ocr_content,
    llm_content,
    created_at
) VALUES (
    '00000000-0000-0000-0000-000000000002',
    'test-hash-456',
    'another-test-file.pdf',
    '/path/to/another-test-file.pdf',
    4096,
    '11111111-1111-1111-1111-111111111111',
    '{"text":"This is another test document"}',
    '{"text":"This is another test document"}',
    NOW()
);

INSERT INTO processed_files (
    file_id,
    file_hash,
    file_name,
    file_path,
    file_size,
    app_id,
    ocr_content,
    llm_content,
    created_at
) VALUES (
    '00000000-0000-0000-0000-000000000003',
    'test-hash-789',
    'third-test-file.pdf',
    '/path/to/third-test-file.pdf',
    6144,
    '22222222-2222-2222-2222-222222222222',
    '{"text":"This is a third test document"}',
    '{"text":"This is a third test document"}',
    NOW()
);
