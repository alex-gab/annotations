package com.test;

import com.example.annotation.Immutable;

@Immutable
public class MutableClass {
    private String name;

    public MutableClass(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
