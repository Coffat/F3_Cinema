package com.f3cinema.app.controller;

import com.f3cinema.app.entity.Showtime;
import com.f3cinema.app.service.ShowtimeService;
import com.f3cinema.app.ui.admin.dialog.ShowtimeDialog;
import com.f3cinema.app.ui.dashboard.ShowtimePanel;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * ShowtimeController - Điều phối giữa UI Quản lý Suất chiếu và Service.
 * Tuân thủ chuẩn Controller: Xử lý sự kiện và chạy tác vụ nền (Async).
 */
public class ShowtimeController {

    private final ShowtimePanel view;
    private final ShowtimeService showtimeService;

    public ShowtimeController(ShowtimePanel view) {
        this.view = view;
        this.showtimeService = ShowtimeService.getInstance();
    }

    public void init() {
        loadShowtimes(LocalDate.now(), null, null);
    }

    public void loadShowtimes(LocalDate date, Long movieId, Long roomId) {
        view.setLoadingState(true);

        new SwingWorker<List<Showtime>, Void>() {
            @Override
            protected List<Showtime> doInBackground() {
                // Sử dụng DAO trực tiếp để lấy entity thô cho admin quản lý
                return new com.f3cinema.app.repository.ShowtimeRepositoryImpl().findByFilter(date, movieId, roomId);
            }

            @Override
            protected void done() {
                try {
                    List<Showtime> data = get();
                    view.updateTableData(data);
                } catch (Exception ex) {
                    view.showErrorMessage("Lỗi kết nối cơ sở dữ liệu: " + ex.getMessage());
                } finally {
                    view.setLoadingState(false);
                }
            }
        }.execute();
    }
    
    // Lưu ý: Logic lấy danh sách gốc ở trên hơi rườm rà do tôi đang cố reuse filter. 
    // Tốt nhất là thêm hàm getAllShowtimesByFilter vào Service.

    public void handleAddAction() {
        Window owner = SwingUtilities.getWindowAncestor(view);
        ShowtimeDialog dialog = new ShowtimeDialog(owner, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadShowtimes(view.getSelectedDate(), view.getSelectedMovieId(), view.getSelectedRoomId());
        }
    }

    public void handleEditAction(Showtime selected) {
        if (selected == null) return;
        Window owner = SwingUtilities.getWindowAncestor(view);
        ShowtimeDialog dialog = new ShowtimeDialog(owner, selected);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadShowtimes(view.getSelectedDate(), view.getSelectedMovieId(), view.getSelectedRoomId());
        }
    }

    public void handleDeleteAction(Showtime selected) {
        if (selected == null) return;

        int confirm = JOptionPane.showConfirmDialog(view,
                "Bạn có chắc muốn xóa suất chiếu lúc " + selected.getStartTime() + " không?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                showtimeService.deleteShowtime(selected.getId());
                loadShowtimes(view.getSelectedDate(), view.getSelectedMovieId(), view.getSelectedRoomId());
            } catch (Exception ex) {
                view.showErrorMessage("Lỗi khi xóa: " + ex.getMessage());
            }
        }
    }
}
