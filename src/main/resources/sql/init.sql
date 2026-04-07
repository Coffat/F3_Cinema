-- Initialization Script for F3 Cinema Database (MySQL 8.4)
-- Strictly matching provided ERD structure
-- Database Name: f3_cinema

DROP DATABASE IF EXISTS f3_cinema;
CREATE DATABASE f3_cinema CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE f3_cinema;

-- Ensure the MySQL client interprets this SQL file as UTF-8 (prevents mojibake).
-- Without this, Vietnamese strings in INSERT can be stored incorrectly after `docker compose down -v`.
SET NAMES utf8mb4;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;

-- BỔ SUNG ĐOẠN NÀY ĐỂ FIX LỖI 172.18.0.1 (Cấp quyền cho root)
CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED BY '123456';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;


-- 1. USERS Table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL, -- ADMIN, STAFF
    full_name VARCHAR(100),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username)
);

-- 2. CUSTOMERS Table
CREATE TABLE IF NOT EXISTS customers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(15) UNIQUE,
    points INT DEFAULT 0
);

-- 3. GENRES Table
CREATE TABLE IF NOT EXISTS genres (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- 4. MOVIES Table
CREATE TABLE IF NOT EXISTS movies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    duration INT, -- in minutes
    status VARCHAR(50), -- NOW_SHOWING, COMING_SOON, ENDED
    poster_url TEXT, -- Path or URL to movie poster image
    deleted_at DATETIME DEFAULT NULL
);

-- 5. MOVIE_GENRES Mapping
CREATE TABLE IF NOT EXISTS movie_genres (
    movie_id INT NOT NULL,
    genre_id INT NOT NULL,
    PRIMARY KEY (movie_id, genre_id),
    CONSTRAINT fk_movie_genres_movie FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE,
    CONSTRAINT fk_movie_genres_genre FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE CASCADE
);

-- 6. ROOMS Table
CREATE TABLE IF NOT EXISTS rooms (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    type VARCHAR(20) NOT NULL -- 2D, 3D, IMAX
);

-- 7. SEATS Table
CREATE TABLE IF NOT EXISTS seats (
    id INT AUTO_INCREMENT PRIMARY KEY,
    room_id INT NOT NULL,
    row_char VARCHAR(2) NOT NULL,
    number INT NOT NULL,
    type VARCHAR(20) NOT NULL, -- NORMAL, VIP, SWEETBOX
    CONSTRAINT fk_seats_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
);

-- 8. SHOWTIMES Table
CREATE TABLE IF NOT EXISTS showtimes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    movie_id INT NOT NULL,
    room_id INT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    base_price DECIMAL(19, 2) NOT NULL,
    CONSTRAINT fk_showtimes_movie FOREIGN KEY (movie_id) REFERENCES movies(id),
    CONSTRAINT fk_showtimes_room FOREIGN KEY (room_id) REFERENCES rooms(id)
);

-- (Other tables truncated for brevity, but I should keep them)
-- 9. PRODUCTS Table
CREATE TABLE IF NOT EXISTS products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(19, 2) NOT NULL,
    unit VARCHAR(50),
    image_url TEXT,
    deleted_at DATETIME DEFAULT NULL
);

-- 10. INVENTORIES Table
CREATE TABLE IF NOT EXISTS inventories (
    product_id INT PRIMARY KEY,
    current_quantity INT DEFAULT 0,
    min_threshold INT DEFAULT 10,
    CONSTRAINT fk_inventories_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- 11. STOCK_RECEIPTS Table
CREATE TABLE IF NOT EXISTS stock_receipts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    receipt_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    supplier VARCHAR(255),
    total_import_cost DECIMAL(19, 2) NOT NULL
);

-- 12. STOCK_RECEIPT_ITEMS Table
CREATE TABLE IF NOT EXISTS stock_receipt_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    receipt_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    import_price DECIMAL(19, 2) NOT NULL,
    CONSTRAINT fk_stock_items_receipt FOREIGN KEY (receipt_id) REFERENCES stock_receipts(id) ON DELETE CASCADE,
    CONSTRAINT fk_stock_items_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- 13. PROMOTIONS Table
CREATE TABLE IF NOT EXISTS promotions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    discount_percent DECIMAL(5, 2)
);

