package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import def.Card;

public class BoardPanel extends JPanel {
    
    private static final int NO_OF_CARDS = 5;
    private final ControlPanel controlPanel;
    private final JLabel betLabel;
    private final JLabel potLabel;
    private final JLabel[] cardLabels;
    private final JLabel messageLabel;
    
    public BoardPanel(ControlPanel controlPanel) {
        this.controlPanel = controlPanel;
        
        setBorder(UIConstants.PANEL_BORDER);
        setBackground(UIConstants.TABLE_COLOR);
        setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        
        JLabel label = new JLabel("Bet");
        label.setForeground(Color.GREEN);
        gc.gridx = 1;
        gc.gridy = 0;
        gc.gridwidth = 1;
        gc.gridheight = 1;
        gc.anchor = GridBagConstraints.CENTER;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 1.0;
        gc.weighty = 0.0;
        gc.insets = new Insets(0, 5, 0, 5);
        add(label, gc);
        
        label = new JLabel("Pot");
        label.setForeground(Color.GREEN);
        gc.gridx = 3;
        gc.gridy = 0;
        gc.gridwidth = 1;
        gc.gridheight = 1;
        gc.anchor = GridBagConstraints.CENTER;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 1.0;
        gc.weighty = 0.0;
        gc.insets = new Insets(0, 5, 0, 5);
        add(label, gc);
        
        betLabel = new JLabel(" ");
        betLabel.setBorder(UIConstants.LABEL_BORDER);
        betLabel.setForeground(Color.GREEN);
        betLabel.setHorizontalAlignment(JLabel.CENTER);
        gc.gridx = 1;
        gc.gridy = 1;
        gc.gridwidth = 1;
        gc.gridheight = 1;
        gc.anchor = GridBagConstraints.CENTER;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.weighty = 0.0;
        gc.insets = new Insets(5, 5, 5, 5);
        add(betLabel, gc);

        potLabel = new JLabel(" ");
        potLabel.setBorder(UIConstants.LABEL_BORDER);
        potLabel.setForeground(Color.GREEN);
        potLabel.setHorizontalAlignment(JLabel.CENTER);
        gc.gridx = 3;
        gc.gridy = 1;
        gc.gridwidth = 1;
        gc.gridheight = 1;
        gc.anchor = GridBagConstraints.CENTER;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.weighty = 0.0;
        gc.insets = new Insets(5, 5, 5, 5);
        add(potLabel, gc);

        cardLabels = new JLabel[NO_OF_CARDS];
        for (int i = 0; i < 5; i++) {
            cardLabels[i] = new JLabel(ResourceManager.getIcon("/images/card_placeholder.png"));
            gc.gridx = i;
            gc.gridy = 2;
            gc.gridwidth = 1;
            gc.gridheight = 1;
            gc.anchor = GridBagConstraints.CENTER;
            gc.fill = GridBagConstraints.NONE;
            gc.weightx = 0.0;
            gc.weighty = 0.0;
            gc.insets = new Insets(5, 1, 5, 1);
            add(cardLabels[i], gc);
        }
        
        messageLabel = new JLabel();
        messageLabel.setForeground(Color.YELLOW);
        messageLabel.setHorizontalAlignment(JLabel.CENTER);
        gc.gridx = 0;
        gc.gridy = 3;
        gc.gridwidth = 5;
        gc.gridheight = 1;
        gc.anchor = GridBagConstraints.CENTER;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.weighty = 1.0;
        gc.insets = new Insets(0, 0, 0, 0);
        add(messageLabel, gc);
        
        gc.gridx = 0;
        gc.gridy = 4;
        gc.gridwidth = 5;
        gc.gridheight = 1;
        gc.insets = new Insets(0, 0, 0, 0);
        gc.anchor = GridBagConstraints.CENTER;
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1.0;
        gc.weighty = 1.0;
        add(controlPanel, gc);
        
        setPreferredSize(new Dimension(400, 270));
        
        update(null, 0, 0);
    }
    
    public void update(List<Card> cards, int bet, int pot) {
        if (bet == 0) {
            betLabel.setText(" ");
        } else {
            betLabel.setText("$ " + bet);
        }
        if (pot == 0) {
            potLabel.setText(" ");
        } else {
            potLabel.setText("$ " + pot);
        }
        int noOfCards = (cards == null) ? 0 : cards.size();
        for (int i = 0; i < NO_OF_CARDS; i++) {
            if (i < noOfCards) {
                cardLabels[i].setIcon(ResourceManager.getCardImage(cards.get(i)));
            } else {
                cardLabels[i].setIcon(ResourceManager.getIcon("/images/card_placeholder.png"));
            }
        }
    }
    
    public void setMessage(String message) {
        if (message.length() == 0) {
            messageLabel.setText(" ");
        } else {
            messageLabel.setText(message);
        }
    }
    
    public void waitForUserInput() {
        controlPanel.waitForUserInput();
    }
    
}
