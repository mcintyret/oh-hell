package com.mcintyret.ohhell.card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {

    private final List<Card> cards = new ArrayList<Card>();

    private int index;

    public Deck() {
        for (Rank rank: Rank.values()) {
            for (Suit suit: Suit.values()) {
                cards.add(new Card(suit, rank));
            }
        }
        index = 0;
    }

    public Deck shuffle() {
        Collections.shuffle(cards);
        return this;
    }

    public Card deal() {
        checkIndex(index);
        return cards.get(index++);
    }

    public List<Card> deal(int howMany) {
        if (howMany == 0) {
            return Collections.emptyList();
        }
        if (howMany == 1) {
            return Collections.singletonList(deal());
        }
        final int newIndex = this.index + howMany;
        checkIndex(newIndex);
        List<Card> dealt = new ArrayList<>(howMany);
        for (; index <= newIndex; index++) {
            dealt.add(cards.get(index));
        }
        return dealt;
    }

    private void checkIndex(int index) {
        if (index >= cards.size()) {
            throw new IllegalStateException();
        }
    }

    public int size() {
        return cards.size() - index;
    }


    public Deck reset() {
        index = 0;
        return this;
    }
}
