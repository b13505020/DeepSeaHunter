import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;

public class GameLauncher extends JFrame {

    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);

    private LandWorld landPanel;
    private OceanWorld oceanPanel;

    public static final int WIN_WIDTH = 1600;
    public static final int WIN_HEIGHT = 900;

    public GameLauncher() {
        setTitle("深海工域 - Deep Sea Industry");
        setSize(WIN_WIDTH, WIN_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // 陸地畫面：在 Dive Zone 按 Enter 後切到海底
        landPanel = new LandWorld(e -> {
            cardLayout.show(mainPanel, "OceanScreen");
            SwingUtilities.invokeLater(() -> oceanPanel.requestFocusInWindow());
        });

        // 海底畫面：按「返回陸地」後切回陸地
        oceanPanel = new OceanWorld(e -> {
            cardLayout.show(mainPanel, "LandScreen");
            SwingUtilities.invokeLater(() -> landPanel.requestFocusInWindow());
        });

        JPanel titlePanel = createTitlePanel();

        mainPanel.add(titlePanel, "TitleScreen");
        mainPanel.add(landPanel, "LandScreen");
        mainPanel.add(oceanPanel, "OceanScreen");

        add(mainPanel);
        cardLayout.show(mainPanel, "TitleScreen");

        setVisible(true);
    }

    private JPanel createTitlePanel() {
        return new JPanel(null) {

            private Image coverImg;
            private Cursor handCursor;

            {
                handCursor = loadCustomCursor();

                try {
                    coverImg = ImageIO.read(new File("assets/cover.png"));
                    System.out.println("✅ 讀取 assets/cover.png 成功");
                } catch (IOException e) {
                    System.out.println("❌ 讀取 assets/cover.png 失敗，請檢查檔名");
                    e.printStackTrace();
                }

                JButton startBtn = new JButton("START MISSION");

                // 按鈕位置與大小：x, y, 寬, 高
                // y 越小越上面
                startBtn.setBounds(600, 600, 400, 100);

                startBtn.setFont(new Font("Monospaced", Font.BOLD, 34));
                startBtn.setBackground(new Color(20, 50, 70));
                startBtn.setForeground(new Color(0, 255, 255));
                startBtn.setFocusPainted(false);
                startBtn.setBorder(BorderFactory.createLineBorder(new Color(0, 255, 255), 4));

                // 滑鼠移到按鈕時，變成你的自訂箭頭
                startBtn.setCursor(handCursor);

                startBtn.addActionListener(e -> {
                    cardLayout.show(mainPanel, "LandScreen");
                    SwingUtilities.invokeLater(() -> landPanel.requestFocusInWindow());
                });

                add(startBtn);
            }

            private Cursor loadCustomCursor() {
                try {
                    File cursorFile = new File("assets/cursor_hand.png");

                    if (!cursorFile.exists()) {
                        System.out.println("找不到 assets/cursor_hand.png，改用系統手指游標");
                        return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
                    }

                    BufferedImage originalImage = ImageIO.read(cursorFile);

                    int cursorWidth = 32;
                    int cursorHeight = 32;

                    BufferedImage cursorImage = new BufferedImage(
                        cursorWidth,
                        cursorHeight,
                        BufferedImage.TYPE_INT_ARGB
                    );

                    Graphics2D g2 = cursorImage.createGraphics();
                    g2.setRenderingHint(
                        RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
                    );
                    g2.drawImage(originalImage, 0, 0, cursorWidth, cursorHeight, null);
                    g2.dispose();

                    return Toolkit.getDefaultToolkit().createCustomCursor(
                        cursorImage,
                        new Point(6, 2),
                        "custom hand cursor"
                    );

                } catch (Exception e) {
                    System.out.println("自訂游標載入失敗，改用系統手指游標");
                    e.printStackTrace();
                    return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                if (coverImg != null) {
                    g.drawImage(coverImg, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(new Color(0, 30, 80));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
    }

    public static void main(String[] args) {
        System.setProperty("sun.java2d.uiScale", "1.0");
        SwingUtilities.invokeLater(() -> new GameLauncher());
    }
}