package def;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import actions.Action;
import actions.BetAction;
import actions.RaiseAction;

public class Table {
    
    private static final int MAX_RAISES = 3;
    private static final boolean ALWAYS_CALL_SHOWDOWN = false;
    private final TableType tableType;
    private final int bigBlind;
    private final List<Player> players;
    private final List<Player> activePlayers;
    private final Deck deck;
    private final List<Card> board;
    private int dealerPosition;
    private Player dealer;
    private int actorPosition;
    private Player actor;
    private int minBet;
    private int bet;
    private final List<Pot> pots;
    private Player lastBettor;
    private int raises;
    
    public Table(TableType type, int bigBlind) {
        this.tableType = type;
        this.bigBlind = bigBlind;
        players = new ArrayList<Player>();
        activePlayers = new ArrayList<Player>();
        deck = new Deck();
        board = new ArrayList<Card>();
        pots = new ArrayList<Pot>();
    }
    
    public void addPlayer(Player player) {
        players.add(player);
    }
    
    public void run() {
        for (Player player : players) {
            player.getClient().joinedTable(tableType, bigBlind, players);
        }
        dealerPosition = -1;
        actorPosition = -1;
        while (true) {
            int noOfActivePlayers = 0;
            for (Player player : players) {
                if (player.getCash() >= bigBlind) {
                    noOfActivePlayers++;
                }
            }
            if (noOfActivePlayers > 1) {
                playHand();
            } else {
                break;
            }
        }
        
        board.clear();
        pots.clear();
        bet = 0;
        notifyBoardUpdated();
        for (Player player : players) {
            player.resetHand();
        }
        notifyPlayersUpdated(false);
        notifyMessage("Game over.");
    }
    
    private void playHand() {
        resetHand();
        
        if (activePlayers.size() > 2) {
            rotateActor();
        }
        postSmallBlind();
        
        rotateActor();
        postBigBlind();
        
        dealHoleCards();
        doBettingRound();
        
        if (activePlayers.size() > 1) {
            bet = 0;
            dealCommunityCards("Flop", 3);
            minBet = bigBlind;
            doBettingRound();

            if (activePlayers.size() > 1) {
                bet = 0;
                dealCommunityCards("Turn", 1);
                if (tableType == TableType.FIXED_LIMIT) {
                    minBet = 2 * bigBlind;
                } else {
                    minBet = bigBlind;
                }
                doBettingRound();

                if (activePlayers.size() > 1) {
                    bet = 0;
                    dealCommunityCards("River", 1);
                    if (tableType == TableType.FIXED_LIMIT) {
                        minBet = 2 * bigBlind;
                    } else {
                        minBet = bigBlind;
                    }
                    doBettingRound();

                    if (activePlayers.size() > 1) {
                        bet = 0;
                        minBet = bigBlind;
                        doShowdown();
                    }
                }
            }
        }
    }
    
    private void resetHand() {
        board.clear();
        pots.clear();
        notifyBoardUpdated();
        
        activePlayers.clear();
        for (Player player : players) {
            player.resetHand();
            if (player.getCash() >= bigBlind) {
                activePlayers.add(player);
            }
        }
        
        dealerPosition = (dealerPosition + 1) % activePlayers.size();
        dealer = activePlayers.get(dealerPosition);

        deck.shuffle();

        actorPosition = dealerPosition;
        actor = activePlayers.get(actorPosition);
        
        minBet = bigBlind;
        bet = minBet;
        
        for (Player player : players) {
            player.getClient().handStarted(dealer);
        }
        notifyPlayersUpdated(false);
        notifyMessage("New hand, %s is the dealer.", dealer);
    }

    private void rotateActor() {
        actorPosition = (actorPosition + 1) % activePlayers.size();
        actor = activePlayers.get(actorPosition);
        for (Player player : players) {
            player.getClient().actorRotated(actor);
        }
    }
    
    private void postSmallBlind() {
        final int smallBlind = bigBlind / 2;
        actor.postSmallBlind(smallBlind);
        contributePot(smallBlind);
        notifyBoardUpdated();
        notifyPlayerActed();
    }
    
