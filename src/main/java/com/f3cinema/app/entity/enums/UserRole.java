package com.f3cinema.app.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    ADMIN("Administrator"),
    STAFF("Staff Member");

    private final String label;
}
