package def;

import java.util.List;
import java.util.Set;
import actions.Action;

public interface Client {
    
    void messageReceived(String message);

    void joinedTable(TableType type, int bigBlind, List<Player> players);
    
    void handStarted(Player dealer);
    
    void actorRotated(Player actor);
    
    void playerUpdated(Player player);
    
    void boardUpdated(List<Card> cards, int bet, int pot);
    
    void playerActed(Player player);

    Action act(int minBet, int currentBet, Set<Action> allowedActions);

}
