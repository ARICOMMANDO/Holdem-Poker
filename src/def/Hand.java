package def;

import java.util.Collection;

public class Hand {
    
    private static final int MAX_NO_OF_CARDS = 7;
    private Card[] cards = new Card[MAX_NO_OF_CARDS];
    private int noOfCards = 0;
    public Hand() {
    }
    
    public Hand(Card[] cards) {
        addCards(cards);
    }
    
    public Hand(Collection<Card> cards) {
        if (cards == null) {
            throw new IllegalArgumentException("Null array");
        }
        for (Card card : cards) {
            addCard(card);
        }
    }
    
    public Hand(String s) {
        if (s == null || s.length() == 0) {
            throw new IllegalArgumentException("Null or empty string");
        }
        
        String[] parts = s.split("\\s");
        if (parts.length > MAX_NO_OF_CARDS) {
            throw new IllegalArgumentException("Too many cards in hand");
        }
        for (String part : parts) {
            addCard(new Card(part));
        }
    }
    
    public int size() {
        return noOfCards;
    }
    
    public void addCard(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("Null card");
        }
        
        int insertIndex = -1;
        for (int i = 0; i < noOfCards; i++) {
            if (card.compareTo(cards[i]) > 0) {
                insertIndex = i;
                break;
            }
        }
        if (insertIndex == -1) {
            cards[noOfCards++] = card;
        } else {
            for (int i = noOfCards; i > insertIndex; i--) {
                cards[i] = cards[i - 1];
            }
            cards[insertIndex] = card;
            noOfCards++;
        }
    }
    
    public void addCards(Card[] cards) {
        if (cards == null) {
            throw new IllegalArgumentException("Null array");
        }
        if (cards.length > MAX_NO_OF_CARDS) {
            throw new IllegalArgumentException("Too many cards");
        }
        for (Card card : cards) {
            addCard(card);
        }
    }
    
    public void addCards(Collection<Card> cards) {
        if (cards == null) {
            throw new IllegalArgumentException("Null collection");
        }
        if (cards.size() > MAX_NO_OF_CARDS) {
            throw new IllegalArgumentException("Too many cards");
        }
        for (Card card : cards) {
            addCard(card);
        }
    }
    
    public Card[] getCards() {
        Card[] dest = new Card[noOfCards];
        System.arraycopy(cards, 0, dest, 0, noOfCards);
        return dest;
    }
    
    public void removeAllCards() {
        noOfCards = 0;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < noOfCards; i++) {
            sb.append(cards[i]);
            if (i < (noOfCards - 1)) {
                sb.append(' ');
            }
        }
        return sb.toString();
    }
    
}
