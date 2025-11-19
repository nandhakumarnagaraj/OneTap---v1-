-- data.sql - Initial test data for SAMS

-- Insert test users
INSERT INTO users (username, password, email, enabled, account_non_expired, account_non_locked, credentials_non_expired, role) 
VALUES
('john.doe', '$2a$10$slYQmyNdGzin7olVN3p5Be7DQns4Z.G67PwUHiHxyvBoPLH/5K0bm', 'john.doe@example.com', true, true, true, true, 'STUDENT'),
('jane.smith', '$2a$10$slYQmyNdGzin7olVN3p5Be7DQns4Z.G67PwUHiHxyvBoPLH/5K0bm', 'jane.smith@example.com', true, true, true, true, 'STUDENT'),
('teacher_mike', '$2a$10$slYQmyNdGzin7olVN3p5Be7DQns4Z.G67PwUHiHxyvBoPLH/5K0bm', 'mike.teacher@example.com', true, true, true, true, 'TEACHER'),
('admin_alice', '$2a$10$slYQmyNdGzin7olVN3p5Be7DQns4Z.G67PwUHiHxyvBoPLH/5K0bm', 'alice.admin@example.com', true, true, true, true, 'ADMIN');

-- Insert test batches (DO NOT specify batch_id - let it auto-increment)
INSERT INTO batches (batch_name, batch_code, max_count, description, start_date, end_date, status) 
VALUES
('Spring Batch 2023', 'SB2023', 50, 'Spring Framework fundamentals', '2023-01-15', '2023-06-15', 'ACTIVE'),
('Java Fundamentals 2023', 'JF2023', 40, 'Core Java concepts and OOP principles', '2023-02-01', '2023-05-01', 'COMPLETED'),
('Web Development 2024', 'WD2024', 35, 'Full-stack web development', '2024-03-01', '2024-08-31', 'ACTIVE');

-- Insert test students (DO NOT specify sid - let it auto-increment)
-- Note: batch_id 1 corresponds to first batch (Spring Batch 2023)
INSERT INTO students (sname, email, phone, roll_number, batch_id) 
VALUES
('Alice Wonderland', 'alice.w@example.com', '9876543210', 'CS-2024-001', 1),
('Bob Builder', 'bob.b@example.com', '8765432109', 'CS-2024-002', 1),
('Charlie Chaplin', 'charlie.c@example.com', '7654321098', 'CS-2024-003', 2),
('Diana Prince', 'diana.p@example.com', '6543210987', 'CS-2024-004', 2),
('Eve Smith', 'eve.s@example.com', '5432109876', 'CS-2024-005', 3);