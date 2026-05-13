-- USERS
INSERT INTO users (
    first_name,
    last_name,
    account_number,
    email,
    password,
    phone,
    merchant_id,
    created_date,
    last_modified_date,
    created_by,
    last_modified_by
)
VALUES
    ('Gospel','Udechukwu',154653675541,'gospel.udechukwu@interswitchgroup.com','$2a$10$h13tMS5NBb8M0Nd/0hLsSeYwsxC1UcsAfmPp3DXqgWvyAB41k.3sG','08010000001','M001',NOW(),NOW(),1,1),
    ('Faith','Chigozirim',1000000001,'gosmajesty@yahoo.com','$2a$10$h13tMS5NBb8M0Nd/0hLsSeYwsxC1UcsAfmPp3DXqgWvyAB41k.3sG','08010000001756','M00451',NOW(),NOW(),1,1),
    ('Peter','Omu',1000000002,'peter.omu@interswitchgroup.com','$2a$10$h13tMS5NBb8M0Nd/0hLsSeYwsxC1UcsAfmPp3DXqgWvyAB41k.3sG','08010000021','M007',NOW(),NOW(),1,1),
    ('Oluwademilade','Okeowo',1000000003,'oluwademilade.okeowo@interswitchgroup.com','$2a$10$h13tMS5NBb8M0Nd/0hLsSeYwsxC1UcsAfmPp3DXqgWvyAB41k.3sG','08010000002','M002',NOW(),NOW(),1,1),
    ('Uko','Uwatt',1000000004,'uko.uwatt@interswitchgroup.com','$2a$10$h13tMS5NBb8M0Nd/0hLsSeYwsxC1UcsAfmPp3DXqgWvyAB41k.3sG','08010000003','M003',NOW(),NOW(),1,1),
    ('Suleiman','Suleiman',1000000005,'suleiman.suleiman@interswitchgroup.com','$2a$10$h13tMS5NBb8M0Nd/0hLsSeYwsxC1UcsAfmPp3DXqgWvyAB41k.3sG','08010000004','M004',NOW(),NOW(),1,1),
    ('Chika','Solomon',1000000006,'chika.solomon@interswitchgroup.com','$2a$10$h13tMS5NBb8M0Nd/0hLsSeYwsxC1UcsAfmPp3DXqgWvyAB41k.3sG','08010000005','M005',NOW(),NOW(),1,1);

-- ROLES
INSERT INTO roles (name, user_id)
VALUES
    ('ROLE_ADMIN', (SELECT id FROM users WHERE email = 'gospel.udechukwu@interswitchgroup.com')),
    ('ROLE_ADMIN', (SELECT id FROM users WHERE email = 'peter.omu@interswitchgroup.com')),
    ('ROLE_USER', (SELECT id FROM users WHERE email = 'oluwademilade.okeowo@interswitchgroup.com')),
    ('ROLE_MERCHANT', (SELECT id FROM users WHERE email = 'uko.uwatt@interswitchgroup.com')),
    ('ROLE_MERCHANT', (SELECT id FROM users WHERE email = 'suleiman.suleiman@interswitchgroup.com')),
    ('ROLE_USER', (SELECT id FROM users WHERE email = 'chika.solomon@interswitchgroup.com'));

-- ACCOUNT MANAGEMENT
INSERT INTO account_management (
    currency,
    card_number,
    merchant_id,
    transaction_type,
    balance,
    user_id,
    created_date,
    last_modified_date,
    created_by,
    last_modified_by
)
VALUES
    ('NGN', 5555444433331111, '109500MG', 'DEPOSIT', 50000.00, 1, NOW(), NOW(), 1, 1),
    ('NGN', 5555444433332222, '398002GH', 'TRANSFER', 120000.00, 2, NOW(), NOW(), 1, 1);