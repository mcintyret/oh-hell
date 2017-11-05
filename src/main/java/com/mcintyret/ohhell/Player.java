package com.mcintyret.ohhell;

import com.mcintyret.ohhell.card.Card;

import java.util.List;

public interface Player {

    void setHand(List<Card> cards);

    List<Card> getHand();

    void notifyTrumpCard(Card trumpCard);

    int getBid();

    void notifyBid(int index, int bid);

    Card getCard();

    void notifyCard(int index, Card card);

    void notifyTrickWinner(int index);

    void notifyWinOrLose(boolean won, boolean joint);

}
