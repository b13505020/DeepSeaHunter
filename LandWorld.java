import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;

public class LandWorld extends JPanel {

    private Image bgImage;
    private Image walkSheet;

    // 玩家起始位置
    private int px = 1380;
    private int py = 780;

    private String currentPrompt = "";

    // 方向鍵狀態
    private boolean upPressed = false;
    private boolean downPressed = false;
    private boolean leftPressed = false;
    private boolean rightPressed = false;

    /*
     * diver_clean.png 的方向列：
     * 0 = 朝前 / 往下
     * 1 = 朝右
     * 2 = 朝後 / 往上
     *
     * 左走不要用第 3 列，直接用第 1 列水平翻轉。
     */
    private static final int DIR_DOWN = 0;
    private static final int DIR_RIGHT = 1;
    private static final int DIR_UP = 2;

    private int facingDirection = DIR_DOWN;
    private boolean facingLeft = false;

    // 動畫控制
    private int frameCounter = 0;
    private int currentFrame = 0;

    // diver_clean.png：1024 x 560，8 欄 x 4 列
    private final int FRAME_WIDTH = 128;
    private final int FRAME_HEIGHT = 140;

    // 如果 8 格還是太亂，可以改成 4
    private final int WALK_FRAME_COUNT = 8;

    private final int PLAYER_DRAW_WIDTH = 95;
    private final int PLAYER_DRAW_HEIGHT = 105;

    private final int SPEED = 8;
    private final int FRAME_SPEED = 5;

    private Timer animationTimer;

    public LandWorld(ActionListener diveAction) {
        setLayout(null);
        setFocusable(true);
        setPreferredSize(new Dimension(GameLauncher.WIN_WIDTH, GameLauncher.WIN_HEIGHT));

        try {
            bgImage = ImageIO.read(new File("assets/land_base.png"));
            walkSheet = ImageIO.read(new File("assets/diver_clean.png"));
            System.out.println("✅ LandWorld 資源載入成功！");
        } catch (IOException e) {
            System.out.println("❌ LandWorld 資源載入失敗，請檢查 assets 資料夾");
            e.printStackTrace();
        }

        setupKeyControl(diveAction);
        setupAnimationTimer();
    }

