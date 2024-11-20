package bots;

import java.util.List;
import java.util.Set;
import def.Card;
import def.Client;
import def.Player;
import def.TableType;
import actions.Action;
import actions.BetAction;
import actions.RaiseAction;
import util.PokerUtils;

public class BasicBot implements Client {
    
	protected static final int NO_OF_HOLE_CARDS = 2;
    private final int tightness;
    private final int aggression;
    private TableType tableType;
    private Card[] cards;
    
    public BasicBot(int tightness, int aggression) {
        if (tightness < 0 || tightness > 100) {
            throw new IllegalArgumentException("Invalid tightness setting");
        }
        if (aggression < 0 || aggression > 100) {
            throw new IllegalArgumentException("Invalid aggression setting");
        }
        this.tightness = tightness;
        this.aggression = aggression;
    }
    
    @Override
    public void joinedTable(TableType type, int bigBlind, List<Player> players) {
        this.tableType = type;
    }
    
    @Override
    public void messageReceived(String message) {
    }
    
    @Override
    public void handStarted(Player dealer) {
        cards = null;
    }
    
    @Override
    public void actorRotated(Player actor) {
    }
    
    @Override
    public void boardUpdated(List<Card> cards, int bet, int pot) {
    }
    
    @Override
    public void playerUpdated(Player player) {
        if (player.getCards().length == NO_OF_HOLE_CARDS) {
            this.cards = player.getCards();
        }
    }
    
    @Override
    public void playerActed(Player player) {
    }
    
    @Override
    public Action act(int minBet, int currentBet, Set<Action> allowedActions) {
        Action action = null;
        if (allowedActions.size() == 1) {
            action = Action.CHECK;
        } else {
            double chenScore = PokerUtils.getChenScore(cards);
            double chenScoreToPlay = tightness * 0.2;
            if ((chenScore < chenScoreToPlay)) {
                if (allowedActions.contains(Action.CHECK)) {
                    action = Action.CHECK;
                } else {
                    action = Action.FOLD;
                }
            } else {
                if ((chenScore - chenScoreToPlay) >= ((20.0 - chenScoreToPlay) / 2.0)) {
                    if (aggression == 0) {
                        if (allowedActions.contains(Action.CALL)) {
                            action = Action.CALL;
                        } else {
                            action = Action.CHECK;
                        }
                    } else if (aggression == 100) {
                        int amount = (tableType == TableType.FIXED_LIMIT) ? minBet : 100 * minBet;
                        if (allowedActions.contains(Action.BET)) {
                            action = new BetAction(amount);
                        } else if (allowedActions.contains(Action.RAISE)) {
                            action = new RaiseAction(amount);
                        } else if (allowedActions.contains(Action.CALL)) {
                            action = Action.CALL;
                        } else {
                            action = Action.CHECK;
                        }
                    } else {
                        int amount = minBet;
                        if (tableType == TableType.NO_LIMIT) {
                            int betLevel = aggression / 20;
                            for (int i = 0; i < betLevel; i++) {
                                amount *= 2;
                            }
                        }
                        if (currentBet < amount) {
                            if (allowedActions.contains(Action.BET)) {
                                action = new BetAction(amount);
                            } else if (allowedActions.contains(Action.RAISE)) {
                                action = new RaiseAction(amount);
                            } else if (allowedActions.contains(Action.CALL)) {
                                action = Action.CALL;
                            } else {
                                action = Action.CHECK;
                            }
                        } else {
                            if (allowedActions.contains(Action.CALL)) {
                                action = Action.CALL;
                            } else {
                                action = Action.CHECK;
                            }
                        }
                    }
                } else {
                    if (allowedActions.contains(Action.CHECK)) {
                        action = Action.CHECK;
                    } else {
                        action = Action.CALL;
                    }
                }
            }
        }
        return action;
    }
    
}