-- 14. INVOICES Table
CREATE TABLE IF NOT EXISTS invoices (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    customer_id INT DEFAULT NULL,
    promotion_id INT DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PENDING',
    final_total DECIMAL(19, 2) NOT NULL,
    CONSTRAINT fk_invoices_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_invoices_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_invoices_promotion FOREIGN KEY (promotion_id) REFERENCES promotions(id)
);

-- 15. TICKETS Table
CREATE TABLE IF NOT EXISTS tickets (
    id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_id INT NOT NULL,
    showtime_id INT NOT NULL,
    seat_id INT NOT NULL,
    final_price DECIMAL(19, 2) NOT NULL,
    CONSTRAINT fk_tickets_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE,
    CONSTRAINT fk_tickets_showtime FOREIGN KEY (showtime_id) REFERENCES showtimes(id),
    CONSTRAINT fk_tickets_seat FOREIGN KEY (seat_id) REFERENCES seats(id)
);

-- 16. INVOICE_ITEMS Table
CREATE TABLE IF NOT EXISTS invoice_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(19, 2) NOT NULL,
    CONSTRAINT fk_invoice_items_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE,
    CONSTRAINT fk_invoice_items_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- 17. PAYMENTS Table
CREATE TABLE IF NOT EXISTS payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_id INT NOT NULL UNIQUE,
    amount DECIMAL(19, 2) NOT NULL,
    method VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    transaction_id VARCHAR(100) NULL,
    CONSTRAINT fk_payments_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE
);

-- ==========================================
-- SEED DATA
-- ==========================================

-- 1. Users (pwd: '1')
INSERT INTO users (username, password, role, full_name) VALUES 
('admin', '$2a$12$eUnQAUgU6wG1akE0xbsAYOkKsx.joNW1QHah.6A7fO7suIDtAnA76', 'ADMIN', 'System Administrator'),
('staff', '$2a$12$eUnQAUgU6wG1akE0xbsAYOkKsx.joNW1QHah.6A7fO7suIDtAnA76', 'STAFF', 'Cinema Staff 01'),
('staff02', '$2a$12$eUnQAUgU6wG1akE0xbsAYOkKsx.joNW1QHah.6A7fO7suIDtAnA76', 'STAFF', 'Cinema Staff 02'),
('staff03', '$2a$12$eUnQAUgU6wG1akE0xbsAYOkKsx.joNW1QHah.6A7fO7suIDtAnA76', 'STAFF', 'Cinema Staff 03'),
('staff04', '$2a$12$eUnQAUgU6wG1akE0xbsAYOkKsx.joNW1QHah.6A7fO7suIDtAnA76', 'STAFF', 'Cinema Staff 04'),
('staff05', '$2a$12$eUnQAUgU6wG1akE0xbsAYOkKsx.joNW1QHah.6A7fO7suIDtAnA76', 'STAFF', 'Cinema Staff 05');

-- 2. Genres
INSERT IGNORE INTO genres (name) VALUES 
('Action'), ('Comedy'), ('Drama'), ('Horror'), ('Sci-Fi'), ('Animation'), ('Thriller'), ('Romance');

-- 3. Movies
INSERT IGNORE INTO movies (id, title, duration, status, poster_url) VALUES 
(1, 'Avatar: The Way of Water', 192, 'NOW_SHOWING', 'https://upload.wikimedia.org/wikipedia/en/5/54/Avatar_The_Way_of_Water_poster.jpg'),
(2, 'Oppenheimer', 180, 'NOW_SHOWING', 'https://upload.wikimedia.org/wikipedia/en/4/4a/Oppenheimer_%28film%29.jpg'),
(3, 'Dune: Part Two', 166, 'NOW_SHOWING', 'https://upload.wikimedia.org/wikipedia/en/5/52/Dune_Part_Two_poster.jpeg'),
(4, 'Deadpool & Wolverine', 127, 'COMING_SOON', 'https://upload.wikimedia.org/wikipedia/en/4/4c/Deadpool_%26_Wolverine_poster.jpg'),
(5, 'Despicable Me 4', 94, 'NOW_SHOWING', 'https://upload.wikimedia.org/wikipedia/en/e/ed/Despicable_Me_4_Theatrical_Release_Poster.jpeg'),
(6, 'Spider-Man: Across the Spider-Verse', 140, 'ENDED', 'https://m.media-amazon.com/images/M/MV5BMzI0NmVkMjEtYmY4MS00ZDMxLTlkZmEtMzU4MDQxYTMzMjU2XkEyXkFqcGdeQXVyMzQ0MzA0NTM@._V1_.jpg');

