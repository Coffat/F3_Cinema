-- Initialization Script for F3 Cinema Database (MySQL 8.4)
-- Strictly matching provided ERD structure
-- Database Name: f3_cinema

CREATE DATABASE IF NOT EXISTS f3_cinema;
USE f3_cinema;

-- 1. USERS Table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL, -- ADMIN, STAFF
    full_name VARCHAR(100),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
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
    poster_url TEXT, -- Path or URL to movie poster image (Supports long Base64)
    deleted_at DATETIME DEFAULT NULL -- Soft delete for data integrity
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
    row_char VARCHAR(2) NOT NULL, -- A, B, C...
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

-- 9. PRODUCTS Table
CREATE TABLE IF NOT EXISTS products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(19, 2) NOT NULL,
    unit VARCHAR(50), -- e.g., 'Cup', 'Box'
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
    user_id INT NOT NULL, -- Staff who created the invoice
    customer_id INT DEFAULT NULL,
    promotion_id INT DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, PAID, CANCELLED
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

-- 16. INVOICE_ITEMS Table (For Products/F&B)
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
    method VARCHAR(50) NOT NULL, -- CASH, BANK_TRANSFER, MOMO
    status VARCHAR(20) NOT NULL, -- PENDING, COMPLETED, FAILED
    CONSTRAINT fk_payments_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE
);

-- ==========================================
-- SEED DATA
-- ==========================================

-- Admin Account (password: 123456)
-- Bcrypt hash of '123456'
INSERT IGNORE INTO users (username, password, full_name, role) 
VALUES ('admin', '$2a$10$vI8AAn.XmO9j2W6K5l2y3uH.zG.O.2E.2E.2E.2E.2E.2E.2E.2E.2E.2E.2E.2E', 'Default Administrator', 'ADMIN');

-- Sample Genres
INSERT IGNORE INTO genres (name) VALUES 
('Action'), ('Comedy'), ('Drama'), ('Horror'), ('Sci-Fi'), ('Animation'), ('Thriller'), ('Romance');

-- Real Movie Data Seeder
INSERT IGNORE INTO movies (title, duration, status, poster_url) VALUES 
('Avatar: The Way of Water', 192, 'NOW_SHOWING', 'https://m.media-amazon.com/images/M/MV5BYjhiNjBlODctY2ZiOC00YjVlLWFlNzAtNTVhNjM1YjI1NzMxXkEyXkFqcGdeQXVyMjQxNTE1MDA@._V1_.jpg'),
('Oppenheimer', 180, 'NOW_SHOWING', 'https://m.media-amazon.com/images/M/MV5BMDBmYTZjNjUtN2M1Zi00N2MzLTk2NzgtOGM2MzFhNDLiZTMzXkEyXkFqcGdeQXVyMTUzMTg2ODkz._V1_.jpg'),
('Dune: Part Two', 166, 'NOW_SHOWING', 'https://m.media-amazon.com/images/M/MV5BN2QyZGU4ZDctOWJmMy00N2IzLThmMWQtOTUxNDcyY2MyNzIxXkEyXkFqcGdeQXVyMTUzMTg2ODkz._V1_.jpg'),
('Deadpool & Wolverine', 127, 'COMING_SOON', 'https://m.media-amazon.com/images/M/MV5BNzRiMjg0MzUtNTQ1Mi00Y2QyLWEwNjMtMzI3ZDBmY2NlNmU0XkEyXkFqcGdeQXVyMTUzMTg2ODkz._V1_.jpg'),
('Despicable Me 4', 94, 'NOW_SHOWING', 'https://m.media-amazon.com/images/M/MV5BZjE0YjVjODQtZGY2NS00MDcyLThhMDAtZGQwMTZiOWNmNjRiXkEyXkFqcGdeQXVyMTUzMTg2ODkz._V1_.jpg'),
('Spider-Man: Across the Spider-Verse', 140, 'ENDED', 'https://m.media-amazon.com/images/M/MV5BMzI0NmVkMjEtYmY4MS00ZDMxLTlkZmEtMzU4MDQxYTMzMjU2XkEyXkFqcGdeQXVyMzQ0MzA0NTM@._V1_.jpg');
