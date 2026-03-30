package com.f3cinema.app.service;

import com.f3cinema.app.dto.RoomSummaryDTO;
import com.f3cinema.app.dto.ShowtimeCreateDTO;
import com.f3cinema.app.dto.ShowtimeSummaryDTO;
import com.f3cinema.app.entity.Movie;
import com.f3cinema.app.entity.Room;
import com.f3cinema.app.entity.Showtime;
import com.f3cinema.app.repository.ShowtimeRepository;
import com.f3cinema.app.repository.ShowtimeRepositoryImpl;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ShowtimeService — Xử lý nghiệp vụ suất chiếu.
 *
 * Tuân thủ Backend Standards §3.2:
 *  - Singleton Pattern.
 *  - Nhận/Trả DTO, KHÔNG để JPA Entity lộ ra ngoài tầng UI.
 *
 * Quy tắc validate:
 *  - Tạo mới (isNew=true)  : Chặn startTime < now - 5 phút.
 *  - Chỉnh sửa (isNew=false): Bỏ qua check quá khứ → cho phép sửa giá/phòng khi suất chiếu đang/đã chạy.
 *  - Cả hai: Tính endTime = startTime + movie.duration, sau đó check conflict.
 */
@Log4j2
public class ShowtimeService {

    private static ShowtimeService instance;
    private final ShowtimeRepository showtimeRepository;

    private ShowtimeService() {
        this.showtimeRepository = new ShowtimeRepositoryImpl();
    }