-- 4. Movie-Genre Mapping
INSERT IGNORE INTO movie_genres (movie_id, genre_id) VALUES 
(1, 1), (1, 5), (2, 3), (2, 7), (3, 1), (3, 5), (4, 1), (4, 2), (5, 6), (5, 2), (6, 6), (6, 1);

-- ==========================================
-- SEED DATA (UPDATE THEO TEMPLATE MỚI)
-- ==========================================

-- 5. Rooms (3 Khuôn mẫu phòng chuẩn)
INSERT IGNORE INTO rooms (id, name, type) VALUES 
(1, N'Phòng 1 (Tiêu Chuẩn)', 'ROOM_2D'),
(2, N'Phòng 2 (IMAX)', 'ROOM_IMAX'),
(3, N'Phòng 3 (Nhỏ)', 'ROOM_2D');

-- 6. SEATS GENERATION (Using Recursive CTE for compatibility - No DELIMITER needed)
-- Room 1: Tiêu Chuẩn (10 rows x 10 cols = 100 seats)
INSERT INTO seats (room_id, row_char, number, type)
WITH RECURSIVE rows_cte AS (SELECT 0 AS r UNION ALL SELECT r + 1 FROM rows_cte WHERE r < 9),
               cols_cte AS (SELECT 1 AS c UNION ALL SELECT c + 1 FROM cols_cte WHERE c < 10)
SELECT 1, CHAR(65 + r), c, CASE WHEN r >= 4 AND r <= 7 THEN 'VIP' ELSE 'NORMAL' END
FROM rows_cte CROSS JOIN cols_cte;

-- Room 2: Lớn / IMAX (10 rows x 16 cols = 160 seats)
INSERT INTO seats (room_id, row_char, number, type)
WITH RECURSIVE rows_cte AS (SELECT 0 AS r UNION ALL SELECT r + 1 FROM rows_cte WHERE r < 9),
               cols_cte AS (SELECT 1 AS c UNION ALL SELECT c + 1 FROM cols_cte WHERE c < 15)
SELECT 2, CHAR(65 + r), c, CASE WHEN r >= 5 THEN 'VIP' ELSE 'NORMAL' END
FROM rows_cte CROSS JOIN cols_cte;

-- Room 3: Nhỏ (6 rows x 10 cols = 60 seats)
INSERT INTO seats (room_id, row_char, number, type)
WITH RECURSIVE rows_cte AS (SELECT 0 AS r UNION ALL SELECT r + 1 FROM rows_cte WHERE r < 5),
               cols_cte AS (SELECT 1 AS c UNION ALL SELECT c + 1 FROM cols_cte WHERE c < 10)
SELECT 3, CHAR(65 + r), c, 'NORMAL'
FROM rows_cte CROSS JOIN cols_cte;

-- 7. Showtimes (Cập nhật id phòng chiếu tương ứng với 3 phòng trên)
INSERT IGNORE INTO showtimes (id, movie_id, room_id, start_time, end_time, base_price) VALUES 
(4, 4, 1, DATE_ADD(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), INTERVAL '20:00:00' HOUR_SECOND), DATE_ADD(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), INTERVAL '22:07:00' HOUR_SECOND), 90000.00);

