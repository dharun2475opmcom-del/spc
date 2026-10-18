-- Run this script in MySQL Workbench against the smart_parking database.
CREATE DATABASE IF NOT EXISTS smart_parking;
USE smart_parking;

-- For a new database, this creates the users table.
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT NOT NULL AUTO_INCREMENT,
    user_name VARCHAR(100) NOT NULL,
    email VARCHAR(190) NOT NULL,
    contact_info VARCHAR(30) NOT NULL,
    vehicle_number VARCHAR(20) NOT NULL,
    password_hash VARCHAR(60) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    UNIQUE KEY uq_users_email (email)
);

-- If your existing users table was created by the earlier project and does not
-- have password_hash yet, run this one-time statement separately:
-- ALTER TABLE users ADD COLUMN password_hash VARCHAR(60) NULL;
-- After existing accounts have been migrated, you can make it NOT NULL.
