package com.f3cinema.app.entity;

import com.f3cinema.app.entity.enums.RoomType;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private RoomType roomType;

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    private List<Seat> seats;
}
