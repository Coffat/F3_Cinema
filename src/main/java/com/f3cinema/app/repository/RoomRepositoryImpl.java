package com.f3cinema.app.repository;

import com.f3cinema.app.entity.Room;

public class RoomRepositoryImpl extends BaseRepositoryImpl<Room, Long> implements RoomRepository {
    public RoomRepositoryImpl() {
        super(Room.class);
    }
}
