# F3 Cinema Management System

## 1. Tổng Quan Dự Án

**F3 Cinema** là hệ thống quản lý rạp chiếu phim desktop được phát triển bằng Java Swing. Dự án cung cấp đầy đủ các chức năng quản lý rạp chiếu phim bao gồm quản lý phim, phòng chiếu, nhân viên, đặt vé, bán đồ ăn/nước uống và thống kê doanh thu.

## 2. Công Nghệ Sử Dụng

### Backend
- **Java**: Phiên bản 21
- **Hibernate**: 6.6.1.Final (ORM)
- **MySQL**: 9.1.0 (Database)
- **Lombok**: 1.18.34 (Giảm boilerplate code)

### Frontend/UI
- **Swing**: UI Framework
- **FlatLaf**: 3.5.1 (Modern UI theme - Modern Midnight)

### Các Thư Viện Khác
- **BCrypt**: 0.10.2 (Mã hóa mật khẩu)
- **JFreeChart**: 1.5.5 (Biểu đồ thống kê)
- **OpenPDF**: 2.0.3 (Tạo file PDF)
- **Log4j2**: 2.24.1 (Logging)

## 3. Cấu Trúc Dự Án

```
f3-cinema-management/
├── src/main/java/com/f3cinema/app/
│   ├── config/           # Cấu hình (Hibernate, Theme)
│   ├── controller/      # Controller (xử lý logic UI)
│   ├── dto/             # Data Transfer Objects
│   ├── entity/          # JPA Entities
│   │   └── enums/       # Các enum types
│   ├── exception/       # Custom exceptions
│   ├── repository/     # Data Access Layer
│   ├── service/         # Business Logic Layer
│   │   ├── cart/        # Cart management (Command pattern)
│   │   ├── payment/     # Payment (Strategy pattern)
│   │   └── impl/        # Service implementations
│   ├── ui/              # UI Layer (Swing)
│   │   ├── admin/       # Admin frames & panels
│   │   ├── dashboard/   # Dashboard components
│   │   ├── staff/       # Staff interface
│   │   └── components/  # Reusable UI components
│   └── util/            # Utilities
├── src/main/resources/
│   ├── hibernate.cfg.xml # Hibernate configuration
│   └── sql/init.sql     # Database initialization
├── pom.xml              # Maven dependencies
├── docker-compose.yml   # MySQL container
└── Dockerfile.mysql    # MySQL Dockerfile
```

## 4. Kiến Trúc Hệ Thống

### Layers
1. **Entity Layer**: JPA entities với Hibernate annotations
2. **Repository Layer**: Data access với custom implementations
3. **Service Layer**: Business logic, sử dụng Command & Strategy patterns
4. **UI Layer**: Swing JFrame/JPanel components

### Design Patterns
- **Command Pattern**: Cart management (`AddToCartCommand`, `RemoveFromCartCommand`, `UpdateQuantityCommand`, `ClearCartCommand`)
- **Strategy Pattern**: Payment methods (`CashPaymentStrategy`, `MomoPaymentStrategy`)
- **Observer Pattern**: `CartObserver` để notify UI khi cart thay đổi

## 5. Database

### Cấu Hình Docker
- **Container**: f3_cinema_db
- **Port**: 3307 (mapping từ 3306)
- **Database**: f3_cinema
- **Credentials**:
  - Root: root / 123456
  - User: f3_admin / f3_password

### Các Entity Chính

| Entity | Mô Tả |
|--------|-------|
| User | Người dùng hệ thống (Admin/Staff) |
| Movie | Phim (title, duration, status, poster) |
| Genre | Thể loại phim |
| Room | Phòng chiếu (name, type) |
| Seat | Ghế ngồi (row, number, type) |
| Showtime | Suất chiếu (movie, room, time) |
| Ticket | Vé đặt (showtime, seat, price) |
| Customer | Khách hàng |
| Product | Sản phẩm (snacks, drinks) |
| Inventory | Tồn kho sản phẩm |
| StockReceipt | Phiếu nhập kho |
| Invoice | Hóa đơn |
| Payment | Thanh toán (method, status) |
| Promotion | Khuyến mãi |

