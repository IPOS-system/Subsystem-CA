CREATE DATABASE IF NOT EXISTS ipos_ca;
USE ipos_ca;

-- i made it so that this is actually meant to be re run every time you reload the program.

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS Sale_Items;
DROP TABLE IF EXISTS Payments;
DROP TABLE IF EXISTS Sales;
DROP TABLE IF EXISTS Monthly_Debts;
DROP TABLE IF EXISTS Customers;
DROP TABLE IF EXISTS Discount_Tiers;
DROP TABLE IF EXISTS Discount_Plans;
DROP TABLE IF EXISTS Items;
DROP TABLE IF EXISTS LocalUser;
DROP TABLE IF EXISTS test;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE IF NOT EXISTS test (
                                    id INT PRIMARY KEY
);

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


CREATE TABLE IF NOT EXISTS Customers (
                                         account_id VARCHAR(10) PRIMARY KEY,
                                         account_holder_name VARCHAR(100) NOT NULL,
                                         contact_name VARCHAR(100),
                                         address VARCHAR(255),
                                         phone VARCHAR(20),
                                         credit_limit DECIMAL(10,2) NOT NULL,
                                         account_status VARCHAR(20) NOT NULL DEFAULT 'active'
    -- customer deletion should remove its plan
);
INSERT IGNORE INTO Customers (
    account_id,
    account_holder_name,
    contact_name,
    address,
    phone,
    credit_limit,
    account_status
) VALUES
      (
          'ACC0001',
          'Ms Eva Bauyer',
          'Ms Eva Bauyer',
          '1, Liverpool street, London EC2V 8NS',
          '0207 321 8001',
          500.00,
          'active'
      ),
      (
          'ACC0002',
          'Mr Glynne Morrison',
          'Ms Glynne Morisson',
          '1, Liverpool street, London EC2V 8NS',
          '0207 321 8001',
          500.00,
          'active'
      );


CREATE TABLE IF NOT EXISTS Discount_Plans (
                                              plan_id INT AUTO_INCREMENT PRIMARY KEY,
                                              account_id VARCHAR(10) NOT NULL UNIQUE,
                                              plan_name VARCHAR(100) NOT NULL,
                                              plan_type VARCHAR(20) NOT NULL,
                                              fixed_rate DECIMAL(5,2),
                                              FOREIGN KEY (account_id) REFERENCES Customers(account_id)
);

CREATE TABLE IF NOT EXISTS Discount_Tiers (
                                              tier_id INT AUTO_INCREMENT PRIMARY KEY,
                                              plan_id INT NOT NULL,
                                              min_amount DECIMAL(10,2) NOT NULL,
                                              max_amount DECIMAL(10,2),
                                              discount_rate DECIMAL(5,2) NOT NULL,
                                              FOREIGN KEY (plan_id) REFERENCES Discount_Plans(plan_id)
                                          ON DELETE CASCADE
    -- tiers should die when plan dies

);
INSERT IGNORE INTO Discount_Plans (plan_id, account_id, plan_name, plan_type, fixed_rate)
VALUES (1, 'ACC0001', 'Fixed 3 Percent', 'fixed', 3.00);

INSERT IGNORE INTO Discount_Plans (plan_id, account_id, plan_name, plan_type, fixed_rate)
VALUES (2, 'ACC0002', 'Variable Plan', 'tiered', NULL);

INSERT IGNORE INTO Discount_Tiers (
    tier_id,
    plan_id,
    min_amount,
    max_amount,
    discount_rate
) VALUES
      (1, 2, 0.00, 99.99, 0.00),
      (2, 2, 100.00, 299.99, 1.00),
      (3, 2, 300.00, NULL, 2.00);

