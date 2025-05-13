package poker;

import other_stuff.SetOperations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static poker.Card.Rank.aceLow;

public class utils {

    public static final int smallBlind = 1;
    public static final int bigBlind = 2;

    public static boolean containsCard(List<Card> cards, Card.Rank rank, Card.Suit suit) {
        Card testCard = new Card(rank, suit);
        for (Card c : cards) {
            if (c.equals(testCard)) {
                return true;
            }
        }
        return false;
    }


    public static boolean isStraight(List<Card> cards) {
        if (cards.size() != 5) {
            throw new IllegalStateException();
        }

        List<Card> clone = new LinkedList<>(cards);
        Collections.sort(clone);

        boolean straight = true;
        for (int i = 0; i < clone.size() - 1; i++) {
            if (clone.get(i).compareTo(clone.get(i + 1)) != -1) {
                straight = false;
                break;
            }
        }

        if (!straight && clone.get(4).getRank() == Card.Rank.ace) {
            clone.set(4, new Card(clone.get(4).getSuit(), aceLow));
            straight = isStraight(clone);
        }

        return straight;
    }


    public static boolean isFlush(List<Card> cards) {
        boolean allHearts = true, allSpades = true, allDiamonds = true, allClubs = true;
        for (Card c : cards) {
            if (c.getSuit() != Card.Suit.hearts) allHearts = false;
            if (c.getSuit() != Card.Suit.spades) allSpades = false;
            if (c.getSuit() != Card.Suit.diamonds) allDiamonds = false;
            if (c.getSuit() != Card.Suit.clubs) allClubs = false;
        }
        return (allSpades || allDiamonds || allHearts || allClubs);
    }


    public static List<Hand> getAllHands(List<Card> cards) {
        List<Hand> fiveCardHands = new ArrayList<>();

        for (List<Card> set : SetOperations.powerSet(cards.toArray(new Card[0]))) {
            if (set.size() == 5) {
                fiveCardHands.add(new Hand(set));
            }
        }

        return fiveCardHands;
    }
}
