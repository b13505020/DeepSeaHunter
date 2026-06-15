import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GameLauncher extends JFrame {

    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);

    private LandWorld landPanel;
    private OceanWorld oceanPanel;
    private ShallowOceanWorld shallowOceanPanel;
    private GearShopScreen gearShopPanel;
    private WeaponShopScreen weaponShopPanel;
    private BeachWorld beachPanel;
    private AquariumView aquariumPanel;
    private QuestHallView questHallPanel;

    private JPanel titlePanel;
    private boolean gameStarted = false;
    private String currentScreen = "TitleScreen";

    private MissionHudOverlay missionHud;

    public static final int WIN_WIDTH = 1600;
    public static final int WIN_HEIGHT = 900;

    public GameLauncher() {
        setTitle("深海工域 - Deep Sea Industry");

        setUndecorated(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        setupWindowCloseSave();

        titlePanel = createTitlePanel();
        mainPanel.add(titlePanel, "TitleScreen");

        mainPanel.setPreferredSize(new Dimension(WIN_WIDTH, WIN_HEIGHT));
        setContentPane(mainPanel);

        missionHud = new MissionHudOverlay();
        setGlassPane(missionHud);
        hideMissionHud();

        showScreen("TitleScreen");

        mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            "goBack"
        );

        mainPanel.getActionMap().put("goBack", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleEscape();
            }
        });

        GraphicsDevice gd = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getDefaultScreenDevice();

        gd.setFullScreenWindow(this);
        setVisible(true);
        playAppleMusicPlaylist();
    }

    private void playAppleMusicPlaylist() {
        if (!isMacOS()) {
            System.out.println("Apple Music 自動播放功能僅支援 macOS。");
            return;
        }

        try {
            new ProcessBuilder(
                "osascript",
                "-e",
                "tell application \"Music\"",
                "-e",
                "set targetPlaylist to user playlist \"🌪️\"",
                "-e",
                "set trackCount to count of tracks of targetPlaylist",
                "-e",
                "if trackCount is 0 then return",
                "-e",
                "set randomIndex to random number from 1 to trackCount",
                "-e",
                "set shuffle enabled to true",
                "-e",
                "set song repeat to all",
                "-e",
                "play track randomIndex of targetPlaylist",
                "-e",
                "end tell"
            ).start();
        } catch (Exception e) {
            System.out.println("無法播放 Apple Music 歌單：" + e.getMessage());
        }
    }

    private void stopAppleMusic() {
        if (!isMacOS()) {
            return;
        }

        try {
            Process pauseProcess = new ProcessBuilder(
                "osascript",
                "-e",
                "tell application \"Music\" to pause"
            ).start();

            if (!pauseProcess.waitFor(3, TimeUnit.SECONDS)) {
                pauseProcess.destroy();
                System.out.println("暫停 Apple Music 逾時。");
            }
        } catch (Exception e) {
            System.out.println("無法暫停 Apple Music：" + e.getMessage());
        }
    }

    private boolean isMacOS() {
        String osName = System.getProperty("os.name", "");
        return osName.toLowerCase().contains("mac");
    }

    private void showMissionHud() {
        if (missionHud != null) {
            missionHud.setVisible(true);
            missionHud.repaint();
        }
    }

    private void hideMissionHud() {
        if (missionHud != null) {
            missionHud.setVisible(false);
            missionHud.repaint();
        }
    }

    private void showScreen(String screenName) {
        currentScreen = screenName;
        cardLayout.show(mainPanel, screenName);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void handleEscape() {
        if ("TitleScreen".equals(currentScreen) || "LandScreen".equals(currentScreen)) {
            return;
        }

        returnToLand();
    }

    private void returnToLand() {
        if (!gameStarted || landPanel == null) {
            return;
        }

        showMissionHud();
        showScreen("LandScreen");

        SwingUtilities.invokeLater(() -> landPanel.requestFocusInWindow());
    }

    private void setupWindowCloseSave() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitGame();
            }
        });
    }

    private void exitGame() {
        if (gameStarted) {
            InventoryManager.saveGame();
        }

        stopAppleMusic();

        GraphicsDevice gd = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getDefaultScreenDevice();

        gd.setFullScreenWindow(null);

        dispose();
        System.exit(0);
    }
    

    private void createGamePanels() {
        mainPanel.removeAll();

        oceanPanel = new OceanWorld(e -> {
            returnToLand();

            SwingUtilities.invokeLater(() -> {
                landPanel.resetPlayerPosition();
            });
        });

        shallowOceanPanel = new ShallowOceanWorld(e -> {
            returnToLand();

            SwingUtilities.invokeLater(() -> {
                landPanel.resetPlayerPosition();
            });
        });

        gearShopPanel = new GearShopScreen(e -> returnToLand());

        weaponShopPanel = new WeaponShopScreen(e -> returnToLand());

        beachPanel = new BeachWorld(e -> returnToLand());

        aquariumPanel = new AquariumView(e -> returnToLand());

        questHallPanel = new QuestHallView(e -> returnToLand());

        landPanel = new LandWorld(
            e -> {
                oceanPanel.resetPlayerPosition();
                showMissionHud();
                showScreen("OceanScreen");

                SwingUtilities.invokeLater(() -> {
                    oceanPanel.requestFocusInWindow();
                });
            },
            e -> {
                shallowOceanPanel.resetPlayerPosition();
                showMissionHud();
                showScreen("ShallowOceanScreen");

                SwingUtilities.invokeLater(() -> {
                    shallowOceanPanel.requestFocusInWindow();
                });
            },
            e -> {
                hideMissionHud();
                showScreen("GearShopScreen");

                SwingUtilities.invokeLater(() -> {
                    gearShopPanel.requestFocusInWindow();
                });
            },
            e -> {
                hideMissionHud();
                showScreen("QuestHallScreen");

                SwingUtilities.invokeLater(() -> {
                    questHallPanel.requestFocusInWindow();
                });
            },
            e -> {
                hideMissionHud();
                beachPanel.resetForEntry();
                showScreen("BeachScreen");

                SwingUtilities.invokeLater(() -> {
                    beachPanel.requestFocusInWindow();
                });
            },
            e -> {
                hideMissionHud();
                showScreen("WeaponShopScreen");

                SwingUtilities.invokeLater(() -> {
                    weaponShopPanel.requestFocusInWindow();
                });
            },
            e -> {
hideMissionHud();
showScreen("AquariumScreen");

    SwingUtilities.invokeLater(() -> {
        aquariumPanel.startAquarium();
        aquariumPanel.requestFocusInWindow();
    });
},
            e -> {
                hideMissionHud();

                SwingUtilities.invokeLater(() -> {
                    showScreen("TitleScreen");
                    titlePanel.requestFocusInWindow();
                });
            }
        );

        mainPanel.add(titlePanel, "TitleScreen");
        mainPanel.add(landPanel, "LandScreen");
        mainPanel.add(oceanPanel, "OceanScreen");
        mainPanel.add(shallowOceanPanel, "ShallowOceanScreen");
        mainPanel.add(gearShopPanel, "GearShopScreen");
        mainPanel.add(weaponShopPanel, "WeaponShopScreen");
        mainPanel.add(beachPanel, "BeachScreen");
        mainPanel.add(aquariumPanel, "AquariumScreen");
        mainPanel.add(questHallPanel, "QuestHallScreen");

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

        showMissionHud();
        showScreen("LandScreen");

        SwingUtilities.invokeLater(() -> {
            landPanel.requestFocusInWindow();
        });
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

        showMissionHud();
        showScreen("LandScreen");

        SwingUtilities.invokeLater(() -> {
            landPanel.requestFocusInWindow();
        });
    }

    private JPanel createTitlePanel() {
        return new JPanel(null) {
            private Image coverImg;
            private Cursor handCursor;

            private JButton continueBtn;
            private JButton newGameBtn;
            private JButton exitBtn;

            {
                handCursor = loadCustomCursor();

                try {
                    coverImg = ImageIO.read(new File("assets/cover.png"));
                } catch (IOException e) {
                    System.out.println("Cannot load assets/cover.png");
                }

                continueBtn = createMenuButton("CONTINUE GAME");
                continueBtn.setEnabled(InventoryManager.hasSaveFile());
                continueBtn.addActionListener(e -> continueGame());

                newGameBtn = createMenuButton("NEW GAME");
                newGameBtn.addActionListener(e -> startNewGame());

                exitBtn = createMenuButton("EXIT");
                exitBtn.addActionListener(e -> exitGame());

                add(continueBtn);
                add(newGameBtn);
                add(exitBtn);

                addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        layoutTitleButtons();
                        repaint();
                    }
                });
            }

            private JButton createMenuButton(String text) {
                JButton btn = new JButton(text);
                btn.setFont(new Font("Monospaced", Font.BOLD, 30));
                btn.setForeground(new Color(0, 255, 255));
                btn.setContentAreaFilled(false);
                btn.setOpaque(false);
                btn.setFocusPainted(false);
                btn.setBorder(BorderFactory.createLineBorder(new Color(0, 255, 255), 4));
                btn.setCursor(handCursor);
                return btn;
            }

            private void layoutTitleButtons() {
                int panelW = getWidth();
                int panelH = getHeight();

                if (panelW <= 0 || panelH <= 0) {
                    return;
                }

                int btnW = Math.max(360, panelW / 4);
                int btnH = Math.max(65, panelH / 13);

                int x = panelW / 2 - btnW / 2;

                int continueY = (int) (panelH * 0.54);
                int gap = 25;

                continueBtn.setBounds(x, continueY, btnW, btnH);
                newGameBtn.setBounds(x, continueY + btnH + gap, btnW, btnH);
                exitBtn.setBounds(x, continueY + (btnH + gap) * 2, btnW, btnH);
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

                layoutTitleButtons();

                if (coverImg != null) {
                    g.drawImage(coverImg, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(new Color(0, 30, 80));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }

                drawButtonBackground(g, continueBtn);
                drawButtonBackground(g, newGameBtn);
                drawButtonBackground(g, exitBtn);
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

                String text;

                if (InventoryManager.hasSaveFile()) {
                    g.setColor(new Color(180, 255, 220));
                    text = "已偵測到存檔，可選擇 Continue Game";
                } else {
                    g.setColor(new Color(255, 220, 180));
                    text = "尚未偵測到存檔，請選擇 New Game";
                }

                FontMetrics fm = g.getFontMetrics();
                int x = getWidth() / 2 - fm.stringWidth(text) / 2;
                int y = Math.min(
                    getHeight() - 35,
                    exitBtn.getY() + exitBtn.getHeight() + 45
                );

                g.drawString(text, x, y);
            }
        };
    }

    public static void main(String[] args) {
        System.setProperty("sun.java2d.uiScale", "1.0");
        SwingUtilities.invokeLater(() -> new GameLauncher());
    }
}                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
