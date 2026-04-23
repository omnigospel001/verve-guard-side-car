CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    first_name VARCHAR(30) NOT NULL,
    last_name VARCHAR(30) NOT NULL,

    account_number BIGINT NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(30) NOT NULL UNIQUE,
    merchant_id VARCHAR(30) NOT NULL UNIQUE,

    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    last_modified_by BIGINT NOT NULL
    ) ENGINE=InnoDB;


--
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    name VARCHAR(50) NOT NULL,

    user_id BIGINT,

    CONSTRAINT fk_roles_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE
    ) ENGINE=InnoDB;


--
CREATE TABLE IF NOT EXISTS account_management (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    currency VARCHAR(10),
    card_number BIGINT,
    merchant_id VARCHAR(30),

    transaction_type VARCHAR(50),

    balance DECIMAL(20,2) NOT NULL DEFAULT 0,

    user_id BIGINT,

    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    last_modified_by BIGINT NOT NULL,

    CONSTRAINT fk_account_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE
    ) ENGINE=InnoDB;