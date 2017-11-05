package com.mcintyret.ohhell;

import com.mcintyret.ohhell.card.Card;
import com.mcintyret.ohhell.card.Deck;
import com.mcintyret.ohhell.card.Suit;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

public class OhHell {

    private static final int MAX_PLAYERS = 6;

    private static final int MATCHED_BID_BONUS = 10;

    private final List<Player> players;

    private final Deck deck = new Deck();

    private int activePlayerIndex = 0;

    private final int[] bids;
    private final int[] tricks;
    private final int[] scores;

    private Card trumpCard;

    public OhHell(List<Player> players) {
        if (players.size() > MAX_PLAYERS) {
            throw new IllegalArgumentException("Too many players. Max = " + MAX_PLAYERS);
        }
        this.players = players;
        this.activePlayerIndex = 0;
        this.bids = new int[players.size()];
        this.tricks = new int[players.size()];
        this.scores = new int[players.size()];
    }

    public void playGame() {
        int cardsInFirstRound = Math.min(10, 52 / players.size());
        for (int i = cardsInFirstRound; i >= 1; i--) {
            playRound(i);
        }
        declareWinners();
    }

    private void declareWinners() {
        Set<Integer> winIndices = findWinIndices();
        boolean joint = winIndices.size() > 1;
        forEachPlayer((player, i) -> {
            player.notifyWinOrLose(winIndices.contains(i), joint);
        });
    }

    private Set<Integer> findWinIndices() {
        int winScore = Integer.MIN_VALUE;
        Set<Integer> winIndices = new HashSet<>();
        for (int i = 0; i < scores.length; i++) {
            int score = scores[i];
            if (score >= winScore) {
                if (score > winScore) {
                    winIndices.clear();
                }
                winIndices.add(i);
            }
        }
        return winIndices;
    }

    private void playRound(int cardsPerPlayer) {
        deck.reset();
        deal(cardsPerPlayer);
        dealTrumpCard();
        gatherBids();
        validateBids(cardsPerPlayer);
        for (int i = 0; i < cardsPerPlayer; i++) {
            playTrick();
        }
        validateTricks(cardsPerPlayer);
        updateScores();
    }

    private void updateScores() {
        for (int i = 0; i < players.size(); i++) {
            int bid = bids[i];
            int tricksWon = tricks[i];
            int score = tricksWon;
            if (tricksWon == bid) {
                score += MATCHED_BID_BONUS;
            }
            scores[i] += score;
        }
    }

    private void validateTricks(int expectedTricks) {
        if (IntStream.of(tricks).sum() != expectedTricks) {
            throw new IllegalStateException("Unexpected error - should have " + expectedTricks + " tricks");
        }
    }

    private void playTrick() {
        Card[] cards = new Card[players.size()];
        AtomicReference<Suit> ledSuit = new AtomicReference<>();
        forEachPlayer((player, index) -> {
            Card playerCard = player.getCard();
            for (int i = 0; i < players.size(); i++) {
                if (i != index) {
                    players.get(i).notifyCard(index, playerCard);
                }
            }
            if (!ledSuit.compareAndSet(null, playerCard.getSuit())) {
                validateSuit(player, ledSuit.get(), playerCard);
            }
            cards[index] = playerCard;
        });
        int winIndex = getWinIndex(cards, ledSuit.get());
        tricks[winIndex]++;
        players.forEach(player -> player.notifyTrickWinner(winIndex));
    }

    private int getWinIndex(Card[] cards, Suit ledSuit) {
        boolean trumpPlayed = false;
        int winRank = Integer.MIN_VALUE;
        int winRankIndex = Integer.MIN_VALUE;

        for (int i = 0; i < cards.length; i++) {
            Card card = cards[i];
            if (card.getSuit() == trumpCard.getSuit()) {
                if (card.getRank().ordinal() > winRank) {
                    winRank = card.getRank().ordinal();
                    winRankIndex = i;
                    trumpPlayed = true;
                }
            } else if (card.getSuit() == ledSuit && !trumpPlayed) {
                if (card.getRank().ordinal() > winRank) {
                    winRank = card.getRank().ordinal();
                    winRankIndex = i;
                }
            }
        }
        return winRankIndex;
    }

    private static void validateSuit(Player player, Suit ledSuit, Card playerCard) {
        if (playerCard.getSuit() != ledSuit) {
            for (Card card : player.getHand()) {
                if (card.getSuit() == ledSuit) {
                    throw new IllegalArgumentException("Player did not follow suit");
                }
            }
        }
    }

    private void validateBids(int tricks) {
        if (IntStream.of(bids).sum() == tricks) {
            throw new IllegalStateException("Total bid cannot equal number of tricks");
        }
    }

    private void dealTrumpCard() {
        trumpCard = deck.deal();
        forEachPlayer((player, i) -> player.notifyTrumpCard(trumpCard));
    }

    private void gatherBids() {
        forEachPlayer((player, index) -> {
            int playerBid = player.getBid();
            if (playerBid < 0) {
                throw new IllegalArgumentException("Invalid bid: " + playerBid);
            }
            for (int i = 0; i < players.size(); i++) {
                if (i != index) {
                    players.get(i).notifyBid(index, playerBid);
                }
            }
            bids[index] = playerBid;
        });
    }

    private void deal(int cardsPerPlayer) {
        // The active player is the dealer - but it's the next player that is the first to get any cards
        incrementActivePlayerIndex();
        forEachPlayer((player, i) -> player.setHand(deck.deal(cardsPerPlayer)));
    }

    private void forEachPlayer(BiConsumer<Player, Integer> callback) {
        for (int i = 0; i < players.size(); i++) {
            callback.accept(players.get(activePlayerIndex), activePlayerIndex);
            incrementActivePlayerIndex();
        }
    }

    private void incrementActivePlayerIndex() {
        activePlayerIndex++;
        if (activePlayerIndex == players.size()) {
            activePlayerIndex = 0;
        }
    }
}