    private void setupKeyControl(ActionListener diveAction) {
        addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();

                if (code == KeyEvent.VK_LEFT) {
                    leftPressed = true;
                    facingDirection = DIR_RIGHT;
                    facingLeft = true;
                } else if (code == KeyEvent.VK_RIGHT) {
                    rightPressed = true;
                    facingDirection = DIR_RIGHT;
                    facingLeft = false;
                } else if (code == KeyEvent.VK_UP) {
                    upPressed = true;
                    facingDirection = DIR_UP;
                    facingLeft = false;
                } else if (code == KeyEvent.VK_DOWN) {
                    downPressed = true;
                    facingDirection = DIR_DOWN;
                    facingLeft = false;
                } else if (code == KeyEvent.VK_ENTER) {
                    checkDiveInteraction(diveAction);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int code = e.getKeyCode();

                if (code == KeyEvent.VK_LEFT) {
                    leftPressed = false;
                } else if (code == KeyEvent.VK_RIGHT) {
                    rightPressed = false;
                } else if (code == KeyEvent.VK_UP) {
                    upPressed = false;
                } else if (code == KeyEvent.VK_DOWN) {
                    downPressed = false;
                }
            }
        });
    }

    private void setupAnimationTimer() {
        animationTimer = new Timer(30, e -> {
            updatePlayerMovement();
            updatePrompt();
            repaint();
        });

        animationTimer.start();
    }

    private void updatePlayerMovement() {
        int dx = 0;
        int dy = 0;

        if (leftPressed) {
            dx -= 1;
        }
        if (rightPressed) {
            dx += 1;
        }
        if (upPressed) {
            dy -= 1;
        }
        if (downPressed) {
            dy += 1;
        }

        boolean isMoving = (dx != 0 || dy != 0);

        if (isMoving) {
            px += dx * SPEED;
            py += dy * SPEED;

            // 可以走整張地圖
            px = Math.max(0, Math.min(px, GameLauncher.WIN_WIDTH - PLAYER_DRAW_WIDTH));
            py = Math.max(0, Math.min(py, GameLauncher.WIN_HEIGHT - PLAYER_DRAW_HEIGHT));

            frameCounter++;

            if (frameCounter % FRAME_SPEED == 0) {
                currentFrame = (currentFrame + 1) % WALK_FRAME_COUNT;
            }

        } else {
            currentFrame = 0;
            frameCounter = 0;
        }
    }

    private void updatePrompt() {
        Rectangle blacksmithRect = new Rectangle(80, 680, 260, 220);
        Rectangle hqRect = new Rectangle(700, 680, 300, 220);

        // 你圈起來的右下 Dive Zone 建築物範圍
        Rectangle diveRect = new Rectangle(900, 560, 680, 330);

        Rectangle playerRect = new Rectangle(px, py, PLAYER_DRAW_WIDTH, PLAYER_DRAW_HEIGHT);

        if (playerRect.intersects(blacksmithRect)) {
            currentPrompt = "Press F to Upgrade";
        } else if (playerRect.intersects(hqRect)) {
            currentPrompt = "Press F to talk to Commander";
        } else if (playerRect.intersects(diveRect)) {
            currentPrompt = "Press Enter to DIVE!";
        } else {
            currentPrompt = "";
        }
    }

    private void checkDiveInteraction(ActionListener diveAction) {
        Rectangle diveRect = new Rectangle(900, 560, 680, 330);
        Rectangle playerRect = new Rectangle(px, py, PLAYER_DRAW_WIDTH, PLAYER_DRAW_HEIGHT);

        if (playerRect.intersects(diveRect)) {
            diveAction.actionPerformed(
                new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "dive")
            );
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (bgImage != null) {
            g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(new Color(40, 40, 40));
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        drawPlayer(g);
        drawPrompt(g);
    }

    private void drawPlayer(Graphics g) {
        if (walkSheet == null) {
            g.setColor(Color.YELLOW);
            g.fillRect(px, py, PLAYER_DRAW_WIDTH, PLAYER_DRAW_HEIGHT);
            return;
        }

        int srcX1 = currentFrame * FRAME_WIDTH;
        int srcY1 = facingDirection * FRAME_HEIGHT;
        int srcX2 = srcX1 + FRAME_WIDTH;
        int srcY2 = srcY1 + FRAME_HEIGHT;

        if (facingLeft) {
            // 用右走那一列，水平翻轉成左走
            g.drawImage(
                walkSheet,
                px + PLAYER_DRAW_WIDTH,
                py,
                px,
                py + PLAYER_DRAW_HEIGHT,
                srcX1,
                srcY1,
                srcX2,
                srcY2,
                this
            );
        } else {
            g.drawImage(
                walkSheet,
                px,
                py,
                px + PLAYER_DRAW_WIDTH,
                py + PLAYER_DRAW_HEIGHT,
                srcX1,
                srcY1,
                srcX2,
                srcY2,
                this
            );
        }
    }

    private void drawPrompt(Graphics g) {
        if (!currentPrompt.isEmpty()) {
            int boxWidth = 310;
            int boxHeight = 35;

            int boxX = px - 20;
            int boxY = py - 45;

            if (boxX + boxWidth > getWidth()) {
                boxX = getWidth() - boxWidth - 20;
            }

            if (boxX < 20) {
                boxX = 20;
            }

            if (boxY < 20) {
                boxY = py + PLAYER_DRAW_HEIGHT + 10;
            }

            g.setColor(new Color(0, 0, 0, 160));
            g.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 12, 12);

            g.setColor(Color.CYAN);
            g.setFont(new Font("Monospaced", Font.BOLD, 18));
            g.drawString(currentPrompt, boxX + 12, boxY + 23);
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();

        SwingUtilities.invokeLater(() -> {
            requestFocusInWindow();
        });
    }
}