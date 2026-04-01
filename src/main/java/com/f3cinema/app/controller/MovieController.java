package com.f3cinema.app.controller;

import com.f3cinema.app.entity.Movie;
import com.f3cinema.app.service.MovieService;
import com.f3cinema.app.ui.admin.dialog.MovieDialog;
import com.f3cinema.app.ui.common.dialog.AppMessageDialogs;
import com.f3cinema.app.ui.dashboard.MoviePanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * MovieController — Processes UI events for Card-Based Movie View.
 * Modified to support direct movie injection from Card events.
 */
public class MovieController {

    private final MoviePanel view;
    private final MovieService movieService;

    public MovieController(MoviePanel view) {
        this.view = view;
        this.movieService = MovieService.getInstance();
    }

    public void init() {
        loadMovies(null, -1L);
    }

    public void loadMovies(String keyword, Long genreId) {
        view.setLoadingState(true);

        SwingWorker<List<Movie>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Movie> doInBackground() {
                List<Movie> list = (keyword == null || keyword.isBlank())
                        ? movieService.getAllMovies()
                        : movieService.searchByTitle(keyword);
                
                if (genreId != null && genreId != -1L) {
                    list = list.stream().filter(m -> 
                        m.getGenres() != null && m.getGenres().stream().anyMatch(g -> g.getId().equals(genreId))
                    ).toList();
                }
                return list;
            }

            @Override
            protected void done() {
                try {
                    List<Movie> data = get();
                    System.out.println("=== DEBUG: Loaded " + data.size() + " movies ===");
                    view.updateTableData(data);
                } catch (Exception ex) {
                    view.showErrorMessage("⚠ Lỗi kết nối cơ sở dữ liệu: " + ex.getMessage());
                } finally {
                    view.setLoadingState(false);
                }
            }
        };
        worker.execute();
    }

    public void handleAddAction() {
        Window owner = SwingUtilities.getWindowAncestor(view);
        MovieDialog dialog = new MovieDialog(owner, null, movieService);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadMovies(view.getSearchText(), view.getSelectedGenreId());
        }
    }

    public void handleEditAction(Movie selected) {
        if (selected == null) return;
        Window owner = SwingUtilities.getWindowAncestor(view);
        MovieDialog dialog = new MovieDialog(owner, selected, movieService);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadMovies(view.getSearchText(), view.getSelectedGenreId());
        }
    }

    public void handleDeleteAction(Movie selected) {
        if (selected == null) return;

        if (AppMessageDialogs.confirmYesNo(view, "Xác nhận xóa",
                "Bạn có chắc muốn xóa phim: \"" + selected.getTitle() + "\"?")) {
            try {
                movieService.deleteMovie(selected.getId());
                loadMovies(view.getSearchText(), view.getSelectedGenreId());
            } catch (Exception ex) {
                AppMessageDialogs.showError(view, "Lỗi", "Lỗi khi xóa phim: " + ex.getMessage());
            }
        }
    }
}
