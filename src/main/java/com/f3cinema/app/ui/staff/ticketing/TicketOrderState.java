package com.f3cinema.app.ui.staff.ticketing;

import com.f3cinema.app.dto.ProductDTO;
import com.f3cinema.app.entity.Customer;
import com.f3cinema.app.entity.enums.PointRedemptionTier;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.math.BigDecimal;
import java.util.*;

/**
 * Central state holder for the unified ticketing flow.
 * Uses Observer Pattern (PropertyChangeSupport) to notify UI panels when state changes.
 */
public class TicketOrderState {

    private static TicketOrderState instance;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private Long showtimeId;
    private String movieTitle;
    private String roomName;
    private String startTime;
    private BigDecimal basePrice;

    private final Map<Long, SeatInfo> selectedSeats = new LinkedHashMap<>();
    private BigDecimal seatTotal = BigDecimal.ZERO;

    private final Map<ProductDTO, Integer> snacksCart = new LinkedHashMap<>();
    private BigDecimal snacksTotal = BigDecimal.ZERO;

    private BigDecimal discount = BigDecimal.ZERO;
    private BigDecimal grandTotal = BigDecimal.ZERO;

    private int currentStep = 1;
    private String paymentMethod = "CASH";

    private Customer customer;
    private PointRedemptionTier selectedTier;
    private Integer pointsToRedeem = 0;

    private TicketOrderState() {}

