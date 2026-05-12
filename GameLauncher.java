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
    private GearShopScreen gearShopPanel;

    public static final int WIN_WIDTH = 1600;
    public static final int WIN_HEIGHT = 900;

    public GameLauncher() {
        setTitle("深海工域 - Deep Sea Industry");
        setSize(WIN_WIDTH, WIN_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        oceanPanel = new OceanWorld(e -> {
            cardLayout.show(mainPanel, "LandScreen");
            SwingUtilities.invokeLater(() -> {
                landPanel.resetPlayerPosition();
                landPanel.requestFocusInWindow();
            });
        });

        gearShopPanel = new GearShopScreen(e -> {
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
            }
        );

        JPanel titlePanel = createTitlePanel();

        mainPanel.add(titlePanel, "TitleScreen");
        mainPanel.add(landPanel, "LandScreen");
        mainPanel.add(oceanPanel, "OceanScreen");
        mainPanel.add(gearShopPanel, "GearShopScreen");

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
                    System.out.println("cover.png loaded");
                } catch (IOException e) {
                    System.out.println("Cannot load assets/cover.png");
                    e.printStackTrace();
                }

                JButton startBtn = new JButton("START MISSION");

                startBtn.setBounds(600, 600, 400, 100);
                startBtn.setFont(new Font("Monospaced", Font.BOLD, 34));
                startBtn.setBackground(new Color(20, 50, 70));
                startBtn.setForeground(new Color(0, 255, 255));
                startBtn.setFocusPainted(false);
                startBtn.setBorder(
                    BorderFactory.createLineBorder(
                        new Color(0, 255, 255),
                        4
                    )
                );

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
                        System.out.println("Cannot find assets/cursor_hand.png. Use system hand cursor.");
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

                    g2.drawImage(
                        originalImage,
                        0,
                        0,
                        cursorWidth,
                        cursorHeight,
                        null
                    );

                    g2.dispose();

                    return Toolkit.getDefaultToolkit().createCustomCursor(
                        cursorImage,
                        new Point(6, 2),
                        "custom hand cursor"
                    );

                } catch (Exception e) {
                    System.out.println("Custom cursor failed. Use system hand cursor.");
                    e.printStackTrace();
                    return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                if (coverImg != null) {
                    g.drawImage(
                        coverImg,
                        0,
                        0,
                        getWidth(),
                        getHeight(),
                        this
                    );
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