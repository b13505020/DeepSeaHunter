import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;

public class OceanWorld extends JPanel {

    public static final int SCREEN_WIDTH = 1600;
    public static final int SCREEN_HEIGHT = 900;

    private int worldWidth = SCREEN_WIDTH;
    private int worldHeight = 2800;

    private int cameraX = 0;
    private int cameraY = 0;

    private double playerX = SCREEN_WIDTH / 2.0 - 35;
    private double playerY = 330;

    private final int PLAYER_WIDTH = 70;
    private final int PLAYER_HEIGHT = 85;
    private final double PLAYER_SPEED = 7.0;

    private boolean upPressed = false;
    private boolean downPressed = false;
    private boolean leftPressed = false;
    private boolean rightPressed = false;

    private double aimAngle = 0;

    private Weapon currentWeapon = new Weapon("初級魚槍", 1, 600);

    private ArrayList<Bullet> bullets = new ArrayList<>();
    private ArrayList<OceanFish> fishList = new ArrayList<>();

    private Timer gameTimer;
    private Random random = new Random();

    private BufferedImage oceanMap;
    private BufferedImage diverSheet;

    private Rectangle shipReturnRect;

    public OceanWorld(ActionListener backToLandAction) {
        setLayout(null);
        setFocusable(true);
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));

        loadImages();
        setupShipReturnArea();
        setupButtons(backToLandAction);
        setupControls(backToLandAction);
        spawnFish();
        setupGameLoop();
    }

    private void loadImages() {
        try {
            oceanMap = ImageIO.read(new File("assets/ocean_map.png"));

            double scale = SCREEN_WIDTH / (double) oceanMap.getWidth();
            worldWidth = SCREEN_WIDTH;
            worldHeight = (int) Math.round(oceanMap.getHeight() * scale);

            System.out.println("ocean_map.png loaded");
            System.out.println("Ocean map size = " + worldWidth + " x " + worldHeight);

        } catch (IOException e) {
            System.out.println("Cannot load assets/ocean_map.png. Use fallback background.");
            worldWidth = SCREEN_WIDTH;
            worldHeight = 2800;
        }

        try {
            diverSheet = ImageIO.read(new File("assets/diver_clean.png"));
            System.out.println("diver_clean.png loaded");
        } catch (IOException e) {
            System.out.println("Cannot load assets/diver_clean.png. Use yellow rectangle.");
        }
    }

    private void setupShipReturnArea() {
        shipReturnRect = new Rectangle(
            worldWidth / 2 - 500,
            0,
            1000,
            430
        );
    }

    private void setupButtons(ActionListener backToLandAction) {
        JButton bagBtn = new JButton("Backpack");
        bagBtn.setBounds(30, 30, 100, 35);
        bagBtn.setFocusable(false);
        bagBtn.addActionListener(e -> {
            new BackpackView();
            requestFocusInWindow();
        });
        add(bagBtn);

        JButton colBtn = new JButton("Collection");
        colBtn.setBounds(140, 30, 120, 35);
        colBtn.setFocusable(false);
        colBtn.addActionListener(e -> {
            new CollectionView();
            requestFocusInWindow();
        });
        add(colBtn);

        JButton backBtn = new JButton("Back to Land");
        backBtn.setBounds(270, 30, 130, 35);
        backBtn.setFocusable(false);
        backBtn.setBackground(new Color(255, 210, 210));
        backBtn.addActionListener(e -> {
            backToLandAction.actionPerformed(e);
            requestFocusInWindow();
        });
        add(backBtn);
    }

    private void setupControls(ActionListener backToLandAction) {
        addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();

                if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) {
                    leftPressed = true;
                } else if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) {
                    rightPressed = true;
                } else if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
                    upPressed = true;
                } else if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) {
                    downPressed = true;
                } else if (code == KeyEvent.VK_SPACE) {
                    fire();
                } else if (code == KeyEvent.VK_1) {
                    currentWeapon = new Weapon("初級魚槍", 1, 600);
                } else if (code == KeyEvent.VK_2) {
                    currentWeapon = new Weapon("重型弩砲", 3, 1000);
                } else if (code == KeyEvent.VK_ENTER) {
                    checkReturnToLand(backToLandAction);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int code = e.getKeyCode();

                if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) {
                    leftPressed = false;
                } else if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) {
                    rightPressed = false;
                } else if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
                    upPressed = false;
                } else if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) {
                    downPressed = false;
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateAimAngle(e.getX(), e.getY());
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                updateAimAngle(e.getX(), e.getY());
                fire();
            }
        });
    }

    private void setupGameLoop() {
        gameTimer = new Timer(16, e -> {
            if (isShowing()) {
                updateGame();
                repaint();
            }
        });

        gameTimer.start();
    }

    private void spawnFish() {
        String[][] data = {
            {"沙丁魚", "0.1", "50", "assets/fish_anchovy.png", "1", "1"},
            {"金魚", "0.3", "150", "assets/fish_goldfish.png", "1", "1"},
            {"小丑魚", "0.5", "200", "assets/fish_clownfish.png", "2", "2"},
            {"螃蟹", "0.8", "400", "assets/fish_crab.png", "3", "2"},
            {"神仙魚", "0.7", "450", "assets/fish_angelfish.png", "3", "3"},
            {"河豚", "0.4", "300", "assets/fish_pufferfish.png", "2", "3"},
            {"藍倒吊", "0.6", "350", "assets/fish_surgefish.png", "2", "4"},
            {"綠鰻魚", "1.2", "600", "assets/fish_green.png", "4", "4"}
        };

        for (String[] d : data) {
            for (int i = 0; i < 6; i++) {
                double x = 120 + random.nextInt(Math.max(1, worldWidth - 240));
                double y = 520 + random.nextInt(Math.max(1, worldHeight - 700));

                fishList.add(new OceanFish(
                    d[0],
                    Double.parseDouble(d[1]),
                    Integer.parseInt(d[2]),
                    d[3],
                    Integer.parseInt(d[4]),
                    Integer.parseInt(d[5]),
                    x,
                    y
                ));
            }
        }
    }

    private void updateGame() {
        updatePlayerMovement();
        updateCamera();
        updateFish();
        updateBullets();
        checkCatchFish();
    }

    private void updatePlayerMovement() {
        double dx = 0;
        double dy = 0;

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

        if (dx != 0 || dy != 0) {
            double length = Math.sqrt(dx * dx + dy * dy);
            dx /= length;
            dy /= length;

            playerX += dx * PLAYER_SPEED;
            playerY += dy * PLAYER_SPEED;
        }

        playerX = clamp(playerX, 0, worldWidth - PLAYER_WIDTH);
        playerY = clamp(playerY, 0, worldHeight - PLAYER_HEIGHT);
    }

    private void updateCamera() {
        cameraX = (int) (playerX + PLAYER_WIDTH / 2.0 - SCREEN_WIDTH / 2.0);
        cameraY = (int) (playerY + PLAYER_HEIGHT / 2.0 - SCREEN_HEIGHT / 2.0);

        cameraX = (int) clamp(cameraX, 0, Math.max(0, worldWidth - SCREEN_WIDTH));
        cameraY = (int) clamp(cameraY, 0, Math.max(0, worldHeight - SCREEN_HEIGHT));
    }

    private void updateFish() {
        for (OceanFish f : fishList) {
            f.update();
        }
    }

    private void updateBullets() {
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.move();

            if (b.getX() < 0 || b.getX() > worldWidth ||
                b.getY() < 0 || b.getY() > worldHeight ||
                b.isOutOfRange()) {
                bullets.remove(i);
                continue;
            }

            for (OceanFish f : fishList) {
                if (!f.dead && b.getBounds().intersects(f.getBounds())) {
                    f.takeDamage(b.getDamage());
                    bullets.remove(i);
                    break;
                }
            }
        }
    }

    private void checkCatchFish() {
        Rectangle playerRect = new Rectangle(
            (int) playerX,
            (int) playerY,
            PLAYER_WIDTH,
            PLAYER_HEIGHT
        );

        for (int i = fishList.size() - 1; i >= 0; i--) {
            OceanFish f = fishList.get(i);

            if (f.dead && playerRect.intersects(f.getBounds())) {
                Fish caughtFish = new Fish(
                    f.name,
                    f.weight,
                    f.price,
                    f.imagePath,
                    f.maxHp,
                    f.rarityStars
                );

                InventoryManager.addFish(caughtFish);
                new CatchFishGame(caughtFish);
                fishList.remove(i);
            }
        }
    }

    private void fire() {
        int bulletX = (int) (playerX + PLAYER_WIDTH / 2.0);
        int bulletY = (int) (playerY + PLAYER_HEIGHT / 2.0);

        bullets.add(new Bullet(bulletX, bulletY, aimAngle, currentWeapon));
    }

    private void updateAimAngle(int mouseScreenX, int mouseScreenY) {
        double mouseWorldX = mouseScreenX + cameraX;
        double mouseWorldY = mouseScreenY + cameraY;

        double dx = mouseWorldX - (playerX + PLAYER_WIDTH / 2.0);
        double dy = mouseWorldY - (playerY + PLAYER_HEIGHT / 2.0);

        aimAngle = Math.toDegrees(Math.atan2(dy, dx));
    }

    private void checkReturnToLand(ActionListener backToLandAction) {
        Rectangle playerRect = new Rectangle(
            (int) playerX,
            (int) playerY,
            PLAYER_WIDTH,
            PLAYER_HEIGHT
        );

        if (playerRect.intersects(shipReturnRect)) {
            backToLandAction.actionPerformed(
                new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "backToLand")
            );
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawOceanMap(g);
        drawFish(g);
        drawBullets(g);
        drawPlayer(g);
        drawUI(g);
    }

    private void drawOceanMap(Graphics g) {
        if (oceanMap != null) {
            g.drawImage(
                oceanMap,
                -cameraX,
                -cameraY,
                worldWidth,
                worldHeight,
                this
            );
        } else {
            Graphics2D g2 = (Graphics2D) g;

            GradientPaint gradient = new GradientPaint(
                0, 0, new Color(20, 130, 180),
                0, SCREEN_HEIGHT, new Color(0, 20, 70)
            );

            g2.setPaint(gradient);
            g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        }
    }

    private void drawFish(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        for (OceanFish f : fishList) {
            int sx = (int) f.x - cameraX;
            int sy = (int) f.y - cameraY;

            if (sx < -150 || sx > SCREEN_WIDTH + 150 ||
                sy < -150 || sy > SCREEN_HEIGHT + 150) {
                continue;
            }

            ImageIcon icon = new ImageIcon(f.imagePath);

            if (icon.getIconWidth() > 0) {
                if (f.dead) {
                    Composite old = g2.getComposite();
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
                    g2.drawImage(icon.getImage(), sx, sy, f.size, f.size, this);
                    g2.setComposite(old);
                } else if (f.facingRight) {
                    g2.drawImage(icon.getImage(), sx, sy, f.size, f.size, this);
                } else {
                    g2.drawImage(icon.getImage(), sx + f.size, sy, -f.size, f.size, this);
                }
            } else {
                g.setColor(Color.ORANGE);
                g.fillOval(sx, sy, f.size, f.size / 2);
            }
        }
    }

    private void drawBullets(Graphics g) {
        g.setColor(Color.YELLOW);

        for (Bullet b : bullets) {
            int sx = b.getX() - cameraX;
            int sy = b.getY() - cameraY;

            if (sx > -20 && sx < SCREEN_WIDTH + 20 &&
                sy > -20 && sy < SCREEN_HEIGHT + 20) {
                g.fillOval(sx, sy, 10, 10);
            }
        }
    }

    private void drawPlayer(Graphics g) {
        int sx = (int) playerX - cameraX;
        int sy = (int) playerY - cameraY;

        if (diverSheet != null) {
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
        } else {
            g.setColor(Color.YELLOW);
            g.fillRect(sx, sy, PLAYER_WIDTH, PLAYER_HEIGHT);
        }

        int centerX = sx + PLAYER_WIDTH / 2;
        int centerY = sy + PLAYER_HEIGHT / 2;

        int aimX = centerX + (int) (Math.cos(Math.toRadians(aimAngle)) * 45);
        int aimY = centerY + (int) (Math.sin(Math.toRadians(aimAngle)) * 45);

        g.setColor(Color.YELLOW);
        g.drawLine(centerX, centerY, aimX, aimY);
    }

    private void drawUI(Graphics g) {
        int depth = Math.max(0, (int) playerY - 300);

        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(30, 80, 390, 120, 15, 15);

        g.setColor(Color.CYAN);
        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        g.drawString("Depth: " + depth + " m", 50, 110);
        g.drawString("Weapon: " + currentWeapon.getName(), 50, 140);
        g.drawString("WASD / Arrow: Move   Space/Click: Fire", 50, 170);

        Rectangle playerRect = new Rectangle(
            (int) playerX,
            (int) playerY,
            PLAYER_WIDTH,
            PLAYER_HEIGHT
        );

        if (playerRect.intersects(shipReturnRect)) {
            g.setColor(new Color(0, 0, 0, 170));
            g.fillRoundRect(SCREEN_WIDTH / 2 - 270, SCREEN_HEIGHT - 90, 540, 50, 15, 15);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Monospaced", Font.BOLD, 22));
            g.drawString("Press Enter to return to land", SCREEN_WIDTH / 2 - 220, SCREEN_HEIGHT - 58);
        }
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    @Override
    public void addNotify() {
        super.addNotify();

        SwingUtilities.invokeLater(() -> requestFocusInWindow());
    }

    private class OceanFish {

        String name;
        double weight;
        int price;
        String imagePath;
        int maxHp;
        int hp;
        int rarityStars;

        double x;
        double y;
        double vx;
        double vy;

        int size;
        boolean dead = false;
        boolean facingRight = true;

        OceanFish(
            String name,
            double weight,
            int price,
            String imagePath,
            int maxHp,
            int rarityStars,
            double x,
            double y
        ) {
            this.name = name;
            this.weight = weight;
            this.price = price;
            this.imagePath = imagePath;
            this.maxHp = maxHp;
            this.hp = maxHp;
            this.rarityStars = rarityStars;
            this.x = x;
            this.y = y;

            this.size = 45 + rarityStars * 8;
            this.vx = random.nextBoolean() ? 1.5 : -1.5;
            this.vy = -0.5 + random.nextDouble();
        }

        void update() {
            if (dead) {
                return;
            }

            x += vx;
            y += vy;

            if (x < 60 || x > worldWidth - 100) {
                vx *= -1;
            }

            if (y < 460 || y > worldHeight - 100) {
                vy *= -1;
            }

            vy += (random.nextDouble() - 0.5) * 0.05;
            vx += (random.nextDouble() - 0.5) * 0.05;

            vx = clamp(vx, -2.5, 2.5);
            vy = clamp(vy, -1.2, 1.2);

            facingRight = vx >= 0;
        }

        void takeDamage(int damage) {
            hp -= damage;

            if (hp <= 0) {
                dead = true;
            }
        }

        Rectangle getBounds() {
            return new Rectangle((int) x, (int) y, size, size);
        }
    }
}