    public static synchronized TicketOrderState getInstance() {
        if (instance == null) instance = new TicketOrderState();
        return instance;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    // ── Showtime ─────────────────────────────────────────────────────────────

    public void setShowtime(Long id, String title, String room, String time, BigDecimal price) {
        Long oldId = this.showtimeId;
        this.showtimeId = id;
        this.movieTitle = title;
        this.roomName = room;
        this.startTime = time;
        this.basePrice = price;
        pcs.firePropertyChange("showtimeId", oldId, id);
        pcs.firePropertyChange("showtime", null, this);
    }

    // ── Seats ────────────────────────────────────────────────────────────────

    public void addSeat(Long seatId, String seatName, BigDecimal price) {
        if (selectedSeats.containsKey(seatId)) return;
        selectedSeats.put(seatId, new SeatInfo(seatId, seatName, price));
        recalculateSeatTotal();
        pcs.firePropertyChange("selectedSeats", null, new ArrayList<>(selectedSeats.values()));
    }

    public void removeSeat(Long seatId) {
        if (selectedSeats.remove(seatId) != null) {
            recalculateSeatTotal();
            pcs.firePropertyChange("selectedSeats", null, new ArrayList<>(selectedSeats.values()));
        }
    }

    public void clearSeats() {
        if (!selectedSeats.isEmpty()) {
            selectedSeats.clear();
            recalculateSeatTotal();
            pcs.firePropertyChange("selectedSeats", null, new ArrayList<>());
        }
    }

    private void recalculateSeatTotal() {
        BigDecimal oldTotal = this.seatTotal;
        seatTotal = selectedSeats.values().stream()
                .map(SeatInfo::price)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        pcs.firePropertyChange("seatTotal", oldTotal, seatTotal);
        recalculateGrandTotal();
    }

    // ── Snacks ───────────────────────────────────────────────────────────────

    public void setSnacksFromCartManager(Map<ProductDTO, Integer> cartItems) {
        this.snacksCart.clear();
        this.snacksCart.putAll(cartItems);
        
        BigDecimal oldTotal = this.snacksTotal;
        this.snacksTotal = cartItems.entrySet().stream()
                .map(e -> e.getKey().price().multiply(BigDecimal.valueOf(e.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        pcs.firePropertyChange("snacksTotal", oldTotal, this.snacksTotal);
        pcs.firePropertyChange("snacksCart", null, new ArrayList<>(snacksCart.entrySet()));
        recalculateGrandTotal();
    }

    public void setDiscount(BigDecimal discount) {
        BigDecimal old = this.discount;
        this.discount = discount;
        pcs.firePropertyChange("discount", old, discount);
        recalculateGrandTotal();
    }

    private void recalculateGrandTotal() {
        BigDecimal old = this.grandTotal;
        this.grandTotal = seatTotal.add(snacksTotal).subtract(discount);
        pcs.firePropertyChange("grandTotal", old, this.grandTotal);
    }

    // ── Navigation ───────────────────────────────────────────────────────────

    public void setCurrentStep(int step) {
        int old = this.currentStep;
        this.currentStep = step;
        pcs.firePropertyChange("currentStep", old, step);
    }

    public void setPaymentMethod(String method) {
        String old = this.paymentMethod;
        this.paymentMethod = method;
        pcs.firePropertyChange("paymentMethod", old, method);
    }

    // ── Customer & Loyalty ───────────────────────────────────────────────────

    public void setCustomer(Customer customer) {
        Customer old = this.customer;
        this.customer = customer;
        pcs.firePropertyChange("customer", old, customer);
    }

    public void setPointRedemption(PointRedemptionTier tier) {
        PointRedemptionTier oldTier = this.selectedTier;
        this.selectedTier = tier;
        
        if (tier != null) {
            this.pointsToRedeem = tier.getRequiredPoints();
            BigDecimal subtotal = seatTotal.add(snacksTotal);
            BigDecimal pointDiscount = subtotal.multiply(BigDecimal.valueOf(tier.getDiscountPercent()))
                    .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
            setDiscount(pointDiscount);
        } else {
            this.pointsToRedeem = 0;
            setDiscount(BigDecimal.ZERO);
        }
        
        pcs.firePropertyChange("selectedTier", oldTier, tier);
        pcs.firePropertyChange("pointsToRedeem", null, pointsToRedeem);
    }

    public void clearCustomer() {
        this.customer = null;
        this.selectedTier = null;
        this.pointsToRedeem = 0;
        pcs.firePropertyChange("customer", null, null);
    }

    // ── Reset ────────────────────────────────────────────────────────────────

    public void reset() {
        showtimeId = null;
        movieTitle = null;
        roomName = null;
        startTime = null;
        basePrice = null;
        selectedSeats.clear();
        snacksCart.clear();
        seatTotal = BigDecimal.ZERO;
        snacksTotal = BigDecimal.ZERO;
        discount = BigDecimal.ZERO;
        grandTotal = BigDecimal.ZERO;
        currentStep = 1;
        paymentMethod = "CASH";
        customer = null;
        selectedTier = null;
        pointsToRedeem = 0;
        pcs.firePropertyChange("reset", null, null);
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public Long getShowtimeId() { return showtimeId; }
    public String getMovieTitle() { return movieTitle; }
    public String getRoomName() { return roomName; }
    public String getStartTime() { return startTime; }
    public BigDecimal getBasePrice() { return basePrice; }

    public List<SeatInfo> getSelectedSeats() {
        return new ArrayList<>(selectedSeats.values());
    }

    public List<Long> getSelectedSeatIds() {
        return new ArrayList<>(selectedSeats.keySet());
    }

    public BigDecimal getSeatTotal() { return seatTotal; }

    public Map<ProductDTO, Integer> getSnacksCart() {
        return new LinkedHashMap<>(snacksCart);
    }
    
    public Map<Long, Integer> getSnacksCartByProductId() {
        Map<Long, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<ProductDTO, Integer> entry : snacksCart.entrySet()) {
            result.put(entry.getKey().id(), entry.getValue());
        }
        return result;
    }

    public BigDecimal getSnacksTotal() { return snacksTotal; }
    public BigDecimal getDiscount() { return discount; }
    public BigDecimal getGrandTotal() { return grandTotal; }
    public int getCurrentStep() { return currentStep; }
    public String getPaymentMethod() { return paymentMethod; }
    
    public Customer getCustomer() { return customer; }
    public PointRedemptionTier getSelectedTier() { return selectedTier; }
    public Integer getPointsToRedeem() { return pointsToRedeem; }

    public boolean hasSeats() {
        return !selectedSeats.isEmpty();
    }

    public boolean hasSnacks() {
        return !snacksCart.isEmpty();
    }
    
    public boolean hasCustomer() {
        return customer != null;
    }

    public record SeatInfo(Long id, String name, BigDecimal price) {}
}