### Enums
- `UserRole`: ADMIN, STAFF
- `MovieStatus`: COMING_SOON, NOW_SHOWING, ENDED
- `RoomType`: STANDARD, VIP, IMAX
- `SeatType`: STANDARD, VIP, COUPLE
- `PaymentMethod`: CASH, MOMO
- `PaymentStatus`: PENDING, COMPLETED, FAILED
- `InvoiceStatus`: UNPAID, PAID, CANCELLED

## 6. UI Structure

### Login Screen
- `LoginFrame`: Màn hình đăng nhập

### Admin Interface
- `AdminMainFrame`: Frame chính cho admin
- `NavbarPanel`: Thanh điều hướng
- `SidebarController`: Điều khiển sidebar
- Các panels:
  - `DashboardPanel`: Tổng quan
  - `MoviePanel`: Quản lý phim
  - `RoomPanel`: Quản lý phòng chiếu
  - `RoomSeatPanel`: Sơ đồ ghế
  - `StaffPanel`: Quản lý nhân viên
  - `PromotionPanel`: Quản lý khuyến mãi
  - `WarehousePanel`: Quản lý kho
  - `StatisticsPanel`: Thống kê

### Staff Interface
- `StaffMainFrame`: Frame chính cho nhân viên
- `StaffNavbarPanel`: Thanh điều hướng
- Các panels:
  - `TicketingPanel`: Đặt vé
  - `SeatSelectionView`: Chọn ghế
  - `ShowtimeListView`: Danh sách suất chiếu
  - `SearchShowtimePanel`: Tìm kiếm suất chiếu
  - `SnacksPanel`: Bán đồ ăn/nước
  - `CustomerPanel`: Quản lý khách hàng
  - `TransactionHistoryPanel`: Lịch sử giao dịch

## 7. Chạy Dự Án

### Yêu Cầu
- Java 21
- Maven 3.9+
- Docker (cho MySQL)

### Khởi Động Database
```bash
cd f3-cinema-management
docker-compose up -d
```

### Chạy Ứng Dụng
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="com.f3cinema.app.App"
```

### Build JAR
```bash
mvn package
```

## 8. Tài Khoản Mặc Định

| Role | Username | Password |
|------|----------|----------|
| Admin | admin | admin123 |
| Staff | staff | staff123 |

*(Các tài khoản được tạo trong init.sql)*

## 9. Các Tính Năng Chính

### Admin
- Quản lý phim (thêm, sửa, xóa, cập nhật trạng thái)
- Quản lý phòng chiếu và ghế ngồi
- Quản lý nhân viên
- Quản lý khuyến mãi
- Quản lý kho và nhập hàng
- Xem thống kê doanh thu

### Staff
- Tìm kiếm và xem suất chiếu
- Đặt vé và chọn ghế
- Bán đồ ăn/nước (giỏ hàng)
- Quản lý khách hàng
- Xem lịch sử giao dịch
- Thanh toán (tiền mặt, MoMo)

## 10. Ghi Chú

- Giao diện sử dụng FlatLaf với theme "Modern Midnight"
- Database được khởi tạo tự động qua `init.sql` khi Docker container khởi động
- Sử dụng Hibernate với cấu hình XML (`hibernate.cfg.xml`)
- Mật khẩu được mã hóa bằng BCrypt
- Log4j2 được sử dụng cho logging

## 11. File Cấu Hình Quan Trọng

- `pom.xml`: Maven dependencies
- `hibernate.cfg.xml`: Cấu hình Hibernate
- `docker-compose.yml`: Cấu hình MySQL container
- `ThemeConfig.java`: Cấu hình UI theme
- `HibernateUtil.java`: Singleton SessionFactory
