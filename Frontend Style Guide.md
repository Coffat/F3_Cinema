
# 🎨 CINEMA PRO MAX - FRONTEND

# STYLE GUIDE (2026)

## 1. Triết lý Thiết kế (Design Philosophy)

```
● Aesthetic: Modern Midnight (Dark Mode làm chủ đạo).
● Style: Flat Design 2.0 & Glassmorphism (Hiệu ứng kính mờ).
● UX Goal: "Zero Latency" - Phản hồi tức thì, mượt mà như Web App hiện đại.
```

## 2. Hệ thống Tokens (Design Tokens)

### 2.1. Bảng màu (Color Palette - Midnight Slate)

```
Tên biến (Key) Mã màu (Hex) Ứng dụng
bg-main #0F172A Nền toàn bộ ứng dụng
(Slate 900).
bg-surface #1E293B Nền Sidebar, Card, Table
(Slate 800).
accent-primary #6366F1 Nút bấm chính, Highlight
(Indigo 500).
accent-danger #F43F5E Ghế đã đặt, Nút xóa, Cảnh
báo (Rose 500).
text-primary #F8FAFC Chữ nội dung chính (Slate
50).
text-secondary #94A3B8 Chữ mô tả, chú thích (Slate
400).
```

### 2.2. Typography (Font & Size)

```
● Font: Inter hoặc Roboto (Sans-serif).
● Sizes:
○ Display: 32px (Bold) - Tên phim nổi bật.
○ Heading: 20px (Semi-bold) - Tiêu đề module.
○ Body: 14px (Medium) - Văn bản thông thường.
○ Caption: 12px (Regular) - Chú thích nhỏ.
```

### 2.3. Hiệu ứng (Effects)

```
● Border Radius: 16px cho tất cả Component (Card, Button, Dialog).
● Shadow: Soft Drop Shadow (Khung đổ bóng mịn, không viền cứng).
● Blur: 15px Backdrop Blur cho các cửa sổ Popup (Glassmorphism).
```

## 3. Quy chuẩn Component (UI Components)

### 3.1. Nút bấm (Buttons)

```
● Primary: Nền Indigo, chữ trắng, bo góc 16px. Hiệu ứng Hover: sáng lên 10%.
● Ghost: Chỉ có viền Indigo, nền trong suốt. Dùng cho các tác vụ phụ.
● Seat Icon: Sử dụng mã Unicode hoặc SVG hình ghế.
```

### 3.2. Bảng dữ liệu (Modern JTable)

```
● Padding: Cell Padding tối thiểu 12px (tạo không gian thở).
● Hover State: Đổi màu nền dòng khi di chuột qua (bg-surface sáng hơn).
● Renderer: Avatar phim (Poster) hiển thị dạng ảnh bo góc ngay trong cell.
```

### 3.3. Card Phim (Movie Card)

```
● Poster chiếm 80% diện tích.
● Phần dưới là Tên phim và Tag thể loại (Badge).
● Hiệu ứng: Scale nhẹ 1.05x khi hover.
```

## 4. Nguyên tắc trải nghiệm (UX Principles)

### 4.1. Hiệu suất & Phản hồi

```
● Skeleton Loading: Hiển thị khung giả lập khi đang truy vấn Hibernate.
● Async Processing: Mọi thao tác I/O (Database, File) phải chạy trên SwingWorker hoặc
CompletableFuture, không làm treo UI Thread.
● Debounce Search: Chỉ thực hiện tìm kiếm sau khi người dùng ngừng gõ 300ms.
```

### 4.2. Tương tác bàn phím (Keyboard-First)

```
● ESC: Đóng mọi Dialog/Popup.
● Enter: Xác nhận thanh toán/Tìm kiếm.
● Ctrl + F: Nhảy vào ô tìm kiếm nhanh.
● Phím mũi tên: Di chuyển chọn ghế trên Seat Map.
```

## 5. Kỹ thuật triển khai (Implementation Details)

### 5.1. FlatLaf Configuration (JSON)

Dự án sẽ sử dụng file cấu hình Midnight.properties để ép FlatLaf tuân thủ bảng màu:

# Custom Midnight Theme

@accentColor=#6366F
Button.arc=
Component.arc=
TextComponent.arc=
Table.showHorizontalLines=true
Table.showVerticalLines=false
Table.selectionBackground=#

### 5.2. Asset Management

```
● Icons: Chỉ sử dụng định dạng SVG (thông qua FlatLaf Extras) để đảm bảo độ nét trên màn
hình Retina/4K.
● Images: Poster phim được cache vào bộ nhớ đệm (In-memory cache) sau khi tải từ
URL/Database để tránh giật lag khi cuộn.
```

## 6. Quy tắc đặt tên (Naming Conventions)

```
● UI Classes: [ModuleName]Panel.java, [ModuleName]Dialog.java.
● UI Components: btnConfirm, lblTitle, txtSearch, tblMovies.
● Events: onBookSeat(), handlePayment(), filterMovies().
```
