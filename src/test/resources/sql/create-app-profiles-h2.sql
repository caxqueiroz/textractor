-- Clean up existing test data
DELETE FROM app_profiles WHERE id IN (
    'abcdef00-0000-0000-0000-000000000001',
    'abcdef00-0000-0000-0000-000000000002'
);

-- Insert test data for H2
INSERT INTO app_profiles (
    id,
    profile_name,
    profile_description,
    created_at
) VALUES (
    'abcdef00-0000-0000-0000-000000000001',
    'Test Profile',
    'Test Description',
    NOW()
);

INSERT INTO app_profiles (
    id,
    profile_name,
    profile_description,
    created_at
) VALUES (
    'abcdef00-0000-0000-0000-000000000002',
    'Another Test Profile',
    'Another Test Description',
    NOW()
);