    private void postBigBlind() {
        actor.postBigBlind(bigBlind);
        contributePot(bigBlind);
        notifyBoardUpdated();
        notifyPlayerActed();
    }
    
    private void dealHoleCards() {
        for (Player player : activePlayers) {
            player.setCards(deck.deal(2));
        }
        System.out.println();
        notifyPlayersUpdated(false);
        notifyMessage("%s deals the hole cards.", dealer);
    }
    
    private void dealCommunityCards(String phaseName, int noOfCards) {
        for (int i = 0; i < noOfCards; i++) {
            board.add(deck.deal());
        }
        notifyPlayersUpdated(false);
        notifyMessage("%s deals the %s.", dealer, phaseName);
    }
    
    private void doBettingRound() {
        int playersToAct = activePlayers.size();
        if (board.size() == 0) {
            bet = bigBlind;
        } else {
            actorPosition = dealerPosition;
            bet = 0;
        }
        
        lastBettor = null;
        raises = 0;
        notifyBoardUpdated();
        
        while (playersToAct > 0) {
            rotateActor();
            Action action = null;
            if (actor.isAllIn()) {
                action = Action.CHECK;
                playersToAct--;
            } else {
                Set<Action> allowedActions = getAllowedActions(actor);
                action = actor.getClient().act(minBet, bet, allowedActions);
                if (!allowedActions.contains(action)) {
                    if (!(action instanceof BetAction && allowedActions.contains(Action.BET)) && !(action instanceof RaiseAction && allowedActions.contains(Action.RAISE))) {
                        throw new IllegalStateException(String.format("Player '%s' acted with illegal %s action", actor, action));
                    }
                }
                playersToAct--;
                if (action == Action.CHECK) {
                } else if (action == Action.CALL) {
                    int betIncrement = bet - actor.getBet();
                    if (betIncrement > actor.getCash()) {
                        betIncrement = actor.getCash();
                    }
                    actor.payCash(betIncrement);
                    actor.setBet(actor.getBet() + betIncrement);
                    contributePot(betIncrement);
                } else if (action instanceof BetAction) {
                    int amount = (tableType == TableType.FIXED_LIMIT) ? minBet : action.getAmount();
                    if (amount < minBet && amount < actor.getCash()) {
                        throw new IllegalStateException("Illegal client action: bet less than minimum bet!");
                    }
                    if (amount > actor.getCash() && actor.getCash() >= minBet) {
                        throw new IllegalStateException("Illegal client action: bet more cash than you own!");
                    }
                    bet = amount;
                    minBet = amount;
                    int betIncrement = bet - actor.getBet();
                    if (betIncrement > actor.getCash()) {
                        betIncrement = actor.getCash();
                    }
                    actor.setBet(bet);
                    actor.payCash(betIncrement);
                    contributePot(betIncrement);
                    lastBettor = actor;
                    playersToAct = (tableType == TableType.FIXED_LIMIT) ? activePlayers.size() : (activePlayers.size() - 1);
                } else if (action instanceof RaiseAction) {
                    int amount = (tableType == TableType.FIXED_LIMIT) ? minBet : action.getAmount();
                    if (amount < minBet && amount < actor.getCash()) {
                        throw new IllegalStateException("Illegal client action: raise less than minimum bet!");
                    }
                    if (amount > actor.getCash() && actor.getCash() >= minBet) {
                        throw new IllegalStateException("Illegal client action: raise more cash than you own!");
                    }
                    bet += amount;
                    minBet = amount;
                    int betIncrement = bet - actor.getBet();
                    if (betIncrement > actor.getCash()) {
                        betIncrement = actor.getCash();
                    }
                    actor.setBet(bet);
                    actor.payCash(betIncrement);
                    contributePot(betIncrement);
                    lastBettor = actor;
                    raises++;
                    if (tableType == TableType.FIXED_LIMIT && (raises < MAX_RAISES || activePlayers.size() == 2)) {
                        playersToAct = activePlayers.size();
                    } else {
                        playersToAct = activePlayers.size() - 1;
                    }
                } else if (action == Action.FOLD) {
                    actor.setCards(null);
                    activePlayers.remove(actor);
                    actorPosition--;
                    if (activePlayers.size() == 1) {
                        notifyBoardUpdated();
                        notifyPlayerActed();
                        Player winner = activePlayers.get(0);
                        int amount = getTotalPot();
                        winner.win(amount);
                        notifyBoardUpdated();
                        notifyMessage("%s wins $ %d.", winner, amount);
                        playersToAct = 0;
                    }
                } else {
                    throw new IllegalStateException("Invalid action: " + action);
                }
            }
            actor.setAction(action);
            if (activePlayers.size() > 1) {
                notifyBoardUpdated();
                notifyPlayerActed();
            }
        }
        
        for (Player player : activePlayers) {
            player.resetBet();
        }
        notifyBoardUpdated();
        notifyPlayersUpdated(false);
    }
    
