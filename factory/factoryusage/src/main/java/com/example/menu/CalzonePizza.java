package com.example.menu;

import com.example.Factory;

@Factory(
        id = "Calzone",
        type = Meal.class
)
public final class CalzonePizza implements Meal {

    @Override
    public final float getPrice() {
        return 8.5f;
    }
}