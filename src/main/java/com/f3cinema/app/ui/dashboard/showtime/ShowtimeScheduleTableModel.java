package com.f3cinema.app.ui.dashboard.showtime;

import com.f3cinema.app.entity.Showtime;

import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Read-only table model for admin showtime schedule (one row per showtime).
 */
public class ShowtimeScheduleTableModel extends AbstractTableModel {

    public static final int COL_ROOM = 0;
    public static final int COL_START = 1;
    public static final int COL_END = 2;
    public static final int COL_MOVIE = 3;
    public static final int COL_PRICE = 4;
    public static final int COL_WARN = 5;

    private static final String[] NAMES = {
            "Phòng", "Bắt đầu", "Kết thúc", "Phim", "Giá (VNĐ)", "Cảnh báo"
    };

    private List<Showtime> rows;
    private final Map<Long, String> conflictById;
    private final LocalDate viewDay;

    public ShowtimeScheduleTableModel(List<Showtime> rows, Map<Long, String> conflictById, LocalDate viewDay) {
        this.rows = rows;
        this.conflictById = conflictById != null ? conflictById : Map.of();
        this.viewDay = viewDay;
    }

    public void setRows(List<Showtime> rows) {
        this.rows = rows;
        fireTableDataChanged();
    }

    public Showtime getShowtimeAt(int modelRow) {
        return rows.get(modelRow);
    }

    public List<Showtime> getRows() {
        return rows;
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return NAMES.length;
    }

    @Override
    public String getColumnName(int column) {
        return NAMES[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Showtime st = rows.get(rowIndex);
        return switch (columnIndex) {
            case COL_ROOM -> st.getRoom() != null ? st.getRoom().getName() : "";
            case COL_START -> ShowtimeScheduleSupport.formatDateTime(st.getStartTime(), viewDay);
            case COL_END -> ShowtimeScheduleSupport.formatDateTime(st.getEndTime(), viewDay);
            case COL_MOVIE -> st.getMovie() != null ? st.getMovie().getTitle() : "";
            case COL_PRICE -> formatPrice(st.getBasePrice());
            case COL_WARN -> trimConflict(conflictById.getOrDefault(st.getId(), ""));
            default -> "";
        };
    }

    private static String formatPrice(BigDecimal p) {
        if (p == null) return "";
        return String.format("%,.0f", p.doubleValue());
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public LocalDate getViewDay() {
        return viewDay;
    }

    private static String trimConflict(String s) {
        if (s.endsWith("; ")) return s.substring(0, s.length() - 2);
        return s;
    }
}