    private Set<Action> getAllowedActions(Player player) {
        Set<Action> actions = new HashSet<Action>();
        if (player.isAllIn()) {
            actions.add(Action.CHECK);
        } else {
            int actorBet = actor.getBet();
            if (bet == 0) {
                actions.add(Action.CHECK);
                if (tableType == TableType.NO_LIMIT || raises < MAX_RAISES || activePlayers.size() == 2) {
                    actions.add(Action.BET);
                }
            } else {
                if (actorBet < bet) {
                    actions.add(Action.CALL);
                    if (tableType == TableType.NO_LIMIT || raises < MAX_RAISES || activePlayers.size() == 2) {
                        actions.add(Action.RAISE);
                    }
                } else {
                    actions.add(Action.CHECK);
                    if (tableType == TableType.NO_LIMIT || raises < MAX_RAISES || activePlayers.size() == 2) {
                        actions.add(Action.RAISE);
                    }
                }
            }
            actions.add(Action.FOLD);
        }
        return actions;
    }
    
    private void contributePot(int amount) {
        for (Pot pot : pots) {
            if (!pot.hasContributer(actor)) {
                int potBet = pot.getBet();
                if (amount >= potBet) {
                    pot.addContributer(actor);
                    amount -= pot.getBet();
                } else {
                    pots.add(pot.split(actor, amount));
                    amount = 0;
                }
            }
            if (amount <= 0) {
                break;
            }
        }
        if (amount > 0) {
            Pot pot = new Pot(amount);
            pot.addContributer(actor);
            pots.add(pot);
        }
    }
    
