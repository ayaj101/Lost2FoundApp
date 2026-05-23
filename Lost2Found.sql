create database lost2found;
use lost2found;
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,  -- BIGINT for FK consistency
    fullname VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(15) NOT NULL,
    address VARCHAR(255),
    id_type VARCHAR(50),
    id_number VARCHAR(50),
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);



CREATE TABLE IF NOT EXISTS lost_items (
    lost_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(120) NOT NULL,
    description TEXT,
    location VARCHAR(120),
    event_date DATE,
    image_path VARCHAR(255),
    description_emb JSON,
    auto_desc_emb JSON,
    status ENUM('OPEN','MATCHED','CLOSED') NOT NULL DEFAULT 'OPEN',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS found_items (
    found_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(120) NOT NULL,
    description TEXT,
    auto_desc TEXT,
    location VARCHAR(120),
    event_date DATE,
    image_path VARCHAR(255),
    description_emb JSON,
    auto_desc_emb JSON,
    status ENUM('OPEN','MATCHED','CLOSED') NOT NULL DEFAULT 'OPEN',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
select * from users;
select * from lost_items;
select * from found_items;
Select * from matches;
select * from notifications;

DROP TABLE IF EXISTS matches;
CREATE TABLE matches (
    match_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lost_id BIGINT NOT NULL,
    found_id BIGINT NOT NULL,
    score DECIMAL(5,4) NOT NULL,
    matched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    admin_verified TINYINT(1) DEFAULT 0,
    FOREIGN KEY (lost_id) REFERENCES lost_items(lost_id) ON DELETE CASCADE,
    FOREIGN KEY (found_id) REFERENCES found_items(found_id) ON DELETE CASCADE
);

-- NOTIFICATIONS TABLE (Optional)
CREATE TABLE notifications (
    notif_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- PERFORMANCE INDEXES
CREATE INDEX idx_lost_loc_date ON lost_items(location, event_date);
CREATE INDEX idx_found_loc_date ON found_items(location, event_date);
CREATE INDEX idx_status ON lost_items(status);
CREATE INDEX idx_status_found ON found_items(status);


ALTER TABLE lost_items MODIFY description_emb LONGTEXT;
ALTER TABLE lost_items MODIFY auto_desc_emb LONGTEXT;

ALTER TABLE found_items MODIFY description_emb LONGTEXT;
ALTER TABLE found_items MODIFY auto_desc_emb LONGTEXT;

DESCRIBE matches;
DESC users;