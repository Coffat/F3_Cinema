package com.f3cinema.app.controller;

import com.f3cinema.app.entity.Room;
import com.f3cinema.app.entity.Showtime;
import com.f3cinema.app.service.RoomService;
import com.f3cinema.app.service.ShowtimeService;
import com.f3cinema.app.ui.admin.dialog.ShowtimeDialog;
import com.f3cinema.app.ui.common.dialog.AppMessageDialogs;
import com.f3cinema.app.ui.dashboard.ShowtimePanel;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

/**
 * ShowtimeController — Orchestrates between the Timeline View and Service layer.
 * Loads showtimes grouped by room for the visual scheduling timeline.
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

        new SwingWorker<TimelineData, Void>() {
            @Override
            protected TimelineData doInBackground() {
                List<Showtime> showtimes = new com.f3cinema.app.repository.ShowtimeRepositoryImpl()
                        .findByFilter(date, movieId, roomId);

                // All rooms (or just the filtered one) for the sidebar
                List<Room> rooms;
                if (roomId != null) {
                    Room single = RoomService.getInstance().getRoomById(roomId);
                    rooms = single != null ? List.of(single) : List.of();
                } else {
                    rooms = RoomService.getInstance().getAllRooms();
                    rooms.sort(Comparator.comparing(Room::getName));
                }

                // Group showtimes by room ID
                Map<Long, List<Showtime>> grouped = new LinkedHashMap<>();
                for (Room r : rooms) grouped.put(r.getId(), new ArrayList<>());
                for (Showtime s : showtimes) {
                    grouped.computeIfAbsent(s.getRoom().getId(), k -> new ArrayList<>()).add(s);
                }

                return new TimelineData(rooms, grouped);
            }

            @Override
            protected void done() {
                try {
                    TimelineData result = get();
                    view.updateTimelineData(result.rooms, result.grouped);
                } catch (Exception ex) {
                    view.showErrorMessage("Lỗi kết nối cơ sở dữ liệu: " + ex.getMessage());
                } finally {
                    view.setLoadingState(false);
                }
            }
        }.execute();
    }

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

        if (AppMessageDialogs.confirmYesNo(view, "Xác nhận xóa",
                "Bạn có chắc muốn xóa suất chiếu lúc " + selected.getStartTime() + " không?")) {
            try {
                showtimeService.deleteShowtime(selected.getId());
                loadShowtimes(view.getSelectedDate(), view.getSelectedMovieId(), view.getSelectedRoomId());
            } catch (Exception ex) {
                view.showErrorMessage("Lỗi khi xóa: " + ex.getMessage());
            }
        }
    }

    private record TimelineData(List<Room> rooms, Map<Long, List<Showtime>> grouped) {}
}
