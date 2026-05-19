import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;

public class GameLauncher extends JFrame {

    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);

    private LandWorld landPanel;
    private OceanWorld oceanPanel;
    private GearShopScreen gearShopPanel;
    private WeaponShopScreen weaponShopPanel;
    private BeachWorld beachPanel;

    private JPanel titlePanel;
    private boolean gameStarted = false;

    public static final int WIN_WIDTH = 1600;
    public static final int WIN_HEIGHT = 900;

    public GameLauncher() {
        setTitle("深海工域 - Deep Sea Industry");
        setSize(WIN_WIDTH, WIN_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        setupWindowCloseSave();

        titlePanel = createTitlePanel();
        mainPanel.add(titlePanel, "TitleScreen");

        add(mainPanel);
        cardLayout.show(mainPanel, "TitleScreen");

        setVisible(true);
    }

    private void setupWindowCloseSave() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (gameStarted) {
                    InventoryManager.saveGame();
                }

                dispose();
                System.exit(0);
            }
        });
    }

    private void createGamePanels() {
        mainPanel.removeAll();

        oceanPanel = new OceanWorld(e -> {
            cardLayout.show(mainPanel, "LandScreen");
            SwingUtilities.invokeLater(() -> landPanel.requestFocusInWindow());
        });

        gearShopPanel = new GearShopScreen(e -> {
            cardLayout.show(mainPanel, "LandScreen");
            SwingUtilities.invokeLater(() -> landPanel.requestFocusInWindow());
        });

        weaponShopPanel = new WeaponShopScreen(e -> {
            cardLayout.show(mainPanel, "LandScreen");
            SwingUtilities.invokeLater(() -> landPanel.requestFocusInWindow());
        });

        beachPanel = new BeachWorld(e -> {
            cardLayout.show(mainPanel, "LandScreen");
            SwingUtilities.invokeLater(() -> landPanel.requestFocusInWindow());
        });

        landPanel = new LandWorld(
            e -> {
                oceanPanel.resetPlayerPosition();
                cardLayout.show(mainPanel, "OceanScreen");
                SwingUtilities.invokeLater(() -> oceanPanel.requestFocusInWindow());
            },
            e -> {
                cardLayout.show(mainPanel, "GearShopScreen");
                SwingUtilities.invokeLater(() -> gearShopPanel.requestFocusInWindow());
            },
            e -> {
                new QuestHallView();
                SwingUtilities.invokeLater(() -> landPanel.requestFocusInWindow());
            },
            e -> {
                beachPanel.resetForEntry();
                cardLayout.show(mainPanel, "BeachScreen");
                SwingUtilities.invokeLater(() -> beachPanel.requestFocusInWindow());
            },
            e -> {
                cardLayout.show(mainPanel, "WeaponShopScreen");
                SwingUtilities.invokeLater(() -> weaponShopPanel.requestFocusInWindow());
            }
        );

        mainPanel.add(titlePanel, "TitleScreen");
        mainPanel.add(landPanel, "LandScreen");
        mainPanel.add(oceanPanel, "OceanScreen");
        mainPanel.add(gearShopPanel, "GearShopScreen");
        mainPanel.add(weaponShopPanel, "WeaponShopScreen");
        mainPanel.add(beachPanel, "BeachScreen");

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void startNewGame() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "確定要開始新遊戲嗎？\n舊存檔會被覆蓋。",
            "開始新遊戲",
            JOptionPane.YES_NO_OPTION
        );

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        InventoryManager.resetGame();
        InventoryManager.saveGame();
        gameStarted = true;

        createGamePanels();
        cardLayout.show(mainPanel, "LandScreen");

        SwingUtilities.invokeLater(() -> landPanel.requestFocusInWindow());
    }

    private void continueGame() {
        boolean success = InventoryManager.loadGame();

        if (!success) {
            JOptionPane.showMessageDialog(
                this,
                "找不到存檔，請先開始新遊戲。",
                "沒有存檔",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        gameStarted = true;

        createGamePanels();
        cardLayout.show(mainPanel, "LandScreen");

        SwingUtilities.invokeLater(() -> landPanel.requestFocusInWindow());
    }

    private JPanel createTitlePanel() {
        return new JPanel(null) {
            private Image coverImg;
            private Cursor handCursor;

            private JButton continueBtn;
            private JButton newGameBtn;

            {
                handCursor = loadCustomCursor();

                try {
                    coverImg = ImageIO.read(new File("assets/cover.png"));
                } catch (IOException e) {
                    System.out.println("Cannot load assets/cover.png");
                }

                continueBtn = createMenuButton("CONTINUE GAME", 600, 560, 400, 80);
                continueBtn.setEnabled(InventoryManager.hasSaveFile());
                continueBtn.addActionListener(e -> continueGame());

                newGameBtn = createMenuButton("NEW GAME", 600, 660, 400, 80);
                newGameBtn.addActionListener(e -> startNewGame());

                add(continueBtn);
                add(newGameBtn);
            }

            private JButton createMenuButton(String text, int x, int y, int w, int h) {
                JButton btn = new JButton(text);
                btn.setBounds(x, y, w, h);
                btn.setFont(new Font("Monospaced", Font.BOLD, 30));
                btn.setForeground(new Color(0, 255, 255));
                btn.setContentAreaFilled(false);
                btn.setOpaque(false);
                btn.setFocusPainted(false);
                btn.setBorder(BorderFactory.createLineBorder(new Color(0, 255, 255), 4));
                btn.setCursor(handCursor);
                return btn;
            }

            private Cursor loadCustomCursor() {
                try {
                    File cursorFile = new File("assets/cursor_hand.png");

                    if (!cursorFile.exists()) {
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

                drawButtonBackground(g, continueBtn);
                drawButtonBackground(g, newGameBtn);
                drawSaveStatus(g);
            }

            private void drawButtonBackground(Graphics g, JButton btn) {
                if (btn == null) {
                    return;
                }

                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
                );

                Rectangle b = btn.getBounds();

                if (btn.isEnabled()) {
                    g2d.setColor(new Color(10, 40, 60, 180));
                } else {
                    g2d.setColor(new Color(80, 80, 80, 150));
                }

                g2d.fillRoundRect(b.x, b.y, b.width, b.height, 14, 14);

                if (btn.isEnabled()) {
                    g2d.setColor(new Color(0, 255, 255, 220));
                } else {
                    g2d.setColor(new Color(150, 150, 150, 180));
                }

                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(b.x, b.y, b.width, b.height, 14, 14);
            }

            private void drawSaveStatus(Graphics g) {
                g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));

                if (InventoryManager.hasSaveFile()) {
                    g.setColor(new Color(180, 255, 220));
                    g.drawString("已偵測到存檔，可選擇 Continue Game", 610, 765);
                } else {
                    g.setColor(new Color(255, 220, 180));
                    g.drawString("尚未偵測到存檔，請選擇 New Game", 625, 765);
                }
            }
        };
    }

    public static void main(String[] args) {
        System.setProperty("sun.java2d.uiScale", "1.0");
        SwingUtilities.invokeLater(() -> new GameLauncher());
    }
}