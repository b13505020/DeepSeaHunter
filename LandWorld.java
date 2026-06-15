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
    private final int WORLD_WIDTH = 1600;
    private final int WORLD_HEIGHT = 900;

    private Rectangle[] shopZones = {
        new Rectangle(0, 230, 240, 270),
        new Rectangle(180, 240, 220, 260),
        new Rectangle(70, 180, 260, 90),
        new Rectangle(250, 260, 150, 230)
    };

    private Rectangle[] weaponShopZones = {
        new Rectangle(390, 205, 210, 285),
        new Rectangle(325, 300, 210, 210)
    };

    private Rectangle[] headquartersZones = {
        new Rectangle(720, 150, 240, 390),
        new Rectangle(930, 360, 180, 140)
    };

    private Rectangle aquariumZone = new Rectangle(45, 505, 355, 265);

    private Rectangle diveZone = new Rectangle(1080, 585, 295, 275);

    private Rectangle beachZone = new Rectangle(620, 680, 380, 210);

    private ActionListener onDive;
    private ActionListener onEnterShop;
    private ActionListener onEnterHeadquarters;
    private ActionListener onEnterBeach;
    private ActionListener onEnterWeaponShop;
    private ActionListener onEnterAquarium;
    private ActionListener onBackToTitle;

    public LandWorld(
        ActionListener onDive,
        ActionListener onEnterShop,
        ActionListener onEnterHeadquarters,
        ActionListener onEnterBeach,
        ActionListener onEnterWeaponShop,
        ActionListener onEnterAquarium,
        ActionListener onBackToTitle
    ) {
        this.onDive = onDive;
        this.onEnterShop = onEnterShop;
        this.onEnterHeadquarters = onEnterHeadquarters;
        this.onEnterBeach = onEnterBeach;
        this.onEnterWeaponShop = onEnterWeaponShop;
        this.onEnterAquarium = onEnterAquarium;
        this.onBackToTitle = onBackToTitle;

        setLayout(null);
        setFocusable(true);
        setPreferredSize(new Dimension(WORLD_WIDTH, WORLD_HEIGHT));
        addKeyListener(this);

        loadImages();
        setupUIButtons();

        Thread gameThread = new Thread(this);
        gameThread.setDaemon(true);
        gameThread.start();
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

        JButton menuBtn = new JButton("Menu 選單");
        menuBtn.setBounds(180, 30, 120, 40);
        menuBtn.setFocusable(false);

        menuBtn.addActionListener(e -> {
            left = false;
            right = false;
            up = false;
            down = false;

            InventoryManager.saveGame();

            if (onBackToTitle != null) {
                onBackToTitle.actionPerformed(null);
            }
        });

        add(menuBtn);
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

        playerX = Math.max(0, Math.min(playerX, WORLD_WIDTH - PLAYER_WIDTH));
        playerY = Math.max(0, Math.min(playerY, WORLD_HEIGHT - PLAYER_HEIGHT));
    }

    private Rectangle getPlayerRect() {
        return new Rectangle(
            (int) playerX,
            (int) playerY,
            PLAYER_WIDTH,
            PLAYER_HEIGHT
        );
    }

    private boolean isInAnyZone(Rectangle playerRect, Rectangle[] zones) {
        for (Rectangle zone : zones) {
            if (playerRect.intersects(zone)) {
                return true;
            }
        }

        return false;
    }

    private boolean isInShopZone(Rectangle playerRect) {
        return isInAnyZone(playerRect, shopZones);
    }

    private boolean isInWeaponShopZone(Rectangle playerRect) {
        return isInAnyZone(playerRect, weaponShopZones);
    }

    private boolean isInHeadquartersZone(Rectangle playerRect) {
        return isInAnyZone(playerRect, headquartersZones);
    }

    private boolean isInAquariumZone(Rectangle playerRect) {
        return playerRect.intersects(aquariumZone);
    }

    private boolean isInBeachZone(Rectangle playerRect) {
        return playerRect.intersects(beachZone);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        double scaleX = getWidth() / (double) WORLD_WIDTH;
        double scaleY = getHeight() / (double) WORLD_HEIGHT;

        g2.scale(scaleX, scaleY);

        if (background != null) {
            g2.drawImage(background, 0, 0, WORLD_WIDTH, WORLD_HEIGHT, this);
        } else {
            g2.setColor(new Color(35, 35, 45));
            g2.fillRect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        }

        drawPlayer(g2);
        drawInteractionPrompt(g2);

        g2.dispose();
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
        Rectangle pRect = getPlayerRect();

        String prompt = null;

        if (isInHeadquartersZone(pRect)) {
            prompt = "Press ENTER to Headquarters";
        } else if (isInAquariumZone(pRect)) {
            prompt = "Press ENTER to Aquarium";
        } else if (isInWeaponShopZone(pRect)) {
            prompt = "Press ENTER to Blacksmith";
        } else if (isInShopZone(pRect)) {
            prompt = "Press ENTER to Shop";
        } else if (pRect.intersects(diveZone)) {
            prompt = "Press ENTER to Dive";
        } else if (isInBeachZone(pRect)) {
            prompt = "Press ENTER to Beach";
        }

        if (prompt == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setFont(new Font("Monospaced", Font.BOLD, 22));
        FontMetrics fm = g2.getFontMetrics();

        int textW = fm.stringWidth(prompt);
        int textX = (int) playerX + PLAYER_WIDTH / 2 - textW / 2;
        int textY = (int) playerY - 20;

        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect(textX - 12, textY - 26, textW + 24, 34, 12, 12);

        g2.setColor(Color.WHITE);
        g2.drawString(prompt, textX, textY);

        g2.dispose();
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
            handleEnterAction();
        }
    }

    private void handleEnterAction() {
        Rectangle pRect = getPlayerRect();

        if (isInHeadquartersZone(pRect)) {
            onEnterHeadquarters.actionPerformed(null);
        } else if (isInAquariumZone(pRect)) {
            onEnterAquarium.actionPerformed(null);
        } else if (isInWeaponShopZone(pRect)) {
            onEnterWeaponShop.actionPerformed(null);
        } else if (isInShopZone(pRect)) {
            onEnterShop.actionPerformed(null);
        } else if (pRect.intersects(diveZone)) {
            onDive.actionPerformed(null);
        } else if (isInBeachZone(pRect)) {
            onEnterBeach.actionPerformed(null);
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