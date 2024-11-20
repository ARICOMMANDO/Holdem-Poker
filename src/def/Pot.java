package def;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Pot {

    private int bet;
    public final Set<Player> contributors;
    
    public Pot(int bet) {
        this.bet = bet;
        contributors = new HashSet<Player>();
    }

    public int getBet() {
        return bet;
    }
    
    public Set<Player> getContributors() {
        return Collections.unmodifiableSet(contributors);
    }

    public void addContributer(Player player) {
        contributors.add(player);
    }

    public boolean hasContributer(Player player) {
        return contributors.contains(player);
    }

    public int getValue() {
        return bet * contributors.size();
    }

    public Pot split(Player player, int partialBet) {
        Pot pot = new Pot(bet - partialBet);
        for (Player contributer : contributors) {
            pot.addContributer(contributer);
        }
        bet = partialBet;
        contributors.add(player);
        return pot;
    }

    public void clear() {
        bet = 0;
        contributors.clear();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.valueOf(bet));
        sb.append(": {");
        boolean isFirst = true;
        for (Player contributer : contributors) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(", ");
            }
            sb.append(contributer.getName());
        }
        sb.append('}');
        sb.append(" (Total: ");
        sb.append(String.valueOf(getValue()));
        sb.append(')');
        return sb.toString();
    }

}
