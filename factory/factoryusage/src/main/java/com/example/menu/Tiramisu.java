package com.example.menu;

import com.example.Factory;

@Factory(
        id = "Tiramisu",
        type = Meal.class
)
public final class Tiramisu implements Meal {

    @Override
    public final float getPrice() {
        return 4.5f;
    }
}