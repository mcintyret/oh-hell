package com.mcintyret.ohhell.card;

public enum Suit {
    HEARTS("H"),
    SPADES("S"),
    CLUBS("C"),
    DIAMONDS("D");

    private final String name;

    Suit(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
