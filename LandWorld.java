import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
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
    private ActionListener onEnterShallowOcean;
    private ActionListener onEnterShop;
    private ActionListener onEnterHeadquarters;
    private ActionListener onEnterBeach;
    private ActionListener onEnterWeaponShop;
    private ActionListener onEnterAquarium;
    private ActionListener onBackToTitle;

    public LandWorld(
        ActionListener onDive,
        ActionListener onEnterShallowOcean,
        ActionListener onEnterShop,
        ActionListener onEnterHeadquarters,
        ActionListener onEnterBeach,
        ActionListener onEnterWeaponShop,
        ActionListener onEnterAquarium,
        ActionListener onBackToTitle
    ) {
        this.onDive = onDive;
        this.onEnterShallowOcean = onEnterShallowOcean;
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
            showDiveSelectDialog();
        } else if (isInBeachZone(pRect)) {
            onEnterBeach.actionPerformed(null);
        }
    }

    private void showDiveSelectDialog() {
        left = false;
        right = false;
        up = false;
        down = false;

        JDialog dialog = new JDialog(
            SwingUtilities.getWindowAncestor(this),
            "選擇潛水地圖",
            Dialog.ModalityType.APPLICATION_MODAL
        );

        dialog.setSize(760, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(new Color(16, 28, 38));

        JLabel titleLabel = new JLabel("請選擇要前往的海域", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 30));
        titleLabel.setForeground(new Color(255, 225, 140));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(26, 0, 14, 0));

        JPanel cardPanel = new JPanel(new GridLayout(1, 2, 28, 0));
        cardPanel.setOpaque(false);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 25, 50));

        JButton shallowButton = createDiveMapButton(
            "淺水區",
            "assets/shallow_ocean_map.jpeg",
            new Color(70, 210, 230)
        );

        JButton deepButton = createDiveMapButton(
            "深海區",
            "assets/ocean_map.png",
            new Color(80, 150, 255)
        );

        shallowButton.addActionListener(e -> {
            dialog.dispose();

            if (onEnterShallowOcean != null) {
                onEnterShallowOcean.actionPerformed(null);
            }
        });

        deepButton.addActionListener(e -> {
            dialog.dispose();

            if (onDive != null) {
                onDive.actionPerformed(null);
            }
        });

        cardPanel.add(shallowButton);
        cardPanel.add(deepButton);

        JButton cancelButton = new JButton("取消");
        cancelButton.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
        cancelButton.setFocusable(false);
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setBackground(new Color(70, 70, 80));
        cancelButton.setBorder(BorderFactory.createEmptyBorder(10, 35, 10, 35));
        cancelButton.addActionListener(e -> dialog.dispose());

        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));
        bottomPanel.add(cancelButton);

        dialog.add(titleLabel, BorderLayout.NORTH);
        dialog.add(cardPanel, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);

        requestFocusInWindow();
    }

    private JButton createDiveMapButton(String title, String imagePath, Color borderColor) {
        JButton button = new JButton() {
            private Image image;

            {
                try {
                    image = ImageIO.read(new File(imagePath));
                } catch (Exception e) {
                    image = null;
                    System.out.println("潛水地圖縮圖載入失敗：" + imagePath);
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
                );

                g2.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR
                );

                int w = getWidth();
                int h = getHeight();

                g2.setColor(new Color(8, 18, 28));
                g2.fillRoundRect(0, 0, w, h, 24, 24);

                if (image != null) {
                    Shape oldClip = g2.getClip();

                    g2.setClip(new RoundRectangle2D.Double(0, 0, w, h, 24, 24));

                    int imgW = image.getWidth(this);
                    int imgH = image.getHeight(this);

                    double scale = Math.max(
                        w / (double) imgW,
                        h / (double) imgH
                    );

                    int drawW = (int) (imgW * scale);
                    int drawH = (int) (imgH * scale);

                    int drawX = (w - drawW) / 2;
                    int drawY = (h - drawH) / 2;

                    g2.drawImage(image, drawX, drawY, drawW, drawH, this);
                    g2.setClip(oldClip);
                } else {
                    g2.setColor(new Color(25, 80, 100));
                    g2.fillRoundRect(0, 0, w, h, 24, 24);
                }

                g2.setColor(new Color(0, 0, 0, 130));
                g2.fillRoundRect(0, 0, w, h, 24, 24);

                if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 45));
                    g2.fillRoundRect(0, 0, w, h, 24, 24);
                }

                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(5));
                g2.drawRoundRect(3, 3, w - 7, h - 7, 24, 24);

                g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 32));
                FontMetrics fm = g2.getFontMetrics();

                int textW = fm.stringWidth(title);
                int textX = w / 2 - textW / 2;
                int textY = h / 2 + fm.getAscent() / 2 - 6;

                g2.setColor(new Color(0, 0, 0, 180));
                g2.fillRoundRect(
                    textX - 20,
                    textY - fm.getAscent() - 10,
                    textW + 40,
                    56,
                    16,
                    16
                );

                g2.setColor(new Color(255, 235, 150));
                g2.drawString(title, textX, textY);

                g2.dispose();
            }
        };

        button.setPreferredSize(new Dimension(280, 280));
        button.setMinimumSize(new Dimension(280, 280));
        button.setMaximumSize(new Dimension(280, 280));
        button.setFocusable(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setOpaque(false);

        return button;
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