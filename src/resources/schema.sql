CREATE DATABASE IF NOT EXISTS ipos_ca;
USE ipos_ca;

CREATE TABLE IF NOT EXISTS test (
                                    id INT PRIMARY KEY

);

#for local users like admin, pharmacist. etc
#please use IF NOT EXISTS and IGNORE INTO because the schema is probably gonna be re run in development
#and it will cause errors.
CREATE TABLE IF NOT EXISTS LocalUser (
                           user_id INT AUTO_INCREMENT PRIMARY KEY,
                           username VARCHAR(50) NOT NULL UNIQUE,
                           password VARCHAR(255) NOT NULL,
                           role VARCHAR(20) NOT NULL
);

INSERT IGNORE INTO LocalUser (username, password, role)
VALUES
    ('admin', 'admin', 'Administrator'),
    ('sysdba', 'masterkey', 'Administrator'),
    ('manager', 'Get_it_done', 'Director of Operations/Manager'),
    ('accountant', 'Count_money', 'Senior accountant'),
    ('clerk', 'Paperwork', 'Accountant');


