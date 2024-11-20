package gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JFrame;
import def.Card;
import def.Client;
import def.Player;
import def.Table;
import def.TableType;
import actions.Action;
import bots.BasicBot;

public class Main extends JFrame implements Client {
    
    private static final TableType TABLE_TYPE = TableType.NO_LIMIT;
    private static final int BIG_BLIND = 10;
    private static final int STARTING_CASH = 500;
    private final Table table;
    private final Map<String, Player> players;
    private final GridBagConstraints gc;
    private final BoardPanel boardPanel;
    private final ControlPanel controlPanel;
    private final Map<String, PlayerPanel> playerPanels;
    private final Player humanPlayer;
    private String dealerName; 
    private String actorName; 

    public Main() {
        super("Texas Hold'em poker");
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(UIConstants.TABLE_COLOR);
        setLayout(new GridBagLayout());

        gc = new GridBagConstraints();
        
        controlPanel = new ControlPanel(TABLE_TYPE);
        
        boardPanel = new BoardPanel(controlPanel);        
        addComponent(boardPanel, 1, 1, 1, 1);
        
        players = new LinkedHashMap<String, Player>();
        humanPlayer = new Player("Player", STARTING_CASH, this);
        players.put("Player", humanPlayer);
        players.put("Joe",    new Player("Joe",   STARTING_CASH, new BasicBot(0, 75)));
        players.put("Mike",   new Player("Mike",  STARTING_CASH, new BasicBot(25, 50)));
        players.put("Eddie",  new Player("Eddie", STARTING_CASH, new BasicBot(50, 25)));

        table = new Table(TABLE_TYPE, BIG_BLIND);
        for (Player player : players.values()) {
            table.addPlayer(player);
        }
        
        playerPanels = new HashMap<String, PlayerPanel>();
        int i = 0;
        for (Player player : players.values()) {
            PlayerPanel panel = new PlayerPanel();
            playerPanels.put(player.getName(), panel);
            switch (i++) {
                case 0:
                    addComponent(panel, 1, 0, 1, 1);
                    break;
                case 1:
                    addComponent(panel, 2, 1, 1, 1);
                    break;
                case 2:
                    addComponent(panel, 1, 2, 1, 1);
                    break;
                case 3:
                    addComponent(panel, 0, 1, 1, 1);
                    break;
                default:
            }
        }
        
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);

        table.run();
    }
    
    public static void main(String[] args) {
        new Main();
    }

    @Override
    public void joinedTable(TableType type, int bigBlind, List<Player> players) {
        for (Player player : players) {
            PlayerPanel playerPanel = playerPanels.get(player.getName());
            if (playerPanel != null) {
                playerPanel.update(player);
            }
        }
    }

    @Override
    public void messageReceived(String message) {
        boardPanel.setMessage(message);
        boardPanel.waitForUserInput();
    }

    @Override
    public void handStarted(Player dealer) {
        setDealer(false);
        dealerName = dealer.getName();
        setDealer(true);
    }

    @Override
    public void actorRotated(Player actor) {
        setActorInTurn(false);
        actorName = actor.getName();
        setActorInTurn(true);
    }

    @Override
    public void boardUpdated(List<Card> cards, int bet, int pot) {
        boardPanel.update(cards, bet, pot);
    }

    @Override
    public void playerUpdated(Player player) {
        PlayerPanel playerPanel = playerPanels.get(player.getName());
        if (playerPanel != null) {
            playerPanel.update(player);
        }
    }

    @Override
    public void playerActed(Player player) {
        String name = player.getName();
        PlayerPanel playerPanel = playerPanels.get(name);
        if (playerPanel != null) {
            playerPanel.update(player);
            Action action = player.getAction();
            if (action != null) {
                boardPanel.setMessage(String.format("%s %s.", name, action.getVerb()));
                if (player.getClient() != this) {
                    boardPanel.waitForUserInput();
                }
            }
        } else {
            throw new IllegalStateException(
                    String.format("No PlayerPanel found for player '%s'", name));
        }
    }

    @Override
    public Action act(int minBet, int currentBet, Set<Action> allowedActions) {
        boardPanel.setMessage("Please select an action:");
        return controlPanel.getUserInput(minBet, humanPlayer.getCash(), allowedActions);
    }

    private void addComponent(Component component, int x, int y, int width, int height) {
        gc.gridx = x;
        gc.gridy = y;
        gc.gridwidth = width;
        gc.gridheight = height;
        gc.anchor = GridBagConstraints.CENTER;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0.0;
        gc.weighty = 0.0;
        getContentPane().add(component, gc);
    }

    private void setActorInTurn(boolean isInTurn) {
        if (actorName != null) {
            PlayerPanel playerPanel = playerPanels.get(actorName);
            if (playerPanel != null) {
                playerPanel.setInTurn(isInTurn);
            }
        }
    }

    private void setDealer(boolean isDealer) {
        if (dealerName != null) {
            PlayerPanel playerPanel = playerPanels.get(dealerName);
            if (playerPanel != null) {
                playerPanel.setDealer(isDealer);
            }
        }
    }

}
