package com.f3cinema.app.service.impl;

import com.f3cinema.app.config.HibernateUtil;
import com.f3cinema.app.dto.StockReceiptDTO;
import com.f3cinema.app.dto.StockReceiptItemDTO;
import com.f3cinema.app.entity.Product;
import com.f3cinema.app.entity.StockReceipt;
import com.f3cinema.app.entity.StockReceiptItem;
import com.f3cinema.app.exception.CinemaException;
import com.f3cinema.app.service.StockReceiptService;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Implement Singleton StockReceiptService with Master-Detail pattern.
 */
@Log4j2
public class StockReceiptServiceImpl implements StockReceiptService {

    private static final StockReceiptServiceImpl INSTANCE = new StockReceiptServiceImpl();

    private StockReceiptServiceImpl() {}

    public static StockReceiptServiceImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public void createReceipt(StockReceiptDTO dto) {
        if (dto.items() == null || dto.items().isEmpty()) {
            throw new CinemaException("Phiếu nhập kho phải có ít nhất 1 sản phẩm");
        }

        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Nhắc nhở cấu hình Backend Development Standards: 
            // Cần setup hibernate.jdbc.batch_size=50 trong hibernate.cfg.xml 
            // để tối ưu batch insert cho danh sách StockReceiptItem lớn.
            transaction = session.beginTransaction();

            // 1. Khởi tạo và Lưu StockReceipt (Master)
            StockReceipt receipt = StockReceipt.builder()
                .receiptDate(LocalDateTime.now())
                .supplier(dto.supplier())
                .totalImportCost(dto.totalImportCost())
                .items(new ArrayList<>())
                .build();

            // 2. Liên kết các StockReceiptItem (Detail)
            for (StockReceiptItemDTO itemDto : dto.items()) {
                Product product = session.get(Product.class, itemDto.productId());
                if (product == null) {
                    throw new CinemaException("Không tìm thấy Sản phẩm với ID: " + itemDto.productId());
                }

                StockReceiptItem receiptItem = StockReceiptItem.builder()
                    .stockReceipt(receipt) // Set liên kết về Master
                    .product(product)
                    .quantity(itemDto.quantity())
                    .importPrice(itemDto.importPrice())
                    .build();

                receipt.getItems().add(receiptItem);
            }
            
            // Persist Master (Detail sẽ được lưu theo vì có cascade = CascadeType.ALL)
            session.persist(receipt);

            // Xả dữ liệu xuống DB ngay để DB ghi nhận Receipt & Items nhưng chưa commit
            session.flush();

            // 3. QUAN TRỌNG: Cập nhật Tồn kho (Inventory) TRONG CÙNG 1 TRANSACTION
            // Thay vì gọi sang Service mở Session mới (mất tính ACID), ta Query thẳng trên session hiện tại.
            for (StockReceiptItemDTO itemDto : dto.items()) {
                String hqlUpdate = "UPDATE Inventory i SET i.currentQuantity = i.currentQuantity + :qty WHERE i.product.id = :pid";
                int updated = session.createMutationQuery(hqlUpdate)
                        .setParameter("qty", itemDto.quantity())
                        .setParameter("pid", itemDto.productId())
                        .executeUpdate();
                        
                if (updated == 0) {
                    throw new CinemaException("Không tìm thấy kho hàng của Sản phẩm ID: " + itemDto.productId());
                }
            }

            transaction.commit();
            log.info("Đã tạo mới phiếu nhập kho ({}) và cập nhật tồn kho thành công.", dto.supplier());
            
        } catch (CinemaException ce) {
            if (transaction != null && transaction.isActive()) {
                try {
                    transaction.rollback();
                } catch (Exception ex) {
                    log.error("Failed to rollback transaction", ex);
                }
            }
            throw ce;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                try {
                    transaction.rollback();
                } catch (Exception ex) {
                    log.error("Failed to rollback transaction", ex);
                }
            }
            log.error("Lỗi khi tạo phiếu nhập kho", e);
            String rootCause = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            throw new CinemaException("Không thể tạo phiếu nhập kho: " + rootCause, e);
        }
    }

    @Override
    public java.util.List<com.f3cinema.app.dto.StockReceiptSummaryDTO> getAllReceipts() {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            String hql = "FROM StockReceipt s ORDER BY s.receiptDate DESC";
            java.util.List<StockReceipt> receipts = session.createQuery(hql, StockReceipt.class).getResultList();
            
            java.util.List<com.f3cinema.app.dto.StockReceiptSummaryDTO> result = receipts.stream()
                .map(r -> new com.f3cinema.app.dto.StockReceiptSummaryDTO(
                    r.getId(),
                    r.getSupplier(),
                    r.getTotalImportCost(),
                    r.getReceiptDate()
                )).collect(java.util.stream.Collectors.toList());
            
            transaction.commit();
            return result;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                try {
                    transaction.rollback();
                } catch (Exception ex) {
                    log.error("Failed to rollback transaction", ex);
                }
            }
            log.error("Lỗi khi lấy danh sách lịch sử phiếu nhập", e);
            String rootMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            throw new CinemaException("Không thể lấy dữ liệu lịch sử nhập kho. Chi tiết: " + rootMessage, e);
        }
    }

    @Override
    public com.f3cinema.app.dto.StockReceiptDTO getReceiptDetails(Long receiptId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // Join Fetch for N+1 optimization
            String hql = "SELECT s FROM StockReceipt s LEFT JOIN FETCH s.items i LEFT JOIN FETCH i.product WHERE s.id = :id";
            StockReceipt receipt = session.createQuery(hql, StockReceipt.class)
                                         .setParameter("id", receiptId)
                                         .uniqueResult();
            
            if (receipt == null) {
                throw new CinemaException("Không tìm thấy phiếu nhập kho có ID: PN-" + receiptId);
            }
            
            java.util.List<com.f3cinema.app.dto.StockReceiptItemDTO> itemDTOs = receipt.getItems().stream()
                .map(item -> new com.f3cinema.app.dto.StockReceiptItemDTO(
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getImportPrice()
                )).collect(java.util.stream.Collectors.toList());
                
            com.f3cinema.app.dto.StockReceiptDTO dto = new com.f3cinema.app.dto.StockReceiptDTO(
                receipt.getSupplier(),
                itemDTOs,
                receipt.getTotalImportCost()
            );

            transaction.commit();
            return dto;
        } catch (CinemaException ce) {
            if (transaction != null && transaction.isActive()) {
                try { transaction.rollback(); } catch (Exception ex) { log.error(ex); }
            }
            throw ce;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                try { transaction.rollback(); } catch (Exception ex) { log.error(ex); }
            }
            log.error("Lỗi khi lấy chi tiết phiếu nhập", e);
            throw new CinemaException("Không thể lấy chi tiết phiếu nhập kho.", e);
        }
    }
}
