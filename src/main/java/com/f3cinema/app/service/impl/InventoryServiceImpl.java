package com.f3cinema.app.service.impl;

import com.f3cinema.app.config.HibernateUtil;
import com.f3cinema.app.dto.ProductDTO;
import com.f3cinema.app.dto.dashboard.InventoryAlertRow;
import com.f3cinema.app.entity.Inventory;
import com.f3cinema.app.entity.Product;
import com.f3cinema.app.exception.CinemaException;
import com.f3cinema.app.repository.DashboardRepository;
import com.f3cinema.app.repository.DashboardRepositoryImpl;
import com.f3cinema.app.repository.ProductRepository;
import com.f3cinema.app.repository.ProductRepositoryImpl;
import com.f3cinema.app.service.InventoryService;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Thread-safe Singleton Implementation of InventoryService.
 * Handles Database Transactions and implements the Master-Detail
 * (Product-Inventory) logic.
 */
@Log4j2
public class InventoryServiceImpl implements InventoryService {

    private static final InventoryServiceImpl INSTANCE = new InventoryServiceImpl();
    private final DashboardRepository dashboardRepository = DashboardRepositoryImpl.getInstance();
    private final ProductRepository productRepository = new ProductRepositoryImpl();

    private InventoryServiceImpl() {
    }

    public static InventoryServiceImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public List<ProductDTO> getAllInventory() {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            String hql = "FROM Product p LEFT JOIN FETCH p.inventory";
            List<Product> products = session.createQuery(hql, Product.class).getResultList();
            
            log.info("Fetched {} products from database", products.size());

            List<ProductDTO> result = products.stream().map(p -> {
                int currentQty = (p.getInventory() != null && p.getInventory().getCurrentQuantity() != null)
                        ? p.getInventory().getCurrentQuantity()
                        : 0;
                int minThreshold = (p.getInventory() != null && p.getInventory().getMinThreshold() != null)
                        ? p.getInventory().getMinThreshold()
                        : 0;

                return new ProductDTO(
                        p.getId(),
                        p.getName(),
                        p.getPrice(),
                        p.getUnit(),
                        p.getImageUrl(),
                        currentQty,
                        minThreshold);
            }).collect(Collectors.toList());

            transaction.commit();
            log.info("Returning {} ProductDTOs", result.size());
            return result;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                try {
                    transaction.rollback();
                } catch (Exception ex) {
                    log.error("Failed to rollback transaction", ex);
                }
            }
            log.error("Lỗi khi lấy danh sách sản phẩm & tồn kho", e);
            String rootMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            throw new CinemaException("Không thể lấy dữ liệu tồn kho. Chi tiết: " + rootMessage, e);
        }
    }

    @Override
    public void addProduct(ProductDTO dto) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // 1. Tạo Product
            Product product = Product.builder()
                    .name(dto.name())
                    .price(dto.price())
                    .unit(dto.unit())
                    .imageUrl(dto.imageUrl())
                    .build();

            // 2. Tạo Inventory đính kèm
            Inventory inventory = Inventory.builder()
                    .product(product)
                    .currentQuantity(0)
                    .minThreshold(dto.minThreshold())
                    .build();

            // 3. Khớp nối quan hệ 2 chiều
            product.setInventory(inventory);

            // 4. Chỉ cần persist Product. Hibernate sẽ tự động lưu luôn Inventory nhờ CascadeType.ALL
            session.persist(product);

            transaction.commit();
            log.info("Successfully added product: {}", dto.name());
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                try {
                    transaction.rollback();
                } catch (Exception ex) {
                    log.error("Failed to rollback transaction", ex);
                }
            }
            log.error("Failed to add product: {}", dto.name(), e);
            throw new CinemaException("Lỗi lưu dữ liệu: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateProduct(ProductDTO dto) {
        if (dto.id() == null) {
            throw new CinemaException("ID sản phẩm không được trống khi cập nhật.");
        }

        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Fetch product within current session to ensure proper merging
            Product product = session.get(Product.class, dto.id());
            if (product == null) {
                throw new CinemaException("Không tìm thấy sản phẩm với ID: " + dto.id());
            }

            // Map standard fields
            product.setName(dto.name());
            product.setPrice(dto.price());
            product.setUnit(dto.unit());
            product.setImageUrl(dto.imageUrl());

            // Update Inventory Threshold
            if (product.getInventory() != null) {
                product.getInventory().setMinThreshold(dto.minThreshold());
            }

            session.merge(product);
            transaction.commit();
            log.info("Successfully updated product ID: {}", dto.id());
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                try { transaction.rollback(); } catch (Exception ex) { log.error(ex); }
            }
            log.error("Failed to update product ID: {}", dto.id(), e);
            throw new CinemaException("Không thể cập nhật sản phẩm: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteProduct(Long productId) {
        if (productId == null) {
            throw new CinemaException("ID sản phẩm không hợp lệ.");
        }

        // 1. Validation: Check existence
        productRepository.findById(productId)
                .orElseThrow(() -> new CinemaException("Không tìm thấy sản phẩm ID: " + productId));

        try {
            // 2. Perform Soft Delete via Repository
            productRepository.softDelete(productId);
            log.info("Successfully soft-deleted product ID: {}", productId);
        } catch (Exception e) {
            log.error("Failed to delete product ID: {}", productId, e);
            throw new CinemaException("Lỗi khi thực hiện xóa sản phẩm: " + e.getMessage(), e);
        }
    }

    @Override
    public synchronized void addStock(Long productId, int quantity) {
        if (quantity <= 0) {
            throw new CinemaException("Số lượng nhập kho cần thêm phải lớn hơn 0");
        }

        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Tìm bản ghi Inventory theo Product ID
            String hql = "SELECT i FROM Inventory i JOIN FETCH i.product WHERE i.product.id = :productId";
            Inventory inventory = session.createQuery(hql, Inventory.class)
                    .setParameter("productId", productId)
                    .uniqueResult();

            if (inventory == null) {
                throw new CinemaException("Không tìm thấy kho hàng của Sản phẩm ID: " + productId);
            }

            // Cộng dồn vào currentQuantity
            inventory.setCurrentQuantity(inventory.getCurrentQuantity() + quantity);
            session.merge(inventory);

            transaction.commit();
            log.info("Cộng dồn thành công {} đơn vị số lượng vào sản phẩm ID: {}", quantity, productId);
        } catch (CinemaException ce) {
            if (transaction != null) {
                try {
                    transaction.rollback();
                } catch (Exception ex) {
                    log.error("Failed to rollback transaction", ex);
                }
            }
            throw ce;
        } catch (Exception e) {
            if (transaction != null) {
                try {
                    transaction.rollback();
                } catch (Exception ex) {
                    log.error("Failed to rollback transaction", ex);
                }
            }
            log.error("Lỗi khi cộng dồn số lượng tồn kho", e);
            throw new CinemaException("Không thể cộng dồn số lượng tồn kho", e);
        }
    }

    @Override
    public List<InventoryAlertRow> getLowStockAlerts() {
        log.debug("Loading inventory alerts from dashboard repository");
        return dashboardRepository.loadInventoryAlerts();
    }
}
