<div align="center">

[![Header](https://capsule-render.vercel.app/api?type=waving&color=0f172a&height=280&section=header&text=F3%20Cinema&fontSize=80&fontAlignY=40&fontColor=ffffff&animation=twinkling&desc=Enterprise%20Cinema%20Management%20System&descAlignY=65&descAlign=50)](https://github.com/f3-cinema)

<br/>

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=java)](https://jdk.java.net/21/)
[![Hibernate](https://img.shields.io/badge/Hibernate-6.6.1.Final-59666C?style=for-the-badge&logo=hibernate)](https://hibernate.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.4-4479A1?style=for-the-badge&logo=mysql)](https://www.mysql.com/)
[![FlatLaf](https://img.shields.io/badge/FlatLaf-3.5.1-7F00FF?style=for-the-badge&logo=flat)](https://www.formdev.com/flatlaf/)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker)](https://www.docker.com/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-C71A36?style=for-the-badge&logo=apachemaven)](https://maven.apache.org/)

[![Typing SVG](https://readme-typing-svg.demolab.com?font=Fira+Code&weight=700&size=18&pause=800&color=6366F1&center=true&vCenter=true&width=700&height=45&lines=Enterprise+Java+Swing+Application;MVC+Architecture+with+Modern+Design+Patterns;Comprehensive+Cinema+POS+and+Admin+System)](https://git.io/typing-svg)

<p align="center" style="margin-top: 20px;">
  <b>🎬 Next-Generation Cinema Management Platform</b><br/>
  <sub>Built with Java 21 LTS • Hibernate 6.6.1 • MySQL 8.4 • FlatLaf Modern UI</sub>
</p>

</div>

---

<details align="center">
  <summary style="cursor: pointer; color: #6366F1; font-weight: 600; padding: 12px; border-radius: 8px; background: rgba(99, 102, 241, 0.1); display: inline-block;">
    📑 Table of Contents
  </summary>
  <ul style="list-style: none; padding: 20px; text-align: left; display: inline-block;">
    <li><a href="#about">About</a></li>
    <li><a href="#features">Key Features</a></li>
    <li><a href="#tech-stack">Tech Stack</a></li>
    <li><a href="#architecture">Architecture & Patterns</a></li>
    <li><a href="#database">Database Schema</a></li>
    <li><a href="#project-structure">Project Structure</a></li>
    <li><a href="#installation">Installation & Setup</a></li>
    <li><a href="#quick-start">Quick Start</a></li>
    <li><a href="#troubleshooting">Troubleshooting</a></li>
    <li><a href="#contributors">Contributors</a></li>
  </ul>
</details>

---

## About

**F3 Cinema Management** is a comprehensive enterprise desktop application designed for cinema chain operations. Built with Java 21 LTS and modern Swing UI via FlatLaf, it delivers a polished Midnight-themed experience with real-time seat mapping, integrated POS, inventory management, customer loyalty system, and powerful analytics dashboards.

The system is architected around **MVC pattern** with **Hibernate ORM** for clean database abstraction, featuring industry-standard design patterns including **Command**, **Observer**, **Strategy** (Payment & Discount), and **Factory** for maintainable, scalable code.

---

## Key Features

<div align="center">

| # | Feature | Description |
|---|---------|-------------|
| **01** | 🎬 **Movie & Showtime Management** | Full movie lifecycle (Coming Soon → Now Showing → Ended), smart scheduling with conflict detection, genre management |
| **02** | 💺 **Real-Time Seat Mapping** | Interactive seat matrix UI with live availability, VIP/Sweetbox seat configuration per theater |
| **03** | 🛒 **POS & Integrated Cart** | Seamless flow: Select Seats → Add Snacks → Apply Voucher → Payment in single screen |
| **04** | 📄 **Invoice Generation** | Auto-generate professional PDF invoices via OpenPDF library with detailed breakdown |
| **05** | 📦 **Inventory Management** | Stock tracking for concessions (popcorn, beverages), auto-alert for low inventory, import receipts |
| **06** | 🎟️ **Voucher System** | 4 discount types: Percentage, Fixed Amount, Buy X Get Y, Combo Discount with usage limits |
| **07** | 👥 **Customer & Loyalty** | Walk-in + registered customers, loyalty points accumulation, tier-based benefits |
| **08** | 🏠 **Room & Seat Configuration** | Theater layouts (2D/IMAX), dynamic seat types (NORMAL/VIP/SWEETBOX) |
| **09** | 📊 **Dashboard & Statistics** | JFreeChart-powered financial reports, revenue analytics, top movies, inventory alerts |
| **10** | 📜 **Transaction History** | Search & filter transactions, export records, payment status tracking |

</div>

---

## Tech Stack

<div align="center">

| Technology | Description |
|------------|-------------|
| ![Java](https://img.shields.io/badge/Java-21_LTS-ED8B00?style=flat&logo=java) | Core language |
| ![Hibernate](https://img.shields.io/badge/Hibernate-6.6.1.Final-59666C?style=flat&logo=hibernate) | ORM & persistence |
| ![MySQL](https://img.shields.io/badge/MySQL-8.4-4479A1?style=flat&logo=mysql) | Primary database |
| ![FlatLaf](https://img.shields.io/badge/FlatLaf-3.5.1-7F00FF?style=flat&logo=flat) | Modern UI theming |
| ![Maven](https://img.shields.io/badge/Maven-3.9+-C71A36?style=flat&logo=apachemaven) | Build automation |
| ![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker) | Container deployment |

### Core Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| HikariCP | 5.1.0 | High-performance connection pooling |
| Lombok | 1.18.34 | Code generation (getters/setters/builders) |
| BCrypt | 0.10.2 | Secure password hashing |
| JFreeChart | 1.5.5 | Data visualization & charts |
| OpenPDF | 2.0.3 | PDF document generation |
| Log4j2 | 2.24.1 | Enterprise logging framework |

</div>

---

## Architecture & Design Patterns

### System Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                        PRESENTATION LAYER                          │
│  ┌─────────────────┐              ┌─────────────────────────────┐  │
│  │  Admin Portal   │              │      Staff POS Terminal      │  │
│  │  (Dashboard)     │              │   (Seat Map + Cart + POS)   │  │
│  └────────┬────────┘              └──────────────┬──────────────┘  │
└───────────┼───────────────────────────────────────┼──────────────────┘
            │                                       │
            ▼                                       ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        BUSINESS LAYER (Service)                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐              │
│  │   Command    │  │   Observer   │  │   Strategy   │              │
│  │   Pattern    │  │   Pattern    │  │   Pattern    │              │
│  │  (Cart Ops)  │  │  (Cart UI)   │  │ (Payment)    │              │
│  └──────────────┘  └──────────────┘  └──────────────┘              │
│                                                                    │
│  ┌──────────────┐  ┌──────────────┐                                │
│  │   Strategy   │  │   Factory    │                                │
│  │   Pattern    │  │   Pattern    │                                │
│  │ (Discount)   │  │ (Strategy)   │                                │
│  └──────────────┘  └──────────────┘                                │
└─────────────────────────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      DATA ACCESS LAYER (Repository)                 │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │              Hibernate ORM + HikariCP Connection Pool         │   │
│  └──────────────────────────────────────────────────────────────┘   │
            │
            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         DATABASE (MySQL 8.4)                      │
└─────────────────────────────────────────────────────────────────────┘
```

### 1. Command Pattern — Cart Operations

```mermaid
sequenceDiagram
    autonumber
    participant Staff as Staff User
    participant UI as Cart UI Layer
    participant Command as CartCommand
    participant Service as CartService
    participant Observer as CartObserver

    Staff->>UI: Add item / Update quantity
    UI->>Command: Execute(command)
    activate Command
    Command->>Service: Process cart operation
    Service-->>Observer: Emit UPDATE event
    Observer->>UI: Trigger re-render
    UI->>Staff: Display real-time total
    deactivate Command
```

**Implementation:**
- `AddToCartCommand` — Add product/seat to cart
- `RemoveFromCartCommand` — Remove item from cart
- `UpdateQuantityCommand` — Change item quantity
- `ClearCartCommand` — Empty entire cart

### 2. Observer Pattern — Real-Time Cart Updates

```mermaid
classDiagram
    class CartManager {
        -List~CartObserver~ observers
        -List~CartItem~ items
        +addItem(item)
        +removeItem(item)
        +attach(observer)
        +detach(observer)
        +notifyAll()
    }
    class CartObserver {
        <<interface>>
        +onCartUpdated()
    }
    class SeatMapPanel {
        +onCartUpdated()
    }
    class CartSummaryPanel {
        +onCartUpdated()
    }
    CartManager o-- CartObserver : notifies
    CartObserver <|.. SeatMapPanel
    CartObserver <|.. CartSummaryPanel
```

### 3. Strategy Pattern — Payment Gateway

```mermaid
classDiagram
    class PaymentContext {
        -PaymentStrategy strategy
        +setStrategy(strategy)
        +executePayment(amount)
    }
    class PaymentStrategy {
        <<interface>>
        +processPayment(Invoice): void
    }
    class CashPaymentStrategy {
        +processPayment(Invoice): void
    }
    PaymentContext --> PaymentStrategy
    PaymentStrategy <|.. CashPaymentStrategy
    note for PaymentStrategy "Thanh toán chuyển khoản bằng MoMo Test: package service.payment + dialog QR trong app."
```

### 4. Strategy + Factory Pattern — Discount System

```mermaid
classDiagram
    class DiscountStrategyFactory {
        +getStrategy(voucherType): DiscountStrategy
    }
    class DiscountStrategy {
        <<interface>>
        +calculate(originalAmount, voucher): BigDecimal
    }
    class PercentageDiscountStrategy {
        +calculate(originalAmount, voucher): BigDecimal
    }
    class FixedAmountDiscountStrategy {
        +calculate(originalAmount, voucher): BigDecimal
    }
    class BuyXGetYDiscountStrategy {
        +calculate(originalAmount, voucher): BigDecimal
    }
    class ComboDiscountStrategy {
        +calculate(originalAmount, voucher): BigDecimal
    }
    DiscountStrategyFactory ..> DiscountStrategy
    DiscountStrategy <|.. PercentageDiscountStrategy
    DiscountStrategy <|.. FixedAmountDiscountStrategy
    DiscountStrategy <|.. BuyXGetYDiscountStrategy
    DiscountStrategy <|.. ComboDiscountStrategy
```

---

## Database Schema

| Table | Description | Key Columns |
|-------|-------------|-------------|
| `users` | Admin/Staff accounts | id, username, password_hash, role, full_name |
| `customers` | Walk-in + registered customers | id, full_name, phone, points |
| `movies` | Movie info | id, title, duration, status, poster_url |
| `genres` | Movie genres | id, name |
| `movie_genres` | Movie-Genre mapping | movie_id, genre_id |
| `rooms` | Theater rooms | id, name, type (2D/IMAX) |
| `seats` | Seats per room | id, room_id, row_char, number, type (NORMAL/VIP/SWEETBOX) |
| `showtimes` | Show schedules | id, movie_id, room_id, start_time, end_time, base_price |
| `products` | Snacks/drinks | id, name, price, unit, image_url |
| `inventories` | Stock tracking | product_id, current_quantity, min_threshold |
| `stock_receipts` | Import orders | id, receipt_date, supplier, total_import_cost |
| `stock_receipt_items` | Import items | id, receipt_id, product_id, quantity, import_price |
| `promotions` | Discount campaigns | id, code, discount_percent |
| `vouchers` | Voucher codes | id, code, voucher_type, discount_percent/amount, usage_limit |
| `invoices` | Sales transactions | id, user_id, customer_id, promotion_id, status, final_total |
| `invoice_items` | Product line items | id, invoice_id, product_id, quantity, unit_price |
| `payments` | Payment records | id, invoice_id, amount, method, status, transaction_id (mã tham chiếu chuyển khoản) |

---

## Project Structure

```
f3-cinema-management/
├── src/main/
│   ├── java/com/f3cinema/app/
│   │   ├── config/
│   │   │   ├── HibernateUtil.java        # JPA/Hibernate configuration
│   │   │   └── ThemeConfig.java           # FlatLaf theming setup
│   │   ├── controller/                   # UI controllers
│   │   ├── dto/                          # Data Transfer Objects (Records)
│   │   │   ├── dashboard/                # Dashboard-specific DTOs
│   │   │   ├── transaction/             # Transaction DTOs
│   │   │   └── customer/                # Customer DTOs
│   │   ├── entity/                      # JPA Entities
│   │   │   └── enums/                   # Enum types (MovieStatus, UserRole, etc.)
│   │   ├── exception/                   # Custom exceptions
│   │   │   ├── CinemaException.java
│   │   │   └── AuthenticationException.java
│   │   ├── repository/                  # Data Access Layer
│   │   ├── service/                     # Business Logic Layer
│   │   │   ├── cart/                    # Cart (Command + Observer Patterns)
│   │   │   │   ├── CartManager.java
│   │   │   │   ├── CartObserver.java
│   │   │   │   └── command/
│   │   │   │       ├── CartCommand.java
│   │   │   │       ├── AddToCartCommand.java
│   │   │   │       ├── RemoveFromCartCommand.java
│   │   │   │       ├── UpdateQuantityCommand.java
│   │   │   │       └── ClearCartCommand.java
│   │   │   ├── payment/                 # Payment (Strategy Pattern)
│   │   │   │   ├── PaymentStrategy.java
│   │   │   │   ├── PaymentContext.java
│   │   │   │   ├── CashPaymentStrategy.java
│   │   │   │   └── MomoPaymentService.java
│   │   │   ├── discount/                # Discount (Strategy + Factory Patterns)
│   │   │   │   ├── DiscountStrategy.java
│   │   │   │   ├── DiscountStrategyFactory.java
│   │   │   │   ├── PercentageDiscountStrategy.java
│   │   │   │   ├── FixedAmountDiscountStrategy.java
│   │   │   │   ├── BuyXGetYDiscountStrategy.java
│   │   │   │   └── ComboDiscountStrategy.java
│   │   │   └── impl/                    # Service implementations
│   │   ├── ui/                          # Swing UI Layer
│   │   │   ├── admin/                   # Admin interface
│   │   │   ├── dashboard/               # Shared dashboard components
│   │   │   ├── staff/                   # Staff POS interface
│   │   │   └── components/              # Reusable UI components
│   │   └── util/                        # Utilities & helpers
│   └── resources/
│       ├── sql/
│       │   └── init.sql                 # Database initialization (17 tables)
│       └── log4j2.xml                   # Logging configuration
├── target/                              # Build output
├── docker-compose.yml                   # MySQL container setup
├── pom.xml                             # Maven configuration
└── README.md                           # This file
```

---

## Installation & Setup

### Prerequisites

| Requirement | Version | Download |
|-------------|---------|----------|
| **Java JDK** | 21 LTS | [Oracle JDK](https://jdk.java.net/21/) or [Amazon Corretto](https://aws.amazon.com/corretto/) |
| **Maven** | 3.9+ | [Maven.apache.org](https://maven.apache.org/download.cgi) |
| **Docker** | Latest | [Docker Desktop](https://www.docker.com/products/docker-desktop) |

### Step 1: Start Database (Docker)

```bash
# Start MySQL 8.4 container with auto-initialization
docker-compose up -d

# Verify container is running
docker ps | grep f3_cinema
```

### Step 2: Build Project

```bash
# Clean and compile the project
mvn clean compile

# (Optional) Package as JAR
mvn package
```

### Step 3: Run Application

```bash
# Execute main class
mvn exec:java -Dexec.mainClass="com.f3cinema.app.App"
```

### Chuyển khoản MoMo Test

Cấu hình tài khoản nhận tiền trong `src/main/resources/momo.properties` (hoặc dùng `momo.properties.example`). Ở bước thanh toán, hệ thống hiển thị QR MoMo Test ngay trong app và thu ngân xác nhận "Đã nhận tiền" để chốt đơn.

---

## Quick Start

### Default Login Credentials

| Role | Username | Password | Access Level |
|------|----------|----------|--------------|
| **Admin** | `admin` | `1` | Full system access (Dashboard, Movies, Rooms, Inventory, Reports, Vouchers) |
| **Staff** | `staff` | `1` | POS terminal (Seat selection, Cart, Checkout, Customer lookup) |

> **Note:** Password is hashed with BCrypt. Default hash: `$2a$12$eUnQAUgU6wG1akE0xbsAYOkKsx.joNW1QHah.6A7fO7suIDtAnA76`

### First Launch Flow

```mermaid
flowchart TD
    A[Login UI] --> B{Role Check}
    B -->|ADMIN| C[Admin Portal]
    B -->|STAFF| D[Staff POS]
    B -->|OTHER| E[Access Denied]
    C --> C1[Dashboard]
    D --> D1[Seat Map]
```

---

## Troubleshooting

### Common Issues & Solutions

| Issue | Cause | Solution |
|-------|-------|----------|
| **Lombok not generating getters/setters** | Annotation processor not run | Run `mvn clean compile` to force re-generation |
| **Database connection failed** | MySQL container not ready | Wait 10-15s after `docker-compose up`, or check logs: `docker logs f3_cinema_db` |
| **Font display issues** | Missing system fonts | App uses `-apple-system` fallback; no action needed |
| **Empty database after restart** | Docker volume not persisted | Use `docker-compose down -v` to wipe volumes, then restart |
| **Port 3306 already in use** | Another MySQL instance running | Stop local MySQL or change port in `docker-compose.yml` |

### Reset Database

```bash
# Complete reset (removes all data)
docker-compose down -v
docker-compose up -d

# Re-initialize if needed
docker exec -i f3_cinema_db mysql -uroot -p123456 f3_cinema < src/main/resources/sql/init.sql
```

### View Application Logs

```bash
# Application logs (Log4j2)
# Check console output when running with -Dexec.outputLogging=always
```

---

## Contributors

<div align="center">

<img src="https://avatars.githubusercontent.com/u/187768450?v=4" width="80" style="border-radius: 50%;" />
<img src="https://ui-avatars.com/api/?name=taidangdev&background=7F00FF&color=fff&size=100" width="80" style="border-radius: 50%;" />
<img src="https://ui-avatars.com/api/?name=NhatHuy&background=4479A1&color=fff&size=100" width="80" style="border-radius: 50%;" />

<br/>

**CoffatDev** &nbsp;&nbsp;•&nbsp;&nbsp; **taidangdev** &nbsp;&nbsp;•&nbsp;&nbsp; **Nhat Huy**

<br/>

![F3 Cinema](https://capsule-render.vercel.app/api?type=waving&color=0f172a&height=180&section=footer&text=Thank+You+For+Visiting&fontSize=40&animation=fadeIn&fontAlignY=55&desc=Built+with+❤️+by+F3+Team)

<br/>

[![License](https://img.shields.io/badge/License-MIT-ffffff?style=for-the-badge&labelColor=1a1a2e)](LICENSE)
[![Status](https://img.shields.io/badge/Status-Active-10b981?style=for-the-badge&labelColor=1a1a2e)](https://github.com/f3-cinema)

</div>

---

<div align="center">

*This README was generated for F3 Cinema Management System v1.0.0*

</div>