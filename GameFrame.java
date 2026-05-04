import java.awt.CardLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class GameFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;

    private StartMenuPanel startMenuPanel;
    private GamePanel gamePanel;

    public GameFrame() {
        this.setTitle("深海工域");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        startMenuPanel = new StartMenuPanel(this);
        gamePanel = new GamePanel();

        mainPanel.add(startMenuPanel, "START_MENU");
        mainPanel.add(gamePanel, "GAME");

        this.add(mainPanel);
        this.pack();

        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public void startGame() {
        cardLayout.show(mainPanel, "GAME");
        gamePanel.requestFocusInWindow();
    }

    public void showStartMenu() {
        cardLayout.show(mainPanel, "START_MENU");
    }
}