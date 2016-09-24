package com.example.menu;

import com.example.Factory;

@Factory(id = "Margherita", type = Meal.class)
public final class MargheritaPizza implements Meal {
    @Override
    public final float getPrice() {
        return 6f;
    }
}
