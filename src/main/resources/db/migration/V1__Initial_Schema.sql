CREATE TABLE batches (
    batch_id INT AUTO_INCREMENT PRIMARY KEY,
    batch_name VARCHAR(100) NOT NULL,
    batch_code VARCHAR(20) NOT NULL UNIQUE,
    max_count INT NOT NULL,
    description VARCHAR(255),
    start_date DATE,
    end_date DATE,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_batch_code (batch_code),
    INDEX idx_batch_name (batch_name)
);

CREATE TABLE students (
    sid INT AUTO_INCREMENT PRIMARY KEY,
    sname VARCHAR(100) NOT NULL,
    email VARCHAR(50),
    phone VARCHAR(20),
    roll_number VARCHAR(50),
    intime TIMESTAMP,
    outtime TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'ABSENT',
    batch_id INT,
    INDEX idx_student_name (sname),
    INDEX idx_check_in_time (intime),
    FOREIGN KEY (batch_id) REFERENCES batches(batch_id)
);

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    student_id INT,
    FOREIGN KEY (student_id) REFERENCES students(sid)
);
