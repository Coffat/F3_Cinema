# F3 Cinema Management - Agent Guidelines

## 1. Build & Run Commands

### Build Project
```bash
mvn clean compile
```

### Run Application
```bash
mvn exec:java -Dexec.mainClass="com.f3cinema.app.App"
```

### Package JAR
```bash
mvn package
```

### Database (Docker)
```bash
# Start MySQL container
docker-compose up -d

# Restart with fresh volume (removes all data)
docker-compose down -v && docker-compose up -d

# Re-run init.sql if needed
docker exec -i f3_cinema_db mysql -uroot -p123456 f3_cinema < src/main/resources/sql/init.sql
```

## 2. Technology Stack

- **Language**: Java 21 (LTS)
- **ORM**: Hibernate 6.6.1.Final (Jakarta Persistence)
- **Database**: MySQL 8.4
- **UI**: Swing with FlatLaf 3.5.1 (Modern Midnight theme)
- **Build**: Maven 3.9+

## 3. Project Structure

```
src/main/java/com/f3cinema/app/
├── config/          # HibernateUtil, ThemeConfig
├── controller/      # UI controllers
├── dto/             # Data Transfer Objects (Java Records)
├── entity/          # JPA Entities
│   └── enums/       # Enum types
├── exception/       # Custom exceptions (CinemaException, AuthenticationException)
├── repository/      # Data Access Layer
├── service/         # Business Logic
│   ├── cart/       # Cart management (Command pattern)
│   ├── payment/    # Payment (Strategy pattern)
│   └── impl/       # Service implementations
├── ui/              # Swing UI Layer
│   ├── admin/       # Admin interface
│   ├── dashboard/  # Shared dashboard components
│   ├── staff/       # Staff interface
│   └── components/ # Reusable UI components
└── util/            # Utilities
```

## 4. Code Style Guidelines

### 4.1 Naming Conventions

- **Classes**: PascalCase (`UserService`, `MoviePanel`)
- **Methods**: camelCase (`getAllMovies()`, `onLoginSuccess()`)
- **Variables**: camelCase (`userName`, `movieList`)
- **Constants**: UPPER_SNAKE_CASE (`MAX_RETRY_COUNT`)
- **Packages**: lowercase (`com.f3cinema.app.service`)

### 4.2 Entity Classes

- Use Lombok annotations: `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- Always use `@Id` with `@GeneratedValue(strategy = GenerationType.IDENTITY)` for auto-increment
- Use `@Enumerated(EnumType.STRING)` for enums
- Use `FetchType.LAZY` for relationships

```java
@Entity
@Table(name = "movies")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Enumerated(EnumType.STRING)
    private MovieStatus status;
}
```

### 4.3 DTOs (Data Transfer Objects)

- Use Java Records for simple DTOs
- Never expose JPA Entities directly to UI layer

```java
public record ProductDTO(Long id, String name, BigDecimal price, String unit) {}
```

### 4.4 Service Layer

- Use Singleton pattern for services that need single instance
- Always handle transactions properly with rollback on failure
- Use custom exceptions (`CinemaException`) for business logic errors

```java
public class InventoryServiceImpl implements InventoryService {
    private static final InventoryServiceImpl INSTANCE = new InventoryServiceImpl();
    
    private InventoryServiceImpl() {}
    
    public static InventoryServiceImpl getInstance() {
        return INSTANCE;
    }
}
```

### 4.5 UI Components

- Use `BaseDashboardModule` as parent for dashboard panels
- Use `FlatLaf` and `FlatClientProperties` for styling
- Use `SwingWorker` for async database operations (never block UI thread)
- Use standard prefix for component names:
  - `btn` = JButton/FlatButton
  - `lbl` = JLabel
  - `txt` = JTextField/JTextArea
  - `tbl` = JTable
  - `cb` = JComboBox

### 4.6 Error Handling

- Use custom exceptions: `CinemaException`, `AuthenticationException`
- Always log errors with Log4j2: `log.error("message", e)`
- Show user-friendly messages in UI dialogs

```java
} catch (Exception e) {
    if (transaction != null && transaction.isActive()) {
        transaction.rollback();
    }
    log.error("Failed to load products", e);
    throw new CinemaException("Cannot load products: " + e.getMessage());
}
```

### 4.7 Logging

- Use `@Log4j2` Lombok annotation
- Log levels: ERROR for failures, INFO for successful operations

## 5. Design Patterns (When Needed)

### Strategy Pattern
Use for payment methods, export formats:
```java
public interface PaymentStrategy {
    void pay(Invoice invoice);
}
```

### Command Pattern
Use for cart operations (add, remove, update, clear):
```java
public interface CartCommand {
    void execute();
}
```

### Observer Pattern
Use for cart updates:
```java
public interface CartObserver {
    void onCartUpdated();
}
```

## 6. Database Conventions

### Foreign Key Naming
- `fk_[table]_[referenced_table]`: `fk_seats_room`

### Enum Values
- Store as VARCHAR with full names: `COMING_SOON`, `NOW_SHOWING`, `ENDED`

## 7. UI Styling Guidelines

### Colors (Midnight Theme)
- Background: `#0F172A` (Slate 900)
- Surface: `#1E293B` (Slate 800)
- Accent: `#6366F1` (Indigo 500)
- Danger: `#F43F5E` (Rose 500)
- Text Primary: `#F8FAFC` (Slate 50)

### Fonts
- Use `-apple-system` for cross-platform compatibility (fallback to system font)
- Do NOT use hardcoded "Inter" font - it may not be installed

### Components
- Border radius: 16px for cards, 12px for buttons
- Use `FlatButton` instead of `JButton`
- Use `FlatSVGIcon` for icons

## 8. Testing

This project does not have a formal test suite. For manual testing:
1. Ensure MySQL container is running: `docker-compose up -d`
2. Run the app: `mvn exec:java -Dexec.mainClass="com.f3cinema.app.App"`
3. Default login credentials:
   - Admin: `admin` / `admin123`
   - Staff: `staff` / `staff123`

## 9. Common Issues & Solutions

### Lombok not generating getters/setters
- Run `mvn clean compile` to force re-compilation
- Ensure annotation processor path is configured in pom.xml

### Font display issues
- Use `-apple-system` instead of "Inter" for cross-platform support

### Database not initialized
- Restart Docker: `docker-compose down -v && docker-compose up -d`
- Manually run init.sql if needed
