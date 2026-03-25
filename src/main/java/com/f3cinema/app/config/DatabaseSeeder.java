package com.f3cinema.app.config;

import com.f3cinema.app.entity.Movie;
import com.f3cinema.app.entity.User;
import com.f3cinema.app.entity.Genre;
import com.f3cinema.app.entity.enums.MovieStatus;
import com.f3cinema.app.entity.enums.UserRole;
import com.f3cinema.app.repository.UserRepository;
import com.f3cinema.app.service.MovieService;
import com.f3cinema.app.service.GenreService;
import com.f3cinema.app.repository.GenreRepository;
import com.f3cinema.app.repository.GenreRepositoryImpl;
import com.f3cinema.app.util.PasswordUtil;
import lombok.extern.log4j.Log4j2;
import java.util.List;
import java.util.stream.Collectors;

import com.f3cinema.app.entity.Room;
import com.f3cinema.app.entity.enums.RoomType;
import com.f3cinema.app.service.RoomService;

/**
 * Seeder to initialize default system data (e.g., Admin account).
 */
@Log4j2
public class DatabaseSeeder {

    private static final UserRepository userRepository = new UserRepository();
    private static final MovieService movieService = MovieService.getInstance();
    private static final GenreService genreService = GenreService.getInstance();
    private static final GenreRepository genreRepository = new GenreRepositoryImpl();

    public static void seed() {
        try {
            seedUsers();
            seedMovies();
            seedRooms();
        } catch (Exception e) {
            log.error("Failed to seed database.", e);
        }
    }

    private static void seedUsers() {
        // Check if admin user exists
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = User.builder()
                    .username("admin")
                    .password(PasswordUtil.hash("1"))
                    .fullName("System Administrator")
                    .role(UserRole.ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Admin account seeded successfully: admin / 1");
        }

        // Check if staff user exists
        if (userRepository.findByUsername("staff").isEmpty()) {
            User staff = User.builder()
                    .username("staff")
                    .password(PasswordUtil.hash("1"))
                    .fullName("Cinema Staff 01")
                    .role(UserRole.STAFF)
                    .build();
            userRepository.save(staff);
            log.info("Staff account seeded successfully: staff / 1");
        }
    }

    private static void seedMovies() {
        if (genreService.getAllGenres().isEmpty()) {
            genreRepository.save(Genre.builder().name("Action").build());
            genreRepository.save(Genre.builder().name("Comedy").build());
            genreRepository.save(Genre.builder().name("Drama").build());
            genreRepository.save(Genre.builder().name("Horror").build());
            genreRepository.save(Genre.builder().name("Sci-Fi").build());
            genreRepository.save(Genre.builder().name("Animation").build());
            genreRepository.save(Genre.builder().name("Thriller").build());
            genreRepository.save(Genre.builder().name("Romance").build());
            log.info("Genres seeded successfully.");
        }

        List<Movie> existingMovies = movieService.getAllMovies();
        List<Genre> allGenres = genreService.getAllGenres();

        List<Genre> actionSciFi = allGenres.stream().filter(g -> g.getName().equals("Action") || g.getName().equals("Sci-Fi")).collect(Collectors.toList());
        List<Genre> drama = allGenres.stream().filter(g -> g.getName().equals("Drama")).collect(Collectors.toList());
        List<Genre> comedyAnim = allGenres.stream().filter(g -> g.getName().equals("Comedy") || g.getName().equals("Animation")).collect(Collectors.toList());

        if (existingMovies.isEmpty()) {
            movieService.addMovie(Movie.builder()
                .title("Avatar: The Way of Water")
                .duration(192)
                .status(MovieStatus.NOW_SHOWING)
                .posterUrl("https://image.tmdb.org/t/p/w500/t6S3vA3uunubvSt9sVvPbZ97Tht.jpg")
                .genres(actionSciFi)
                .build());
            
            movieService.addMovie(Movie.builder()
                .title("Oppenheimer")
                .duration(180)
                .status(MovieStatus.NOW_SHOWING)
                .posterUrl("https://image.tmdb.org/t/p/w500/8GxvynZTMBL1C9Sqo7a9D3S3GPm.jpg")
                .genres(drama)
                .build());

            movieService.addMovie(Movie.builder()
                .title("Dune: Part Two")
                .duration(166)
                .status(MovieStatus.NOW_SHOWING)
                .posterUrl("https://image.tmdb.org/t/p/w500/1pdfSTbt1t9SxDvWoV2as9399Ep.jpg")
                .genres(actionSciFi)
                .build());

            movieService.addMovie(Movie.builder()
                .title("Deadpool & Wolverine")
                .duration(127)
                .status(MovieStatus.COMING_SOON)
                .posterUrl("https://image.tmdb.org/t/p/w500/uxO9uov9id9ofYpU9DPh8699G9N.jpg")
                .genres(actionSciFi)
                .build());

            movieService.addMovie(Movie.builder()
                .title("Despicable Me 4")
                .duration(94)
                .status(MovieStatus.NOW_SHOWING)
                .posterUrl("https://image.tmdb.org/t/p/w500/wWba30Vzh9AnZ697H8Z7pY6H8Y6.jpg")
                .genres(comedyAnim)
                .build());

            log.info("Real movie data seeded with TMDB links and genres.");
        } else {
            for (Movie m : existingMovies) {
                if (m.getGenres() == null || m.getGenres().isEmpty()) {
                    String title = m.getTitle().toLowerCase();
                    if (title.contains("avatar") || title.contains("dune") || title.contains("deadpool") || title.contains("spider-man")) {
                        m.setGenres(actionSciFi);
                    } else if (title.contains("oppenheimer")) {
                        m.setGenres(drama);
                    } else if (title.contains("despicable")) {
                        m.setGenres(comedyAnim);
                    } else {
                        m.setGenres(actionSciFi);
                    }
                    movieService.updateMovie(m);
                }
            }
        }
    }

    private static void seedRooms() {
        RoomService roomService = new RoomService();
        if (roomService.getAllRooms().isEmpty()) {
            Room r1 = new Room(); r1.setName("Cinema 1 - IMAX"); r1.setRoomType(RoomType.ROOM_IMAX);
            roomService.saveRoomWithSeats(r1, 12, 16);
            
            Room r2 = new Room(); r2.setName("Cinema 2 - 3D"); r2.setRoomType(RoomType.ROOM_3D);
            roomService.saveRoomWithSeats(r2, 10, 12);
            
            Room r3 = new Room(); r3.setName("Cinema 3 - Standard"); r3.setRoomType(RoomType.ROOM_2D);
            roomService.saveRoomWithSeats(r3, 10, 14);
            log.info("Rooms and seats seeded successfully.");
        }
    }
}