-- 9. Products (Snacks & Drinks)
INSERT IGNORE INTO products (id, name, price, unit, image_url) VALUES 
(1, N'Bắp rang Caramel (L)', 65000.00, N'Hộp', 'https://images.unsplash.com/photo-1578849278619-e73505e9610f?w=400'),
(2, N'Bắp rang Phô mai (L)', 65000.00, N'Hộp', 'https://images.unsplash.com/photo-1585647347384-2593bc35786b?w=400'),
(3, N'Bắp rang Caramel (M)', 55000.00, N'Hộp', 'https://images.unsplash.com/photo-1578849278619-e73505e9610f?w=400'),
(4, N'Bắp rang Phô mai (M)', 55000.00, N'Hộp', 'https://images.unsplash.com/photo-1585647347384-2593bc35786b?w=400'),
(5, 'Coca Cola (L)', 35000.00, 'Ly', 'https://images.unsplash.com/photo-1554866585-cd94860890b7?w=400'),
(6, 'Sprite (L)', 35000.00, 'Ly', 'https://images.unsplash.com/photo-1625772299848-391b6a87d7b3?w=400'),
(7, 'Fanta (L)', 35000.00, 'Ly', 'https://images.unsplash.com/photo-1624517452488-04869289c4ca?w=400'),
(8, N'Nước suối Dasani', 20000.00, 'Chai', 'https://images.unsplash.com/photo-1548839140-29a749e1cf4d?w=400'),
(9, N'Combo Solo (1 Bắp L + 1 Nước L)', 85000.00, 'Combo', 'https://images.unsplash.com/photo-1627662235654-e188a0c1b8df?w=400'),
(10, N'Combo Couple (1 Bắp L + 2 Nước L)', 105000.00, 'Combo', 'https://images.unsplash.com/photo-1505686994434-e3cc5abf1330?w=400');

-- 10. Inventories
INSERT IGNORE INTO inventories (product_id, current_quantity, min_threshold) VALUES 
(1, 100, 10),
(2, 100, 10),
(3, 100, 10),
(4, 100, 10),
(5, 200, 20),
(6, 200, 20),
(7, 200, 20),
(8, 150, 15),
(9, 50, 5),
(10, 50, 5);

-- 11. Vouchers
CREATE TABLE IF NOT EXISTS vouchers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    voucher_type VARCHAR(30) NOT NULL DEFAULT 'PERCENTAGE',
    discount_percent DECIMAL(5,2),
    discount_amount DECIMAL(19,2),
    max_discount DECIMAL(19,2),
    min_order_amount DECIMAL(19,2),
    buy_quantity INT,
    get_quantity INT,
    applies_to_category VARCHAR(50),
    valid_from DATETIME NOT NULL,
    valid_until DATETIME NOT NULL,
    usage_limit INT,
    usage_count INT DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT IGNORE INTO vouchers (code, description, voucher_type, discount_percent, discount_amount, max_discount, min_order_amount, buy_quantity, get_quantity, applies_to_category, valid_from, valid_until, usage_limit, status) VALUES
('F3CINEMA10', N'Giảm 10% cho đơn hàng', 'PERCENTAGE', 10.00, NULL, 50000.00, 100000.00, NULL, NULL, NULL, CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), 100, 'ACTIVE'),
('F3CINEMA20', N'Giảm 20% cho đơn hàng', 'PERCENTAGE', 20.00, NULL, 100000.00, 200000.00, NULL, NULL, NULL, CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), 50, 'ACTIVE'),
('F3WELCOME', N'Chào mừng khách mới', 'PERCENTAGE', 15.00, NULL, 75000.00, 150000.00, NULL, NULL, NULL, CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 60 DAY), 200, 'ACTIVE'),
('FIXED50K', N'Giảm cố định 50,000đ', 'FIXED_AMOUNT', NULL, 50000.00, NULL, 150000.00, NULL, NULL, NULL, CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), 50, 'ACTIVE'),
('BUY2GET1', N'Mua 2 ghế tặng 1', 'BUY_X_GET_Y', NULL, NULL, NULL, 0, 2, 1, NULL, CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), 30, 'ACTIVE'),
('COMBO30', N'Giảm 30% combo bắp nước', 'COMBO_DISCOUNT', 30.00, NULL, 50000.00, 0, NULL, NULL, 'COMBO', CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY), 100, 'ACTIVE');

-- 12. Dashboard Sample Data (dynamic, valid for at least next 7 days)
-- Keep low-stock alerts visible.
UPDATE inventories SET current_quantity = 3  WHERE product_id = 1;
UPDATE inventories SET current_quantity = 10 WHERE product_id = 5;
UPDATE inventories SET current_quantity = 1  WHERE product_id = 9;

