package util;

import def.Card;

public abstract class PokerUtils {
    public static double getChenScore(Card[] cards) {
        if (cards.length != 2) {
            throw new IllegalArgumentException("Invalid number of cards: " + cards.length);
        }
        
        int rank1 = cards[0].getRank();
        int suit1 = cards[0].getSuit();
        int rank2 = cards[1].getRank();
        int suit2 = cards[1].getSuit();
        int highRank = Math.max(rank1, rank2);
        int lowRank = Math.min(rank1, rank2);
        int rankDiff = highRank - lowRank;
        int gap = (rankDiff > 1) ? rankDiff - 1 : 0;  
        boolean isPair = (rank1 == rank2);
        boolean isSuited = (suit1 == suit2);
        
        double score = 0.0;
        
        if (highRank == Card.ACE) {
            score = 10.0;
        } else if (highRank == Card.KING) {
            score = 8.0;
        } else if (highRank == Card.QUEEN) {
            score = 7.0;
        } else if (highRank == Card.JACK) {
            score = 6.0;
        } else {
            score = (highRank + 2) / 2.0;
        }
        
        if (isPair) {
            score *= 2.0;
            if (score < 5.0) {
                score = 5.0;
            }
        }
        
        if (isSuited) {
            score += 2.0;
        }
        
        if (gap == 1) {
            score -= 1.0;
        } else if (gap == 2) {
            score -= 2.0;
        } else if (gap == 3) {
            score -= 4.0;
        } else if (gap > 3) {
            score -= 5.0;
        }
        
        if (!isPair && gap < 2 && rank1 < Card.QUEEN && rank2 < Card.QUEEN) {
            score += 1.0;
        }
        
        if (score < 0.0) {
            score = 0.0;
        }
        
        return Math.round(score);        
    }

}
