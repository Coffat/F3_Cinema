package com.f3cinema.app.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoomType {
    ROOM_2D("2D Standard"),
    ROOM_3D("3D Immersion"),
    ROOM_IMAX("IMAX Ultimate");

    private final String label;
}