-- Customers reserved for daily dashboard invoices (today -> next 7 days).
INSERT IGNORE INTO customers (id, full_name, phone, points) VALUES
(4001, 'Dashboard Customer D0', '0900400001', 0),
(4002, 'Dashboard Customer D1', '0900400002', 0),
(4003, 'Dashboard Customer D2', '0900400003', 0),
(4004, 'Dashboard Customer D3', '0900400004', 0),
(4005, 'Dashboard Customer D4', '0900400005', 0),
(4006, 'Dashboard Customer D5', '0900400006', 0),
(4007, 'Dashboard Customer D6', '0900400007', 0),
(4008, 'Dashboard Customer D7', '0900400008', 0);

-- Additional showtimes for today + next 7 days (supports now-showing list and occupancy KPI).
-- ID mapping by day offset n:
--   movie 1 -> 9001 + (n*3), movie 2 -> 9002 + (n*3), movie 3 -> 9003 + (n*3)
INSERT IGNORE INTO showtimes (id, movie_id, room_id, start_time, end_time, base_price) VALUES
(9001, 1, 1, TIMESTAMP(CURDATE(), '10:00:00'), TIMESTAMP(CURDATE(), '13:12:00'),  80000.00),
(9002, 2, 2, TIMESTAMP(CURDATE(), '14:00:00'), TIMESTAMP(CURDATE(), '17:00:00'), 120000.00),
(9003, 3, 3, TIMESTAMP(CURDATE(), '19:00:00'), TIMESTAMP(CURDATE(), '21:46:00'), 100000.00),
(9004, 1, 1, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '13:12:00'),  80000.00),
(9005, 2, 2, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '17:00:00'), 120000.00),
(9006, 3, 3, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '19:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '21:46:00'), 100000.00),
(9007, 1, 1, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '10:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '13:12:00'),  80000.00),
(9008, 2, 2, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '14:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '17:00:00'), 120000.00),
(9009, 3, 3, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '19:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '21:46:00'), 100000.00),
(9010, 1, 1, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '10:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '13:12:00'),  80000.00),
(9011, 2, 2, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '14:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '17:00:00'), 120000.00),
(9012, 3, 3, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '19:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '21:46:00'), 100000.00),
(9013, 1, 1, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '10:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '13:12:00'),  80000.00),
(9014, 2, 2, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '14:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '17:00:00'), 120000.00),
(9015, 3, 3, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '19:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '21:46:00'), 100000.00),
(9016, 1, 1, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '10:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '13:12:00'),  80000.00),
(9017, 2, 2, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '14:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '17:00:00'), 120000.00),
(9018, 3, 3, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '19:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '21:46:00'), 100000.00),
(9019, 1, 1, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '10:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '13:12:00'),  80000.00),
(9020, 2, 2, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '14:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '17:00:00'), 120000.00),
(9021, 3, 3, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '19:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '21:46:00'), 100000.00),
(9022, 1, 1, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 7 DAY), '10:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 7 DAY), '13:12:00'),  80000.00),
(9023, 2, 2, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 7 DAY), '14:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 7 DAY), '17:00:00'), 120000.00),
(9024, 3, 3, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 7 DAY), '19:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 7 DAY), '21:46:00'), 100000.00);

-- Paid invoices for each day from today to next 7 days.
INSERT IGNORE INTO invoices (id, user_id, customer_id, promotion_id, created_at, status, final_total) VALUES
(8001, 2, 4001, NULL, TIMESTAMP(CURDATE(), '12:15:00'), 'PAID', 820000.00),
(8002, 2, 4002, NULL, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '12:15:00'), 'PAID', 860000.00),
(8003, 2, 4003, NULL, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '12:15:00'), 'PAID', 900000.00),
(8004, 2, 4004, NULL, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '12:15:00'), 'PAID', 940000.00),
(8005, 2, 4005, NULL, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '12:15:00'), 'PAID', 980000.00),
(8006, 2, 4006, NULL, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '12:15:00'), 'PAID', 1020000.00),
(8007, 2, 4007, NULL, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '12:15:00'), 'PAID', 1060000.00),
(8008, 2, 4008, NULL, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 7 DAY), '12:15:00'), 'PAID', 1100000.00);

-- Tickets mapped to matching day showtimes (supports Top Movies + occupancy each day).
-- Room 1 seats: 1..100, Room 2 seats: 101..250, Room 3 seats: 251..310.
INSERT IGNORE INTO tickets (invoice_id, showtime_id, seat_id, final_price) VALUES
-- D0
(8001, 9001, 1, 80000.00), (8001, 9001, 2, 80000.00), (8001, 9001, 3, 80000.00), (8001, 9001, 4, 80000.00),
(8001, 9002, 101, 120000.00), (8001, 9002, 102, 120000.00), (8001, 9002, 103, 120000.00), (8001, 9003, 251, 100000.00),
-- D1
(8002, 9004, 5, 80000.00), (8002, 9004, 6, 80000.00), (8002, 9004, 7, 80000.00), (8002, 9004, 8, 80000.00),
(8002, 9005, 104, 120000.00), (8002, 9005, 105, 120000.00), (8002, 9005, 106, 120000.00), (8002, 9006, 252, 100000.00),
-- D2
(8003, 9007, 9, 80000.00), (8003, 9007, 10, 80000.00), (8003, 9007, 11, 80000.00), (8003, 9007, 12, 80000.00),
(8003, 9008, 107, 120000.00), (8003, 9008, 108, 120000.00), (8003, 9008, 109, 120000.00), (8003, 9009, 253, 100000.00),
-- D3
(8004, 9010, 13, 80000.00), (8004, 9010, 14, 80000.00), (8004, 9010, 15, 80000.00), (8004, 9010, 16, 80000.00),
(8004, 9011, 110, 120000.00), (8004, 9011, 111, 120000.00), (8004, 9011, 112, 120000.00), (8004, 9012, 254, 100000.00),
-- D4
(8005, 9013, 17, 80000.00), (8005, 9013, 18, 80000.00), (8005, 9013, 19, 80000.00), (8005, 9013, 20, 80000.00),
(8005, 9014, 113, 120000.00), (8005, 9014, 114, 120000.00), (8005, 9014, 115, 120000.00), (8005, 9015, 255, 100000.00),
-- D5
(8006, 9016, 21, 80000.00), (8006, 9016, 22, 80000.00), (8006, 9016, 23, 80000.00), (8006, 9016, 24, 80000.00),
(8006, 9017, 116, 120000.00), (8006, 9017, 117, 120000.00), (8006, 9017, 118, 120000.00), (8006, 9018, 256, 100000.00),
-- D6
(8007, 9019, 25, 80000.00), (8007, 9019, 26, 80000.00), (8007, 9019, 27, 80000.00), (8007, 9019, 28, 80000.00),
(8007, 9020, 119, 120000.00), (8007, 9020, 120, 120000.00), (8007, 9020, 121, 120000.00), (8007, 9021, 257, 100000.00),
-- D7
(8008, 9022, 29, 80000.00), (8008, 9022, 30, 80000.00), (8008, 9022, 31, 80000.00), (8008, 9022, 32, 80000.00),
(8008, 9023, 122, 120000.00), (8008, 9023, 123, 120000.00), (8008, 9023, 124, 120000.00), (8008, 9024, 258, 100000.00);

-- F&B revenue per day for pie chart.
INSERT IGNORE INTO invoice_items (invoice_id, product_id, quantity, unit_price) VALUES
(8001, 1, 2, 65000.00), (8001, 5, 1, 35000.00),
(8002, 2, 2, 65000.00), (8002, 6, 1, 35000.00),
(8003, 3, 2, 55000.00), (8003, 8, 2, 20000.00),
(8004, 1, 1, 65000.00), (8004, 5, 2, 35000.00),
(8005, 9, 1, 85000.00), (8005, 8, 1, 20000.00),
(8006, 10, 1, 105000.00), (8006, 6, 1, 35000.00),
(8007, 2, 1, 65000.00), (8007, 7, 2, 35000.00),
(8008, 1, 2, 65000.00), (8008, 5, 2, 35000.00);

-- 13. Backfill past 6 days to enrich 7-day revenue + pie chart window.
INSERT IGNORE INTO customers (id, full_name, phone, points) VALUES
(4101, 'Dashboard Customer M1', '0900410001', 0),
(4102, 'Dashboard Customer M2', '0900410002', 0),
(4103, 'Dashboard Customer M3', '0900410003', 0),
(4104, 'Dashboard Customer M4', '0900410004', 0),
(4105, 'Dashboard Customer M5', '0900410005', 0),
(4106, 'Dashboard Customer M6', '0900410006', 0);

INSERT IGNORE INTO showtimes (id, movie_id, room_id, start_time, end_time, base_price) VALUES
(8801, 1, 1, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '10:00:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '13:12:00'),  80000.00),
(8802, 2, 2, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '14:00:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '17:00:00'), 120000.00),
(8803, 3, 3, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '19:00:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '21:46:00'), 100000.00),
(8804, 1, 1, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '10:00:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '13:12:00'),  80000.00),
(8805, 2, 2, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '14:00:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '17:00:00'), 120000.00),
(8806, 3, 3, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '19:00:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '21:46:00'), 100000.00),
(8807, 1, 1, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 3 DAY), '10:00:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 3 DAY), '13:12:00'),  80000.00),
(8808, 2, 2, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 3 DAY), '14:00:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 3 DAY), '17:00:00'), 120000.00),
(8809, 3, 3, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 3 DAY), '19:00:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 3 DAY), '21:46:00'), 100000.00),
(8810, 1, 1, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 4 DAY), '10:00:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 4 DAY), '13:12:00'),  80000.00),
(8811, 2, 2, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 4 DAY), '14:00:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 4 DAY), '17:00:00'), 120000.00),
(8812, 3, 3, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 4 DAY), '19:00:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 4 DAY), '21:46:00'), 100000.00),
(8813, 1, 1, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '10:00:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '13:12:00'),  80000.00),
(8814, 2, 2, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '14:00:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '17:00:00'), 120000.00),
(8815, 3, 3, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '19:00:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '21:46:00'), 100000.00),
(8816, 1, 1, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 6 DAY), '10:00:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 6 DAY), '13:12:00'),  80000.00),
(8817, 2, 2, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 6 DAY), '14:00:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 6 DAY), '17:00:00'), 120000.00),
(8818, 3, 3, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 6 DAY), '19:00:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 6 DAY), '21:46:00'), 100000.00);

INSERT IGNORE INTO invoices (id, user_id, customer_id, promotion_id, created_at, status, final_total) VALUES
(8101, 2, 4101, NULL, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '12:10:00'), 'PAID', 780000.00),
(8102, 2, 4102, NULL, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '12:10:00'), 'PAID', 740000.00),
(8103, 2, 4103, NULL, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 3 DAY), '12:10:00'), 'PAID', 910000.00),
(8104, 2, 4104, NULL, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 4 DAY), '12:10:00'), 'PAID', 690000.00),
(8105, 2, 4105, NULL, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '12:10:00'), 'PAID', 830000.00),
(8106, 2, 4106, NULL, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 6 DAY), '12:10:00'), 'PAID', 970000.00);

INSERT IGNORE INTO tickets (invoice_id, showtime_id, seat_id, final_price) VALUES
(8101, 8801, 33, 80000.00), (8101, 8801, 34, 80000.00), (8101, 8802, 125, 120000.00), (8101, 8803, 259, 100000.00),
(8102, 8804, 35, 80000.00), (8102, 8804, 36, 80000.00), (8102, 8805, 126, 120000.00), (8102, 8806, 260, 100000.00),
(8103, 8807, 37, 80000.00), (8103, 8807, 38, 80000.00), (8103, 8808, 127, 120000.00), (8103, 8809, 261, 100000.00),
(8104, 8810, 39, 80000.00), (8104, 8810, 40, 80000.00), (8104, 8811, 128, 120000.00), (8104, 8812, 262, 100000.00),
(8105, 8813, 41, 80000.00), (8105, 8813, 42, 80000.00), (8105, 8814, 129, 120000.00), (8105, 8815, 263, 100000.00),
(8106, 8816, 43, 80000.00), (8106, 8816, 44, 80000.00), (8106, 8817, 130, 120000.00), (8106, 8818, 264, 100000.00);

INSERT IGNORE INTO invoice_items (invoice_id, product_id, quantity, unit_price) VALUES
(8101, 1, 1, 65000.00), (8101, 5, 2, 35000.00),
(8102, 2, 1, 65000.00), (8102, 8, 2, 20000.00),
(8103, 3, 2, 55000.00), (8103, 6, 1, 35000.00),
(8104, 9, 1, 85000.00), (8104, 8, 1, 20000.00),
(8105, 10, 1, 105000.00), (8105, 7, 1, 35000.00),
(8106, 1, 2, 65000.00), (8106, 5, 1, 35000.00);
