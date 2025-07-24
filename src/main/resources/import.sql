-- Custom DDL to control column order
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS company CASCADE;

-- Create company table first (referenced by users)
CREATE TABLE company (
    id BIGSERIAL PRIMARY KEY,
    company_name VARCHAR(255),
    pib_number VARCHAR(50),
    phone_number VARCHAR(20),
    country VARCHAR(100),
    city VARCHAR(100),
    zip_code VARCHAR(20)
);

-- Create users table with specific column order
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    account_type VARCHAR(20),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    username VARCHAR(50),
    email VARCHAR(255),
    phone_number VARCHAR(20),
    password_hash VARCHAR(255),
    country VARCHAR(100),
    disabled BOOLEAN DEFAULT FALSE,
    company_id BIGINT REFERENCES company(id)
); 