    public static synchronized ShowtimeService getInstance() {
        if (instance == null) instance = new ShowtimeService();
        return instance;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ — trả về DTO, không để Entity lộ ra UI
    // ─────────────────────────────────────────────────────────────────────────

    /** Danh sách suất chiếu rút gọn theo bộ lọc (dùng cho Admin Table & Staff Showtime List). */
    public List<ShowtimeSummaryDTO> getShowtimesForUI(LocalDate date, Long movieId, Long roomId) {
        log.info("Fetching showtimes — date: {}, movieId: {}, roomId: {}", date, movieId, roomId);
        return showtimeRepository.findByFilter(date, movieId, roomId)
                .stream()
                .map(this::mapToSummaryDTO)
                .collect(Collectors.toList());
    }

    /** Lấy Entity nội bộ để prefill Dialog khi Edit (không export DTO cho bước này). */
    public Showtime getShowtimeById(Long id) {
        return showtimeRepository.findById(id).orElse(null);
    }

    /** Mọi Showtime để Admin bảng (giữ lại cho ShowtimeController). */
    public List<Showtime> getAllShowtimes() {
        return showtimeRepository.findAll();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE — nhận ShowtimeCreateDTO từ UI
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Tạo mới suất chiếu từ DTO.
     * Quy trình: DTO → hydrate Entity (load Movie + Room) → tính endTime → validate → save.
     */
    public void createFromDTO(ShowtimeCreateDTO dto) {
        Showtime s = hydrateFromDTO(dto, null);
        validateShowtime(s, true);   // isNew = true → check quá khứ
        showtimeRepository.save(s);
        log.info("Showtime created: movieId={}, roomId={}, start={}", dto.movieId(), dto.roomId(), dto.startTime());
    }

    /**
     * Cập nhật suất chiếu từ DTO.
     * @param existingId ID của suất chiếu cần sửa.
     */
    public void updateFromDTO(Long existingId, ShowtimeCreateDTO dto) {
        Showtime s = getShowtimeById(existingId);
        if (s == null) throw new RuntimeException("Không tìm thấy suất chiếu ID=" + existingId);
        s = hydrateFromDTO(dto, s);
        validateShowtime(s, false);  // isNew = false → bỏ qua check quá khứ
        showtimeRepository.update(s);
        log.info("Showtime updated: id={}, start={}", existingId, dto.startTime());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────────────────────────────────

    public void deleteShowtime(Long id) {
        Showtime s = getShowtimeById(id);
        if (s != null) {
            showtimeRepository.delete(s);
            log.info("Showtime deleted: id={}", id);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LEGACY — giữ lại để không phá ShowtimeController (Admin table CRUD)
    // Sẽ dần loại bỏ khi Controller cũng chuyển sang DTO
    // ─────────────────────────────────────────────────────────────────────────

    /** @deprecated Dùng createFromDTO(ShowtimeCreateDTO) thay thế. */
    @Deprecated
    public void addShowtime(Showtime showtime) {
        validateShowtime(showtime, true);
        showtimeRepository.save(showtime);
    }

    /** @deprecated Dùng updateFromDTO(Long, ShowtimeCreateDTO) thay thế. */
    @Deprecated
    public void updateShowtime(Showtime showtime) {
        validateShowtime(showtime, false);
        showtimeRepository.update(showtime);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INTERNAL HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Hydrate Entity từ DTO: Load Movie & Room theo ID, áp dụng các trường từ DTO.
     * @param target Entity đang sửa (null = tạo mới).
     */
    private Showtime hydrateFromDTO(ShowtimeCreateDTO dto, Showtime target) {
        Showtime s = (target != null) ? target : new Showtime();

        // Load Movie
        Movie movie = MovieService.getInstance().getMovieById(dto.movieId());
        if (movie == null) throw new RuntimeException("Không tìm thấy Phim ID=" + dto.movieId());
        s.setMovie(movie);

        // Load Room
        Room room = RoomService.getInstance().getRoomById(dto.roomId());
        if (room == null) throw new RuntimeException("Không tìm thấy Phòng ID=" + dto.roomId());
        s.setRoom(room);

        s.setStartTime(dto.startTime());
        s.setBasePrice(dto.basePrice());
        return s;
    }

    /**
     * Validate & tính toán trước khi lưu.
     *
     * @param isNew true = tạo mới (áp dụng check quá khứ chặt chẽ),
     *              false = sửa (bỏ qua check quá khứ để Admin có thể sửa giá/phòng).
     */
    private void validateShowtime(Showtime s, boolean isNew) {
        // 1. Tính endTime = startTime + duration phim
        if (s.getMovie() == null || s.getStartTime() == null)
            throw new RuntimeException("Thiếu thông tin Phim hoặc Thời gian bắt đầu.");
        s.setEndTime(s.getStartTime().plusMinutes(s.getMovie().getDuration()));

        // 2. Chặn quá khứ — CHỈ áp dụng khi TẠO MỚI (cho phép buffer 5 phút)
        if (isNew && s.getStartTime().isBefore(LocalDateTime.now().minusMinutes(5))) {
            throw new RuntimeException(
                    "Không thể tạo suất chiếu trong quá khứ. " +
                    "Thời gian bắt đầu phải từ " +
                    LocalDateTime.now().toString() + " trở đi.");
        }

        // 3. Kiểm tra trùng lịch (cả tạo lẫn sửa)
        if (showtimeRepository.existsConflict(
                s.getRoom().getId(), s.getStartTime(), s.getEndTime(), s.getId())) {
            throw new RuntimeException(
                    "Suất chiếu bị TRÙNG LỊCH tại " + s.getRoom().getName() +
                    ". Vui lòng chọn khung giờ khác.");
        }
    }

    /** Chuyển Entity → DTO tóm tắt trả về UI. */
    private ShowtimeSummaryDTO mapToSummaryDTO(Showtime showtime) {
        return new ShowtimeSummaryDTO(
            showtime.getId(),
            showtime.getMovie().getTitle(),
            showtime.getRoom().getName() + " - " + showtime.getRoom().getRoomType(),
            showtime.getStartTime(),
            showtime.getEndTime(),
            showtime.getBasePrice().doubleValue()
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DTO helpers dùng cho Dialog (populate ComboBox)
    // ─────────────────────────────────────────────────────────────────────────

    /** Danh sách RoomSummaryDTO để populate cbRoom trong Dialog — không expose Entity. */
    public List<RoomSummaryDTO> getRoomSummaries() {
        return RoomService.getInstance().getAllRooms().stream()
                .map(r -> new RoomSummaryDTO(r.getId(), r.getName(), r.getRoomType().name()))
                .collect(Collectors.toList());
    }
}
