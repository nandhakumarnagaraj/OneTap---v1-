INSERT INTO users (username, password, email, enabled, account_non_expired, account_non_locked, credentials_non_expired, role) VALUES
('john.doe', 'password123', 'john.doe@example.com', true, true, true, true, 'STUDENT'),
('jane.smith', 'password456', 'jane.smith@example.com', true, true, true, true, 'STUDENT');

INSERT INTO batches (batch_id, batch_name, start_date, end_date, batch_code, max_count) VALUES
(1, 'Spring Batch 2023', '2023-01-15', '2023-06-15', 'SB2023', 50),
(2, 'Java Fundamentals 2023', '2023-02-01', '2023-05-01', 'JF2023', 40);

INSERT INTO students (sid, sname, email, phone, batch_id) VALUES
(101, 'Alice Wonderland', 'alice.w@example.com', '123-456-7890', 1),
(102, 'Bob Builder', 'bob.b@example.com', '234-567-8901', 1),
(103, 'Charlie Chaplin', 'charlie.c@example.com', '345-678-9012', 2);
