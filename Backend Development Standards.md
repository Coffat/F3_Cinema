
# 🛠 F3 CINEMA - BACKEND SKILL

# STANDARDS (2026)

_Version: 2.1 (Master-Detail Warehouse & Design Patterns Integration)_

## 1. Core Technologies (Latest 2026)

```
● Language: Java 21 (LTS) - Sử dụng Virtual Threads cho I/O tasks.
● ORM: Hibernate 6.6+ (Jakarta Persistence 3.1).
● Database: MySQL 8.4 (LTS).
● Security: BCrypt (cost=12) cho mật khẩu.
● Dependencies: Lombok, HikariCP, Log4j2, JFreeChart, OpenPDF.
```

## 2. Kiến trúc 3-Tier & Master-Detail Pattern

Dự án tuân thủ mô hình 3 lớp chuẩn, đặc biệt chú trọng vào quan hệ Master-Detail để giải quyết
logic thực tế:

### 2.1. Phân hệ Bán hàng (Sales)

```
● Master: Invoice (Thông tin chung giao dịch).
● Detail 1: Ticket (Danh sách vé xem phim).
● Detail 2: InvoiceItem (Danh sách bắp nước).
● Logic: Một giao dịch thanh toán phải tạo đồng thời Invoice và các Detail tương ứng trong
một Transaction.
```

### 2.2. Phân hệ Kho hàng (Warehouse - UPDATED)

```
● Master: StockReceipt (Thông tin phiếu nhập: Ngày, Nhà cung cấp, Tổng tiền nhập).
● Detail: StockReceiptItem (Chi tiết: Sản phẩm nào, số lượng bao nhiêu, giá nhập).
● Logic: * StockReceiptItem giữ quan hệ @ManyToOne với Product.
○ Sau khi StockReceipt được lưu (Status: COMPLETED), hệ thống phải tự động cập nhật
Inventory.currentQuantity.
```

## 3. Design Patterns ứng dụng (Quy Tắc Bắt Buộc)

> **QUY TẮC CỐT LÕI (MANDATORY YET PRAGMATIC):**
> Việc áp dụng Design Pattern trong dự án là **bắt buộc khi cần thiết**, nhằm giải quyết đúng điểm đau (pain points) về thiết kế, nhưng **tuyệt đối không lạm dụng (Over-engineering)**.
>
> - **Chỉ dùng khi cần:** Nếu nghiệp vụ có khả năng mở rộng (như nạp nhiều loại thanh toán) thì dùng *Strategy*. Nếu luồng logic phức tạp từ nhiều nguồn, dùng *Facade*. Nếu chỉ có một logic đơn giản không bao giờ đổi, hãy để Code thuần (KISS).
> - **Sử dụng đúng mục đích:** Không "gọt chân cho vừa giày", tránh việc viết ra hàng loạt Interface/Factory/Builder trong khi Entity chỉ có 2-3 fields. Tối ưu hoá nhưng phải thực tế lập trình.

### 3.1. Creational Patterns

```
● Singleton: Áp dụng cho HibernateUtil để quản lý SessionFactory.
● Builder Pattern: Sử dụng @Builder của Lombok cho các thực thể có nhiều thuộc tính như
Movie, Showtime, Customer.
● Factory Method: Sử dụng DocumentFactory để tạo trình xuất file (PDF Ticket, Excel
Report).
```

### 3.2. Structural Patterns

```
● Repository Pattern: Tách biệt logic truy vấn dữ liệu khỏi Service. Mỗi Entity chính có một
Repository tương ứng.
● DTO Pattern: Sử dụng Java Records để chuyển dữ liệu từ Service sang UI. Tuyệt đối
không trả về JPA Entity cho tầng UI.
● Facade Pattern (NEW): Áp dụng cho các giao diện giao dịch phức tạp (như POS Bán hàng) để gom nhóm logic, giảm việc UI phải gọi trực tiếp hàng loạt Service riêng biệt (ví dụ: `TicketingFacade`).
```

### 3.3. Behavioral Patterns

```
● Strategy Pattern (IMPLEMENTED): Áp dụng cho PaymentService. Đã triển khai `PaymentStrategy` interface (CashPaymentStrategy, MomoPaymentStrategy) và `PaymentContext`.
● Template Method (IMPLEMENTED): Đã triển khai `InvoiceProcessor` định nghĩa bộ workflow xử lý Hóa đơn chuẩn quy trình (Validate -> Calculate -> Save -> Print).
```

## 4. Quy chuẩn Code Java 21 & Hibernate 6

### 4.1. Modern Java Features

```
● Virtual Threads: Sử dụng Executors.newVirtualThreadPerTaskExecutor() cho các tác vụ
xuất PDF hoặc thống kê nặng để tránh block UI.
● Switch Expressions: Dùng để xử lý phân loại ghế (VIP/Normal) hoặc loại thanh toán.
● Pattern Matching for instanceof: Giúp code clear hơn khi xử lý các loại Product.
```

### 4.2. Hibernate Best Practices

```
● Fetch Type: Luôn sử dụng FetchType.LAZY cho các tập hợp (@OneToMany).
● Orphan Removal: Thiết lập orphanRemoval = true cho các quan hệ Master-Detail (xóa
Invoice thì xóa luôn Tickets).
● Batch Processing: Cấu hình hibernate.jdbc.batch_size = 50 cho các nghiệp vụ nhập kho
số lượng lớn.
```

## 5. Logic Nghiệp vụ Cốt lõi (F3 Cinema Core)

```
● Showtime Conflict Validation: * Logic: startTime < existing.endTime AND endTime >
existing.startTime cho cùng một Room.
● Inventory Synchronization: * Mọi thay đổi về tồn kho phải thông qua InventoryService để
đảm bảo tính nhất quán (Thread-safe).
● Loyalty Points Logic: * 100,000 VNĐ = 10 điểm. 10 điểm = 5,000 VNĐ giảm giá (có thể
cấu hình).
```

## 6. Error Handling & Logging

```
● Sử dụng Custom Runtime Exceptions: CinemaException, InventoryException,
PaymentException.
● Log4j2: Ghi log theo cấp độ (INFO cho giao dịch, ERROR cho hệ thống).
```