CREATE TABLE IF NOT EXISTS Monthly_Debts (
                                             debt_id INT AUTO_INCREMENT PRIMARY KEY,
                                             account_id VARCHAR(10) NOT NULL,
                                             debt_month DATE NOT NULL,
                                             due_date DATE NOT NULL,
                                             total_amount DECIMAL(10,2) NOT NULL,
                                             remaining_amount DECIMAL(10,2) NOT NULL,
                                             status_1stReminder VARCHAR(10),
                                             first_reminder_date DATE,
                                             status_2ndReminder VARCHAR(10),
                                             second_reminder_date DATE,
                                             FOREIGN KEY (account_id) REFERENCES Customers(account_id)

);

CREATE TABLE IF NOT EXISTS Sales (
                                     sale_id INT AUTO_INCREMENT PRIMARY KEY,
                                     account_id VARCHAR(10),
                                     sale_date DATE NOT NULL,
                                     total_amount DECIMAL(10,2) NOT NULL,
                                     payment_method VARCHAR(20) NOT NULL,
                                     debt_id INT,
                                     FOREIGN KEY (account_id) REFERENCES Customers(account_id),
                                     FOREIGN KEY (debt_id) REFERENCES Monthly_Debts(debt_id)
                                     ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS Items (
                                     item_id VARCHAR(12) PRIMARY KEY,
                                     description VARCHAR(255) NOT NULL,
                                     package_type VARCHAR(50) NOT NULL,
                                     unit VARCHAR(50) NOT NULL,
                                     units_in_pack INT NOT NULL,
                                     package_cost DECIMAL(10,2) NOT NULL,
                                     quantity_in_stock INT NOT NULL,
                                     stock_limit INT NOT NULL
);

INSERT IGNORE INTO Items (
    item_id, description, package_type, unit, units_in_pack,
    package_cost, quantity_in_stock, stock_limit
) VALUES
      ('10000001', 'Paracetamol', 'Box', 'Caps', 20, 0.10, 121, 10),
      ('10000002', 'Aspirin', 'Box', 'Caps', 20, 0.50, 201, 15),
      ('10000003', 'Analgin', 'Box', 'Caps', 10, 1.20, 25, 10),
      ('10000004', 'Celebrex, caps 100 mg', 'Box', 'Caps', 10, 10.00, 43, 10),
      ('10000005', 'Celebrex, caps 200 mg', 'Box', 'Caps', 10, 18.50, 35, 5),
      ('10000006', 'Retin-A Tretin, 30 g', 'Box', 'Caps', 20, 25.00, 28, 10),
      ('10000007', 'Lipitor TB, 20 mg', 'Box', 'Caps', 30, 15.50, 10, 10),
      ('10000008', 'Claritin CR, 60g', 'Box', 'Caps', 20, 19.50, 21, 10),
      ('20000004', 'Iodine tincture', 'Bottle', 'Ml', 100, 0.30, 35, 10),
      ('20000005', 'Rhynol', 'Bottle', 'Ml', 200, 2.50, 14, 15),
      ('30000001', 'Ospen', 'Box', 'Caps', 20, 10.50, 78, 10),
      ('30000002', 'Amopen', 'Box', 'Caps', 30, 15.00, 90, 15),
      ('40000001', 'Vitamin C', 'Box', 'Caps', 30, 1.20, 22, 15),
      ('40000002', 'Vitamin B12', 'Box', 'Caps', 30, 1.30, 43, 15);

CREATE TABLE IF NOT EXISTS Sale_Items (
                                          sale_id INT NOT NULL,
                                          item_id VARCHAR(12) NOT NULL,
                                          quantity INT NOT NULL,
                                          sale_price DECIMAL(10,2) NOT NULL,
                                          PRIMARY KEY (sale_id, item_id),
                                          FOREIGN KEY (sale_id) REFERENCES Sales(sale_id) ,
                                          FOREIGN KEY (item_id) REFERENCES Items(item_id)
);

CREATE TABLE IF NOT EXISTS Payments (
                                        payment_id INT PRIMARY KEY,
                                        account_id VARCHAR(10) NOT NULL,
                                        payment_date DATE NOT NULL,
                                        amount DECIMAL(10,2) NOT NULL,
                                        payment_method VARCHAR(20) NOT NULL,
                                        FOREIGN KEY (account_id) REFERENCES Customers(account_id)
);