    private void doShowdown() {
        List<Player> showingPlayers = new ArrayList<Player>();
        for (Pot pot : pots) {
            for (Player contributor : pot.getContributors()) {
                if (!showingPlayers.contains(contributor) && contributor.isAllIn()) {
                    showingPlayers.add(contributor);
                }
            }
        }
        if (lastBettor != null) {
            if (!showingPlayers.contains(lastBettor)) {
                showingPlayers.add(lastBettor);
            }
        }
        int pos = (dealerPosition + 1) % activePlayers.size();
        while (showingPlayers.size() < activePlayers.size()) {
            Player player = activePlayers.get(pos);
            if (!showingPlayers.contains(player)) {
                showingPlayers.add(player);
            }
            pos = (pos + 1) % activePlayers.size();
        }
        
        boolean firstToShow = true;
        int bestHandValue = -1;
        for (Player playerToShow : showingPlayers) {
            Hand hand = new Hand(board);
            hand.addCards(playerToShow.getCards());
            HandValue handValue = new HandValue(hand);
            boolean doShow = ALWAYS_CALL_SHOWDOWN;
            if (!doShow) {
                if (playerToShow.isAllIn()) {
                    doShow = true;
                    firstToShow = false;
                } else if (firstToShow) {
                    doShow = true;
                    bestHandValue = handValue.getValue();
                    firstToShow = false;
                } else {
                    if (handValue.getValue() >= bestHandValue) {
                        doShow = true;
                        bestHandValue = handValue.getValue();
                    }
                }
            }
            if (doShow) {
                for (Player player : players) {
                    player.getClient().playerUpdated(playerToShow);
                }
                notifyMessage("%s has %s.", playerToShow, handValue.getDescription());
            } else {
                playerToShow.setCards(null);
                activePlayers.remove(playerToShow);
                for (Player player : players) {
                    if (player.equals(playerToShow)) {
                        player.getClient().playerUpdated(playerToShow);
                    } else {
                        player.getClient().playerUpdated(playerToShow.publicClone());
                    }
                }
                notifyMessage("%s folds.", playerToShow);
            }
        }
        
        Map<HandValue, List<Player>> rankedPlayers = new TreeMap<HandValue, List<Player>>();
        for (Player player : activePlayers) {
            Hand hand = new Hand(board);
            hand.addCards(player.getCards());
            HandValue handValue = new HandValue(hand);
            List<Player> playerList = rankedPlayers.get(handValue);
            if (playerList == null) {
                playerList = new ArrayList<Player>();
            }
            playerList.add(player);
            rankedPlayers.put(handValue, playerList);
        }

        int totalPot = getTotalPot();
        Map<Player, Integer> potDivision = new HashMap<Player, Integer>();
        for (HandValue handValue : rankedPlayers.keySet()) {
            List<Player> winners = rankedPlayers.get(handValue);
            for (Pot pot : pots) {
                int noOfWinnersInPot = 0;
                for (Player winner : winners) {
                    if (pot.hasContributer(winner)) {
                        noOfWinnersInPot++;
                    }
                }
                if (noOfWinnersInPot > 0) {
                    int potShare = pot.getValue() / noOfWinnersInPot;
                    for (Player winner : winners) {
                        if (pot.hasContributer(winner)) {
                            Integer oldShare = potDivision.get(winner);
                            if (oldShare != null) {
                                potDivision.put(winner, oldShare + potShare);
                            } else {
                                potDivision.put(winner, potShare);
                            }
                            
                        }
                    }
                    int oddChips = pot.getValue() % noOfWinnersInPot;
                    if (oddChips > 0) {
                        pos = dealerPosition;
                        while (oddChips > 0) {
                            pos = (pos + 1) % activePlayers.size();
                            Player winner = activePlayers.get(pos);
                            Integer oldShare = potDivision.get(winner);
                            if (oldShare != null) {
                                potDivision.put(winner, oldShare + 1);
                                oddChips--;
                            }
                        }
                        
                    }
                    pot.clear();
                }
            }
        }
        
        StringBuilder winnerText = new StringBuilder();
        int totalWon = 0;
        for (Player winner : potDivision.keySet()) {
            int potShare = potDivision.get(winner);
            winner.win(potShare);
            totalWon += potShare;
            if (winnerText.length() > 0) {
                winnerText.append(", ");
            }
            winnerText.append(String.format("%s wins $ %d", winner, potShare));
            notifyPlayersUpdated(true);
        }
        winnerText.append('.');
        notifyMessage(winnerText.toString());
        
        if (totalWon != totalPot) {
            throw new IllegalStateException("Incorrect pot division!");
        }
    }
    
    private void notifyMessage(String message, Object... args) {
        message = String.format(message, args);
        for (Player player : players) {
            player.getClient().messageReceived(message);
        }
    }
    
    private void notifyBoardUpdated() {
        int pot = getTotalPot();
        for (Player player : players) {
            player.getClient().boardUpdated(board, bet, pot);
        }
    }
    
    private int getTotalPot() {
        int totalPot = 0;
        for (Pot pot : pots) {
            totalPot += pot.getValue();
        }
        return totalPot;
    }

    private void notifyPlayersUpdated(boolean showdown) {
        for (Player playerToNotify : players) {
            for (Player player : players) {
                if (!showdown && !player.equals(playerToNotify)) {
                    player = player.publicClone();
                }
                playerToNotify.getClient().playerUpdated(player);
            }
        }
    }
    
    private void notifyPlayerActed() {
        for (Player p : players) {
            Player playerInfo = p.equals(actor) ? actor : actor.publicClone();
            p.getClient().playerActed(playerInfo);
        }
    }
    
}
