import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;

public class StartMenuPanel extends JPanel {

    private GameFrame frame;
    private Image backgroundImage;

    private boolean isHoveringStartButton = false;

    private float glowAlpha = 0.45f;
    private boolean glowIncreasing = true;

    private Cursor normalCursor;
    private Cursor handCursor;

    public StartMenuPanel(GameFrame frame) {
        this.frame = frame;

        this.setPreferredSize(new Dimension(GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT));
        this.setLayout(null);
        this.setDoubleBuffered(true);
        this.setFocusable(true);

        normalCursor = Cursor.getDefaultCursor();
        handCursor = loadCustomCursor();

        loadBackgroundImage();
        setupMouseControl();
        setupGlowAnimation();
    }

    private void loadBackgroundImage() {
        try {
            backgroundImage = ImageIO.read(new File("assets/start_menu.png"));
            System.out.println("Start menu image loaded.");
        } catch (IOException e) {
            System.out.println("Cannot load start_menu.png.");
            e.printStackTrace();
        }
    }

    private Cursor loadCustomCursor() {
        File assetsDir = new File("assets");

        if (!assetsDir.exists()) {
            System.out.println("assets folder does not exist. Use default hand cursor.");
            return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
        }

        File[] cursorFiles = assetsDir.listFiles((dir, name) -> {
            String lowerName = name.toLowerCase();
            return lowerName.startsWith("cursor_hand") && lowerName.endsWith(".png");
        });

        if (cursorFiles == null || cursorFiles.length == 0) {
            System.out.println("No cursor_hand png file found. Use default hand cursor.");
            return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
        }

        try {
            File cursorFile = cursorFiles[0];
            System.out.println("Cursor file found: " + cursorFile.getAbsolutePath());

            BufferedImage originalImage = ImageIO.read(cursorFile);

            if (originalImage == null) {
                System.out.println("Cursor image cannot be read. Use default hand cursor.");
                return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
            }

            Dimension bestSize = Toolkit.getDefaultToolkit().getBestCursorSize(32, 32);

            int cursorWidth = bestSize.width;
            int cursorHeight = bestSize.height;

            if (cursorWidth <= 0 || cursorHeight <= 0) {
                cursorWidth = 32;
                cursorHeight = 32;
            }

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

            Cursor customCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                cursorImage,
                new Point(6, 2),
                "custom hand cursor"
            );

            System.out.println("Custom cursor loaded successfully.");
            return customCursor;

        } catch (Exception e) {
            System.out.println("Failed to load custom cursor. Use default hand cursor.");
            e.printStackTrace();
            return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
        }
    }

    /*
     * 根據你的封面原圖 1254 x 1254 換算開始遊戲按鈕位置。
     * 原圖按鈕約為：
     * x = 359, y = 678, width = 535, height = 136
     */
    private Rectangle getStartButtonArea() {
        int panelWidth = getWidth();
        int panelHeight = getHeight();

        if (panelWidth <= 0) {
            panelWidth = GamePanel.SCREEN_WIDTH;
        }

        if (panelHeight <= 0) {
            panelHeight = GamePanel.SCREEN_HEIGHT;
        }

        int x = (int) Math.round(panelWidth * 359.0 / 1254.0);
        int y = (int) Math.round(panelHeight * 678.0 / 1254.0);
        int width = (int) Math.round(panelWidth * 535.0 / 1254.0);
        int height = (int) Math.round(panelHeight * 136.0 / 1254.0);

        return new Rectangle(x, y, width, height);
    }

    private void setupMouseControl() {
        this.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Rectangle startButtonArea = getStartButtonArea();

                if (startButtonArea.contains(e.getPoint())) {
                    isHoveringStartButton = true;
                    setCursor(handCursor);
                } else {
                    isHoveringStartButton = false;
                    setCursor(normalCursor);
                }

                repaint();
            }
        });

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Rectangle startButtonArea = getStartButtonArea();

                if (startButtonArea.contains(e.getPoint())) {
                    frame.startGame();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHoveringStartButton = false;
                setCursor(normalCursor);
                repaint();
            }
        });
    }

    private void setupGlowAnimation() {
        Timer timer = new Timer(35, e -> {
            if (isHoveringStartButton) {
                if (glowIncreasing) {
                    glowAlpha += 0.035f;

                    if (glowAlpha >= 1.0f) {
                        glowAlpha = 1.0f;
                        glowIncreasing = false;
                    }
                } else {
                    glowAlpha -= 0.035f;

                    if (glowAlpha <= 0.45f) {
                        glowAlpha = 0.45f;
                        glowIncreasing = true;
                    }
                }
            } else {
                glowAlpha = 0.45f;
                glowIncreasing = true;
            }

            repaint();
        });

        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int panelWidth = getWidth();
        int panelHeight = getHeight();

        if (panelWidth <= 0) {
            panelWidth = GamePanel.SCREEN_WIDTH;
        }

        if (panelHeight <= 0) {
            panelHeight = GamePanel.SCREEN_HEIGHT;
        }

        if (backgroundImage != null) {
            g.drawImage(
                backgroundImage,
                0,
                0,
                panelWidth,
                panelHeight,
                this
            );
        } else {
            g.setColor(new Color(0, 30, 80));
            g.fillRect(0, 0, panelWidth, panelHeight);
        }

        if (isHoveringStartButton) {
            drawButtonGlow(g);
        }
    }

    private void drawButtonGlow(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );

        Rectangle area = getStartButtonArea();

        int x = area.x;
        int y = area.y;
        int w = area.width;
        int h = area.height;

        float pulse = glowAlpha;

        // 外層淡藍光暈
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.10f * pulse));
        g2.setColor(new Color(60, 180, 255));
        g2.setStroke(new BasicStroke(10));
        g2.drawRoundRect(x - 6, y - 6, w + 12, h + 12, 18, 18);

        // 中層藍色霓虹
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.30f * pulse));
        g2.setColor(new Color(100, 225, 255));
        g2.setStroke(new BasicStroke(5));
        g2.drawRoundRect(x - 2, y - 2, w + 4, h + 4, 16, 16);

        // 主亮框
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.88f));
        g2.setColor(new Color(220, 250, 255));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x, y, w, h, 14, 14);

        // 上緣亮光
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.14f * pulse));
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(x + 18, y + 7, w - 36, 7, 8, 8);

        // 內側微亮
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.04f * pulse));
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(x + 4, y + 4, w - 8, h - 8, 12, 12);

        // 四角小亮點
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.90f * pulse));
        g2.setColor(new Color(255, 220, 110));

        int s = 5;
        g2.fillRect(x + 7, y + 7, s, s);
        g2.fillRect(x + w - 12, y + 7, s, s);
        g2.fillRect(x + 7, y + h - 12, s, s);
        g2.fillRect(x + w - 12, y + h - 12, s, s);

        g2.dispose();
    }
}