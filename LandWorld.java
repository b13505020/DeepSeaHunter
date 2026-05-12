import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.imageio.ImageIO;

public class LandWorld extends JPanel implements KeyListener, Runnable {

    private Image background;
    private Image diverSheet;

    private double playerX = 800;
    private double playerY = 600;

    private final int PLAYER_WIDTH = 110;
    private final int PLAYER_HEIGHT = 135;

    private boolean left;
    private boolean right;
    private boolean up;
    private boolean down;

    private boolean facingLeft = false;

    private final double SPEED = 6.0;

    private Rectangle equipmentZone = new Rectangle(45, 350, 150, 130);
    private Rectangle diveZone = new Rectangle(1150, 600, 250, 250);

    private ActionListener onDive;
    private ActionListener onEnterShop;

    public LandWorld(ActionListener onDive, ActionListener onEnterShop) {
        this.onDive = onDive;
        this.onEnterShop = onEnterShop;

        setLayout(null);
        setFocusable(true);
        addKeyListener(this);

        loadImages();
        setupUIButtons();

        new Thread(this).start();
    }

    private void loadImages() {
        try {
            background = ImageIO.read(new File("assets/land_base.png"));
            diverSheet = ImageIO.read(new File("assets/diver_clean.png"));
        } catch (Exception e) {
            System.out.println("圖片載入失敗，請檢查 assets 資料夾");
        }
    }

    private void setupUIButtons() {
        JButton storageBtn = new JButton("Storage 箱子");
        storageBtn.setBounds(30, 30, 140, 40);
        storageBtn.setFocusable(false);

        storageBtn.addActionListener(e -> {
            new StorageView();
            requestFocusInWindow();
        });

        add(storageBtn);
    }

    public void resetPlayerPosition() {
        playerX = 800;
        playerY = 600;

        left = false;
        right = false;
        up = false;
        down = false;

        requestFocusInWindow();
    }

    @Override
    public void run() {
        while (true) {
            updatePlayerMovement();
            repaint();

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void updatePlayerMovement() {
        double dx = 0;
        double dy = 0;

        if (left) {
            dx -= 1;
            facingLeft = true;
        }

        if (right) {
            dx += 1;
            facingLeft = false;
        }

        if (up) {
            dy -= 1;
        }

        if (down) {
            dy += 1;
        }

        if (dx != 0 || dy != 0) {
            double length = Math.sqrt(dx * dx + dy * dy);
            dx /= length;
            dy /= length;

            playerX += dx * SPEED;
            playerY += dy * SPEED;
        }

        playerX = Math.max(0, Math.min(playerX, 1600 - PLAYER_WIDTH));
        playerY = Math.max(0, Math.min(playerY, 900 - PLAYER_HEIGHT));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (background != null) {
            g.drawImage(background, 0, 0, 1600, 900, this);
        }

        drawPlayer(g);
        drawInteractionPrompt(g);
    }

    private void drawPlayer(Graphics g) {
        int sx = (int) playerX;
        int sy = (int) playerY;

        if (diverSheet != null) {
            if (facingLeft) {
                g.drawImage(
                    diverSheet,
                    sx + PLAYER_WIDTH,
                    sy,
                    sx,
                    sy + PLAYER_HEIGHT,
                    0,
                    0,
                    128,
                    140,
                    this
                );
            } else {
                g.drawImage(
                    diverSheet,
                    sx,
                    sy,
                    sx + PLAYER_WIDTH,
                    sy + PLAYER_HEIGHT,
                    0,
                    0,
                    128,
                    140,
                    this
                );
            }
        } else {
            g.setColor(new Color(200, 100, 50));
            g.fillRect(sx, sy, PLAYER_WIDTH, PLAYER_HEIGHT);
        }
    }

    private void drawInteractionPrompt(Graphics g) {
        Rectangle pRect = new Rectangle(
            (int) playerX,
            (int) playerY,
            PLAYER_WIDTH,
            PLAYER_HEIGHT
        );

        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 22));

        if (pRect.intersects(equipmentZone)) {
            g.drawString("Press ENTER to Shop", (int) playerX - 30, (int) playerY - 20);
        } else if (pRect.intersects(diveZone)) {
            g.drawString("Press ENTER to Dive", (int) playerX - 30, (int) playerY - 20);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) {
            left = true;
        }

        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) {
            right = true;
        }

        if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
            up = true;
        }

        if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) {
            down = true;
        }

        if (code == KeyEvent.VK_ENTER) {
            Rectangle pRect = new Rectangle(
                (int) playerX,
                (int) playerY,
                PLAYER_WIDTH,
                PLAYER_HEIGHT
            );

            if (pRect.intersects(equipmentZone)) {
                onEnterShop.actionPerformed(null);
            } else if (pRect.intersects(diveZone)) {
                onDive.actionPerformed(null);
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) {
            left = false;
        }

        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) {
            right = false;
        }

        if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
            up = false;
        }

        if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) {
            down = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}