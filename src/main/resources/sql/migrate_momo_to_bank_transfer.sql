-- Chạy thủ công nếu DB cũ còn phương thức MOMO.
UPDATE payments SET method = 'BANK_TRANSFER' WHERE method = 'MOMO';
