package com.f3cinema.app.ui.dashboard.showtime;

import com.f3cinema.app.entity.Room;
import com.f3cinema.app.entity.Showtime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Flattens grouped showtimes, detects room conflicts, formats times for the schedule table.
 */
public final class ShowtimeScheduleSupport {

    private static final DateTimeFormatter FULL = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter TIME_ONLY = DateTimeFormatter.ofPattern("HH:mm");

    private ShowtimeScheduleSupport() {}

    /**
     * Ordered list: by room order, then by start time within each room.
     */
    public static List<Showtime> flattenInRoomOrder(List<Room> rooms, Map<Long, List<Showtime>> grouped) {
        List<Showtime> out = new ArrayList<>();
        for (Room room : rooms) {
            List<Showtime> list = grouped.getOrDefault(room.getId(), List.of());
            List<Showtime> copy = new ArrayList<>(list);
            copy.sort(Comparator.comparing(Showtime::getStartTime));
            out.addAll(copy);
        }
        return out;
    }

    /**
     * Maps showtime id → conflict warning text (may combine multiple overlaps).
     */
    public static Map<Long, String> buildConflictMessages(List<Showtime> sameRoomSorted) {
        Map<Long, StringBuilder> buf = new HashMap<>();
        int n = sameRoomSorted.size();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                Showtime a = sameRoomSorted.get(i);
                Showtime b = sameRoomSorted.get(j);
                if (a.getEndTime().isAfter(b.getStartTime())) {
                    appendConflict(buf, a, b);
                    appendConflict(buf, b, a);
                }
            }
        }
        Map<Long, String> out = new HashMap<>();
        buf.forEach((id, sb) -> out.put(id, sb.toString()));
        return out;
    }

    private static void appendConflict(Map<Long, StringBuilder> buf, Showtime src, Showtime other) {
        String line = "Trùng với: " + other.getMovie().getTitle()
                + " (" + formatRange(other.getStartTime(), other.getEndTime()) + ")";
        buf.computeIfAbsent(src.getId(), k -> new StringBuilder()).append(line).append("; ");
    }

    public static Map<Long, String> conflictMessagesByShowtimeId(List<Room> rooms, Map<Long, List<Showtime>> grouped) {
        Map<Long, String> merged = new HashMap<>();
        for (Room room : rooms) {
            List<Showtime> list = new ArrayList<>(grouped.getOrDefault(room.getId(), List.of()));
            if (list.size() < 2) continue;
            list.sort(Comparator.comparing(Showtime::getStartTime));
            merged.putAll(buildConflictMessages(list));
        }
        return merged;
    }

    public static String formatRange(LocalDateTime start, LocalDateTime end) {
        return formatDateTime(start, start.toLocalDate()) + " — " + formatDateTime(end, start.toLocalDate());
    }

    /**
     * @param viewDay selected calendar day in the admin picker (showtimes loaded for this day)
     */
    public static String formatDateTime(LocalDateTime dt, LocalDate viewDay) {
        if (dt == null) return "";
        LocalDate d = dt.toLocalDate();
        if (d.equals(viewDay)) {
            return dt.format(TIME_ONLY);
        }
        if (d.equals(viewDay.plusDays(1))) {
            return dt.format(TIME_ONLY) + " (hôm sau)";
        }
        return dt.format(FULL);
    }
}
