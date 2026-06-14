-- FraudGuard AI Seed Data
-- Seed Roles
INSERT INTO ROLES (name) VALUES ('ROLE_SUPER_ADMIN');
INSERT INTO ROLES (name) VALUES ('ROLE_FRAUD_ANALYST');
INSERT INTO ROLES (name) VALUES ('ROLE_AUDITOR');

-- Seed Users (Password is Password@123 for all: BCrypt hash $2a$10$k1h5Fp9y7D2n7q5kQv4SJeX0Y3aW/e6nO4U32O6y7nK2z/L2S1L2C)
INSERT INTO USERS (username, password, email, full_name, active, locked, failed_login_attempts, created_at, updated_at) 
VALUES ('admin@fraudguard.ai', '$2a$10$k1h5Fp9y7D2n7q5kQv4SJeX0Y3aW/e6nO4U32O6y7nK2z/L2S1L2C', 'admin@fraudguard.ai', 'Super Administrator', 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO USERS (username, password, email, full_name, active, locked, failed_login_attempts, created_at, updated_at) 
VALUES ('analyst@fraudguard.ai', '$2a$10$k1h5Fp9y7D2n7q5kQv4SJeX0Y3aW/e6nO4U32O6y7nK2z/L2S1L2C', 'analyst@fraudguard.ai', 'Fraud Analyst', 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO USERS (username, password, email, full_name, active, locked, failed_login_attempts, created_at, updated_at) 
VALUES ('auditor@fraudguard.ai', '$2a$10$k1h5Fp9y7D2n7q5kQv4SJeX0Y3aW/e6nO4U32O6y7nK2z/L2S1L2C', 'auditor@fraudguard.ai', 'System Auditor', 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Map Users to Roles
-- Super Admin (user_id = 1, role_id = 1)
INSERT INTO USER_ROLES (user_id, role_id) VALUES (1, 1);
-- Fraud Analyst (user_id = 2, role_id = 2)
INSERT INTO USER_ROLES (user_id, role_id) VALUES (2, 2);
-- Auditor (user_id = 3, role_id = 3)
INSERT INTO USER_ROLES (user_id, role_id) VALUES (3, 3);

-- Seed Sample Transactions (representing a variety of cases for Chart.js)
INSERT INTO TRANSACTIONS (account_number, amount, transaction_date, location, device, status)
VALUES ('ACC-90823412', 450.00, CURRENT_TIMESTAMP - INTERVAL '5' DAY, 'New York, US', 'iPhone 15', 'APPROVED');

INSERT INTO TRANSACTIONS (account_number, amount, transaction_date, location, device, status)
VALUES ('ACC-12093847', 15000.00, CURRENT_TIMESTAMP - INTERVAL '4' DAY, 'London, UK', 'MacBook Pro', 'PENDING_REVIEW');

INSERT INTO TRANSACTIONS (account_number, amount, transaction_date, location, device, status)
VALUES ('ACC-55463728', 75.50, CURRENT_TIMESTAMP - INTERVAL '3' DAY, 'Chicago, US', 'Dell XPS 13', 'APPROVED');

INSERT INTO TRANSACTIONS (account_number, amount, transaction_date, location, device, status)
VALUES ('ACC-90823412', 52000.00, CURRENT_TIMESTAMP - INTERVAL '2' DAY, 'Lagos, NG', 'Android Phone', 'BLOCKED');

INSERT INTO TRANSACTIONS (account_number, amount, transaction_date, location, device, status)
VALUES ('ACC-77483920', 2500.00, CURRENT_TIMESTAMP - INTERVAL '1' DAY, 'San Jose, US', 'iPad Pro', 'PENDING_REVIEW');

INSERT INTO TRANSACTIONS (account_number, amount, transaction_date, location, device, status)
VALUES ('ACC-11223344', 95.00, CURRENT_TIMESTAMP, 'New York, US', 'iPhone 15', 'APPROVED');

-- Seed Sample Fraud Results for the transactions above
INSERT INTO FRAUD_RESULTS (transaction_id, fraud_score, risk_level, fraud_reason, created_at)
VALUES (1, 10.0, 'LOW', 'Transaction pattern normal. Device and location match historical profile.', CURRENT_TIMESTAMP - INTERVAL '5' DAY);

INSERT INTO FRAUD_RESULTS (transaction_id, fraud_score, risk_level, fraud_reason, created_at)
VALUES (2, 45.0, 'MEDIUM', 'Transaction amount ($15,000) exceeds threshold. Location normal.', CURRENT_TIMESTAMP - INTERVAL '4' DAY);

INSERT INTO FRAUD_RESULTS (transaction_id, fraud_score, risk_level, fraud_reason, created_at)
VALUES (3, 5.0, 'LOW', 'Transaction pattern normal.', CURRENT_TIMESTAMP - INTERVAL '3' DAY);

INSERT INTO FRAUD_RESULTS (transaction_id, fraud_score, risk_level, fraud_reason, created_at)
VALUES (4, 95.0, 'CRITICAL', 'Critical alert: blacklisted account ACC-90823412, transaction amount ($52,000) exceeds threshold, geographic location mismatch (Lagos, NG vs US).', CURRENT_TIMESTAMP - INTERVAL '2' DAY);

INSERT INTO FRAUD_RESULTS (transaction_id, fraud_score, risk_level, fraud_reason, created_at)
VALUES (5, 65.0, 'HIGH', 'High risk: Transaction amount is large and device is unknown.', CURRENT_TIMESTAMP - INTERVAL '1' DAY);

INSERT INTO FRAUD_RESULTS (transaction_id, fraud_score, risk_level, fraud_reason, created_at)
VALUES (6, 8.0, 'LOW', 'Transaction pattern normal.', CURRENT_TIMESTAMP);

-- Seed System Notifications
INSERT INTO NOTIFICATIONS (message, read_status, created_at, target_role)
VALUES ('Critical Fraud Detected: Transaction 4 marked as Critical (Score: 95).', 0, CURRENT_TIMESTAMP - INTERVAL '2' DAY, 'ROLE_SUPER_ADMIN');

INSERT INTO NOTIFICATIONS (message, read_status, created_at, target_role)
VALUES ('Critical Fraud Detected: Transaction 4 marked as Critical (Score: 95).', 0, CURRENT_TIMESTAMP - INTERVAL '2' DAY, 'ROLE_FRAUD_ANALYST');

INSERT INTO NOTIFICATIONS (message, read_status, created_at, target_role)
VALUES ('High Risk Transaction detected for account ACC-77483920.', 0, CURRENT_TIMESTAMP - INTERVAL '1' DAY, 'ROLE_FRAUD_ANALYST');

-- Seed Audit Logs
INSERT INTO AUDIT_LOGS (username, action, timestamp, ip_address, browser, module)
VALUES ('system', 'Database Initialized', CURRENT_TIMESTAMP - INTERVAL '5' DAY, '127.0.0.1', 'System Core', 'DATABASE');

INSERT INTO AUDIT_LOGS (username, action, timestamp, ip_address, browser, module)
VALUES ('admin@fraudguard.ai', 'User Login Successful', CURRENT_TIMESTAMP - INTERVAL '2' DAY, '192.168.1.10', 'Chrome 125.0', 'SECURITY');
