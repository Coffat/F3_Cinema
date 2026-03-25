package com.f3cinema.app.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MovieStatus {
    NOW_SHOWING("Now Showing"),
    COMING_SOON("Coming Soon"),
    ENDED("Ended");

    private final String label;
}
