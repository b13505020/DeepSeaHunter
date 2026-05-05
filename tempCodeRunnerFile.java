import javax.swing.*;
import java.awt.*;

public class GameLauncher extends JFrame {

    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);

    private GameCover coverPanel; // 修正後的封面
    private LandWorld landPanel;
    private OceanWorld oceanPanel;
    private GearShopScreen gearShopPanel;

    public static final int WIN_WIDTH = 1600;
    public static final int WIN_HEIGHT = 900;

    public GameLauncher() {
        setTitle("深海工域 - Deep Sea Industry");
        setSize(WIN_WIDTH, WIN_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // 1. 初始化封面：按下按鈕進入地面地圖
        coverPanel = new GameCover(e -> {
            cardLayout.show(mainPanel, "LandScreen");
            landPanel.requestFocusInWindow();
        });

        // 2. 初始化裝備商店
        gearShopPanel = new GearShopScreen(e -> {
            cardLayout.show(mainPanel, "LandScreen");
            landPanel.requestFocusInWindow();
        });

        // 3. 初始化海底世界
        oceanPanel = new OceanWorld(e -> {
            cardLayout.show(mainPanel, "LandScreen");
            landPanel.requestFocusInWindow();
        });

        // 4. 初始化陸地世界：傳入切換到海底與商店的邏輯
        landPanel = new LandWorld(
            e -> { // 下水
                oceanPanel.resetPlayerPosition();
                cardLayout.show(mainPanel, "OceanScreen");
                SwingUtilities.invokeLater(() -> oceanPanel.requestFocusInWindow());
            },
            e -> { // 進入店舖
                cardLayout.show(mainPanel, "GearShopScreen");
                SwingUtilities.invokeLater(() -> gearShopPanel.requestFocusInWindow());
            }
        );

        // 5. 加入 CardLayout
        mainPanel.add(coverPanel, "CoverScreen");
        mainPanel.add(landPanel, "LandScreen");
        mainPanel.add(oceanPanel, "OceanScreen");
        mainPanel.add(gearShopPanel, "GearShopScreen");

        add(mainPanel);

        // 啟動時即為封面
        cardLayout.show(mainPanel, "CoverScreen"); 

        setVisible(true);
    }

    public static void main(String[] args) {
        System.setProperty("sun.java2d.uiScale", "1.0");
        SwingUtilities.invokeLater(() -> new GameLauncher());
    }
}