-- Run this ONCE in MySQL Workbench if smart_parking.users already exists
-- and does not yet contain the password_hash column.
USE smart_parking;

ALTER TABLE users
    ADD COLUMN password_hash VARCHAR(60) NULL;

-- Existing users cannot sign in until they register again or their password
-- is securely reset and a BCrypt hash is stored for their account.
