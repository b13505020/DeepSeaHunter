import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayDeque;

public class ShallowOceanWorld extends JPanel {

    public static final int SCREEN_WIDTH = 1600;
    public static final int SCREEN_HEIGHT = 900;

    // 淺水地圖是橫向，所以寬度變長，高度維持 900
    private int worldWidth = 3200;
    private int worldHeight = SCREEN_HEIGHT;

    private int cameraX = 0;
    private int cameraY = 0;

    private double playerX;
    private double playerY;

    private final int PLAYER_WIDTH = 110;
    private final int PLAYER_HEIGHT = 135;
    private final double PLAYER_SPEED = 7.0;

    private int oxygenLevel = 1;
    private double maxOxygenTime = 60.0;
    private double currentOxygen;

    private boolean upPressed = false;
    private boolean downPressed = false;
    private boolean leftPressed = false;
    private boolean rightPressed = false;

    private double aimAngle = 0;
    private boolean isFacingLeft = false;

    private ArrayList<Weapon> weaponList = new ArrayList<>();
    private int currentWeaponIndex = 0;
    private Weapon currentWeapon;

    private int lastMouseX = SCREEN_WIDTH / 2;
    private int lastMouseY = SCREEN_HEIGHT / 2;
    private Map<String, BufferedImage> weaponImageCache = new HashMap<>();

    private ArrayList<Bullet> bullets = new ArrayList<>();
    private ArrayList<ExplosionEffect> explosionEffects = new ArrayList<>();
    private ArrayList<ShallowFish> fishList = new ArrayList<>();

    private Timer gameTimer;
    private Random random = new Random();

    private BufferedImage shallowMap;
    private BufferedImage diverSheet;

    private boolean isShowingReturnDialog = false;

    public ShallowOceanWorld(ActionListener backToLandAction) {
        setLayout(null);
        setFocusable(true);
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));

        loadImages();
        setupWeapons();
        resetPlayerPosition();
        setupButtons();
        setupControls();
        spawnFish();
        setupGameLoop(backToLandAction);
    }

    public void resetPlayerPosition() {
        reloadOwnedWeapons();

        // 淺水區從左邊開始
        this.playerX = 120;
        this.playerY = 430;

        this.oxygenLevel = InventoryManager.getOxygenLevel();
        this.maxOxygenTime = InventoryManager.getMaxOxygenTime();
        this.currentOxygen = maxOxygenTime;

        this.isShowingReturnDialog = false;

        stopMovement();
        updateCamera();
        requestFocusInWindow();
    }

    private void stopMovement() {
        this.upPressed = false;
        this.downPressed = false;
        this.leftPressed = false;
        this.rightPressed = false;
    }

    private void loadImages() {
        try {
            shallowMap = ImageIO.read(new File("assets/shallow_ocean_map.jpeg"));
            diverSheet = ImageIO.read(new File("assets/diver_clean.png"));

            if (shallowMap != null) {
                // 橫向地圖：至少要比螢幕寬，這裡放大成兩倍寬比較好探索
                worldWidth = Math.max(SCREEN_WIDTH * 2, shallowMap.getWidth() * 2);
                worldHeight = SCREEN_HEIGHT;
            }

        } catch (IOException e) {
            System.out.println("❌ 淺水地圖圖片載入失敗，請確認 assets/shallow_ocean_map.jpeg");
        }
    }

    private void setupWeapons() {
        reloadOwnedWeapons();
    }

    private void reloadOwnedWeapons() {
        weaponList.clear();
        weaponList.addAll(WeaponManager.getOwnedWeapons());

        if (weaponList.isEmpty()) {
            weaponList.add(new Weapon("初級魚槍", 1, 600));
        }

        if (currentWeaponIndex < 0 || currentWeaponIndex >= weaponList.size()) {
            currentWeaponIndex = 0;
        }

        currentWeapon = weaponList.get(currentWeaponIndex);
    }

    private void setupButtons() {
        JButton bagBtn = new JButton("Backpack");
        bagBtn.setBounds(30, 30, 100, 35);
        bagBtn.setFocusable(false);
        bagBtn.addActionListener(e -> {
            stopMovement();
            new BackpackView();
            requestFocusInWindow();
        });
        add(bagBtn);

        JButton colBtn = new JButton("Collection");
        colBtn.setBounds(140, 30, 120, 35);
        colBtn.setFocusable(false);
        colBtn.addActionListener(e -> {
            stopMovement();
            new CollectionView();
            requestFocusInWindow();
        });
        add(colBtn);
    }

    private double getScaleX() {
        if (getWidth() <= 0) {
            return 1.0;
        }
        return getWidth() / (double) SCREEN_WIDTH;
    }

    private double getScaleY() {
        if (getHeight() <= 0) {
            return 1.0;
        }
        return getHeight() / (double) SCREEN_HEIGHT;
    }

    private int toGameX(int screenX) {
        return (int) (screenX / getScaleX());
    }

    private int toGameY(int screenY) {
        return (int) (screenY / getScaleY());
    }

    private void setupControls() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();

                if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) {
                    leftPressed = true;
                    isFacingLeft = true;
                } else if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) {
                    rightPressed = true;
                    isFacingLeft = false;
                } else if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
                    upPressed = true;
                } else if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) {
                    downPressed = true;
                } else if (code == KeyEvent.VK_SPACE) {
                    updateAimAngle(lastMouseX, lastMouseY);
                    fire();
                } else if (code == KeyEvent.VK_Q) {
    switchWeapon(-1);
} else if (code == KeyEvent.VK_E) {
    switchWeapon(1);
} else if (code == KeyEvent.VK_ENTER) {
    collectNearbyStarfish();
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
                updateAimAngle(toGameX(e.getX()), toGameY(e.getY()));
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                updateAimAngle(toGameX(e.getX()), toGameY(e.getY()));
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                updateAimAngle(toGameX(e.getX()), toGameY(e.getY()));
                fire();
            }
        });
    }

    private void setupGameLoop(ActionListener backToLandAction) {
        gameTimer = new Timer(16, e -> {
            if (isShowing()) {
                updateGame(backToLandAction);
                repaint();
            }
        });

        gameTimer.start();
    }

    private void switchWeapon(int direction) {
        reloadOwnedWeapons();

        if (weaponList.isEmpty()) {
            return;
        }

        currentWeaponIndex += direction;

        if (currentWeaponIndex < 0) {
            currentWeaponIndex = weaponList.size() - 1;
        } else if (currentWeaponIndex >= weaponList.size()) {
            currentWeaponIndex = 0;
        }

        currentWeapon = weaponList.get(currentWeaponIndex);
    }

    private void spawnFish() {
    fishList.clear();

    // 淺水區專屬魚種
    // 格式：
    // 名稱、重量、價格、圖片路徑、血量、稀有度
    String[][] data = {
        {"銀礁魚", "0.4", "180", "assets/silver_reef.png", "2", "2"},
        {"海星", "0.2", "120", "assets/starfish.png", "2", "1"},
        {"海馬", "0.3", "260", "assets/seahorse.png", "3", "3"},
        {"礁岩蟹", "0.8", "400", "assets/reef_crab.png", "3", "2"},
        {"鸚哥魚", "1.1", "520", "assets/parrotfish.png", "4", "3"},
        {"小丑魚", "0.5", "200", "assets/clownfish.png", "2", "2"},
        {"蝶魚", "0.6", "320", "assets/butterflyfish.png", "3", "3"},
        {"箱魨", "1.0", "650", "assets/boxfish.png", "5", "4"}
    };

    for (String[] fishData : data) {
        for (int i = 0; i < 5; i++) {
            double x = 250 + random.nextInt(Math.max(1, worldWidth - 500));

            double y;

            // 螃蟹跟海星都在海底活動
            if (
                fishData[0].equals("礁岩蟹")
                || fishData[0].equals("海星")
            ) {
                y = worldHeight - 145;
            } else {
                y = 230 + random.nextInt(500);
            }

            fishList.add(
                new ShallowFish(
                    fishData[0],
                    Double.parseDouble(fishData[1]),
                    Integer.parseInt(fishData[2]),
                    fishData[3],
                    Integer.parseInt(fishData[4]),
                    Integer.parseInt(fishData[5]),
                    x,
                    y
                )
            );
        }
    }
}

    private void updateGame(ActionListener backAction) {
        updatePlayerMovement();
        updateCamera();

        if (!isShowingReturnDialog) {
            currentOxygen -= 0.016;

            if (currentOxygen <= 0) {
                currentOxygen = 0;
                handleOxygenOut(backAction);
            }
        }

        for (ShallowFish f : fishList) {
            double dist = Math.sqrt(
                Math.pow(f.x - playerX, 2)
                + Math.pow(f.y - playerY, 2)
            );

            if (dist < 180) {
                f.triggerScared();
            }

            f.update();
        }

        updateBullets();
        checkCatchFish();
        checkSurfaceInteraction(backAction);
    }

    private void handleOxygenOut(ActionListener backAction) {
        isShowingReturnDialog = true;
        stopMovement();

        JOptionPane.showMessageDialog(
            this,
            "氧氣耗盡！本次潛水物資遺失。",
            "緊急情況",
            JOptionPane.ERROR_MESSAGE
        );

        InventoryManager.clearCurrentDive();

        backAction.actionPerformed(new ActionEvent(this, 0, "backToLand"));

        isShowingReturnDialog = false;
    }

    private void updatePlayerMovement() {
        double dx = 0;
        double dy = 0;

        if (leftPressed) dx -= 1;
        if (rightPressed) dx += 1;
        if (upPressed) dy -= 1;
        if (downPressed) dy += 1;

        if (dx != 0 || dy != 0) {
            double len = Math.sqrt(dx * dx + dy * dy);

            playerX += (dx / len) * PLAYER_SPEED;
            playerY += (dy / len) * PLAYER_SPEED;
        }

        playerX = clamp(playerX, 0, worldWidth - PLAYER_WIDTH);

        // 淺水區不需要像深海一樣往下 2800，只限制在畫面內
        playerY = clamp(playerY, 120, worldHeight - PLAYER_HEIGHT);
    }

    private void updateCamera() {
        cameraX = (int) clamp(
            playerX + PLAYER_WIDTH / 2.0 - SCREEN_WIDTH / 2.0,
            0,
            worldWidth - SCREEN_WIDTH
        );

        // 淺水地圖固定橫向，不上下捲
        cameraY = 0;
    }

    private void updateBullets() {
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.move();

            if (b.isOutOfRange()) {
                bullets.remove(i);
                continue;
            }

            for (ShallowFish f : fishList) {
                if (!f.dead && b.getBounds().intersects(f.getBounds())) {
                    applyBulletEffect(b, f);

                    if (b.isPiercingBullet()) {
                        b.registerPierceHit();

                        if (!b.canPierceAfterHit()) {
                            bullets.remove(i);
                        }
                    } else {
                        bullets.remove(i);
                    }

                    break;
                }
            }
        }
    }

    private void applyBulletEffect(Bullet bullet, ShallowFish target) {
        if (bullet.isExplosionBullet()) {
            applyExplosionBullet(bullet);
            return;
        }

        if (bullet.isNetBullet()) {
            applyNetBullet(bullet);
            return;
        }

        target.takeDamage(bullet.getDamage());

        if (bullet.getSleepDurationMs() > 0) {
            if (bullet.getWeaponName().equals("麻醉槍")) {
                target.tranquilizedUntil = Math.max(
                    target.tranquilizedUntil,
                    System.currentTimeMillis() + bullet.getSleepDurationMs()
                );
            } else {
                target.sleepUntil = Math.max(
                    target.sleepUntil,
                    System.currentTimeMillis() + bullet.getSleepDurationMs()
                );
            }
        }

        if (bullet.getSlowDurationMs() > 0) {
            target.slowUntil = Math.max(
                target.slowUntil,
                System.currentTimeMillis() + bullet.getSlowDurationMs()
            );
        }
    }

    private void applyExplosionBullet(Bullet bullet) {
        int radius = bullet.getExplosionRadius();
        int bx = bullet.getX();
        int by = bullet.getY();

        explosionEffects.add(new ExplosionEffect(bx, by, radius));

        for (ShallowFish f : fishList) {
            if (f.dead) {
                continue;
            }

            Rectangle bounds = f.getBounds();
            double dx = bounds.getCenterX() - bx;
            double dy = bounds.getCenterY() - by;

            if (Math.sqrt(dx * dx + dy * dy) <= radius) {
                f.takeDamage(bullet.getDamage());
            }
        }
    }

    private void applyNetBullet(Bullet bullet) {
        int radius = bullet.getNetRadius();
        int bx = bullet.getX();
        int by = bullet.getY();

        for (ShallowFish f : fishList) {
            if (f.dead) {
                continue;
            }

            Rectangle bounds = f.getBounds();
            double dx = bounds.getCenterX() - bx;
            double dy = bounds.getCenterY() - by;

            if (Math.sqrt(dx * dx + dy * dy) <= radius) {
                f.netted = true;
                f.nettedAt = System.currentTimeMillis();
                f.dead = true;
                f.vx = 0;
                f.vy = 0;
            }
        }
    }
private void collectNearbyStarfish() {
    Rectangle pRect = new Rectangle(
        (int) playerX,
        (int) playerY,
        PLAYER_WIDTH,
        PLAYER_HEIGHT
    );

    for (int i = fishList.size() - 1; i >= 0; i--) {
        ShallowFish f = fishList.get(i);

        if (!f.name.equals("海星")) {
            continue;
        }

        Rectangle collectBox = f.getBounds();
        collectBox.grow(70, 70);

        if (pRect.intersects(collectBox)) {
            Fish collected = new Fish(
                "海星",
                f.weight,
                f.price,
                f.imagePath,
                f.maxHp,
                f.rarityStars
            );

            boolean success = InventoryManager.addFish(collected);

            if (success) {
                CollectionManager.unlock(collected.getName());
                fishList.remove(i);

                JOptionPane.showMessageDialog(
                    this,
                    "取得補貨素材：海星"
                );
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "背包已滿！請先返回陸地結算或升級背包。"
                );
            }

            requestFocusInWindow();
            return;
        }
    }

    JOptionPane.showMessageDialog(
        this,
        "附近沒有可以補貨的海星。"
    );

    requestFocusInWindow();
}
    private void checkCatchFish() {
        Rectangle pRect = new Rectangle(
            (int) playerX,
            (int) playerY,
            PLAYER_WIDTH,
            PLAYER_HEIGHT
        );

        for (int i = fishList.size() - 1; i >= 0; i--) {
            ShallowFish f = fishList.get(i);

if (f.name.equals("海星")) {
    continue;
}

if (f.dead && pRect.intersects(f.getBounds())) {
                stopMovement();

                Fish caught = new Fish(
                    f.name,
                    f.weight,
                    f.price,
                    f.imagePath,
                    f.maxHp,
                    f.rarityStars
                );

                boolean success = InventoryManager.addFish(caught);

                if (success) {
                    CollectionManager.unlock(caught.getName());
                    new CatchFishGame(caught);
                    fishList.remove(i);
                } else {
                    JOptionPane.showMessageDialog(
                        this,
                        "背包已滿！請先返回陸地結算，或去商店升級背包。"
                    );

                    pushPlayerAwayFromFish(f);
                    requestFocusInWindow();
                }

                break;
            }
        }
    }

    private void pushPlayerAwayFromFish(ShallowFish f) {
        Rectangle fishRect = f.getBounds();

        double playerCenterX = playerX + PLAYER_WIDTH / 2.0;
        double playerCenterY = playerY + PLAYER_HEIGHT / 2.0;

        double fishCenterX = fishRect.getCenterX();
        double fishCenterY = fishRect.getCenterY();

        double dx = playerCenterX - fishCenterX;
        double dy = playerCenterY - fishCenterY;

        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance == 0) {
            dx = -1;
            dy = 0;
            distance = 1;
        }

        dx /= distance;
        dy /= distance;

        double pushDistance = 220;

        playerX += dx * pushDistance;
        playerY += dy * pushDistance;

        playerX = clamp(playerX, 0, worldWidth - PLAYER_WIDTH);
        playerY = clamp(playerY, 120, worldHeight - PLAYER_HEIGHT);

        updateCamera();
    }

    private void checkSurfaceInteraction(ActionListener backAction) {
        if (isShowingReturnDialog) {
            return;
        }

        // 淺水區一樣往上游到水面就可以上岸
        if (playerY <= 135 && upPressed) {
            isShowingReturnDialog = true;
            stopMovement();

            int result = JOptionPane.showConfirmDialog(
                this,
                "成功返回陸地？本次抓到的魚會放入永久儲藏箱。",
                "結算上岸",
                JOptionPane.YES_NO_OPTION
            );

            if (result == JOptionPane.YES_OPTION) {
                InventoryManager.moveToStorage();
                InventoryManager.saveGame();
                backAction.actionPerformed(null);
            } else {
                playerY = 430;
            }

            isShowingReturnDialog = false;
            requestFocusInWindow();
        }
    }

    private void fire() {
        if (currentWeapon == null) {
            return;
        }

        bullets.add(
            new Bullet(
                (int) (playerX + PLAYER_WIDTH / 2),
                (int) (playerY + PLAYER_HEIGHT / 2),
                aimAngle,
                currentWeapon
            )
        );
    }

    private void updateAimAngle(int mx, int my) {
        lastMouseX = mx;
        lastMouseY = my;

        double worldMX = mx + cameraX;
        double worldMY = my + cameraY;

        double playerCenterX = playerX + PLAYER_WIDTH / 2.0;
        double playerCenterY = playerY + PLAYER_HEIGHT / 2.0;

        double dx = worldMX - playerCenterX;
        double dy = worldMY - playerCenterY;

        aimAngle = Math.toDegrees(Math.atan2(dy, dx));

        if (dx < 0) {
            isFacingLeft = true;
        } else if (dx > 0) {
            isFacingLeft = false;
        }
    }

    private void drawCurrentWeapon(Graphics2D g2d, int playerScreenX, int playerScreenY) {
        if (currentWeapon == null) {
            return;
        }

        BufferedImage weaponImg = getWeaponImage(currentWeapon);

        if (weaponImg == null) {
            return;
        }

        int weaponW = 110;
        int weaponH = 55;

        String weaponName = currentWeapon.getName();

        if (weaponName.equals("狙擊槍")) {
            weaponW = 165;
            weaponH = 55;
        } else if (weaponName.equals("水下步槍")) {
            weaponW = 145;
            weaponH = 55;
        } else if (weaponName.equals("榴彈發射器")) {
            weaponW = 135;
            weaponH = 65;
        } else if (weaponName.equals("網槍")) {
            weaponW = 130;
            weaponH = 60;
        } else if (weaponName.equals("寒冰槍")) {
            weaponW = 130;
            weaponH = 60;
        } else if (weaponName.equals("初級魚槍")) {
            weaponW = 95;
            weaponH = 45;
        }

        int centerX = playerScreenX + PLAYER_WIDTH / 2;
        int centerY = playerScreenY + PLAYER_HEIGHT / 2 + 8;

        Graphics2D gWeapon = (Graphics2D) g2d.create();

        gWeapon.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );

        gWeapon.translate(centerX, centerY);
        gWeapon.rotate(Math.toRadians(aimAngle));

        gWeapon.drawImage(
            weaponImg,
            8,
            -weaponH / 2,
            weaponW,
            weaponH,
            this
        );

        gWeapon.dispose();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();

        double scaleX = getWidth() / (double) SCREEN_WIDTH;
        double scaleY = getHeight() / (double) SCREEN_HEIGHT;

        g2d.scale(scaleX, scaleY);

        if (shallowMap != null) {
            g2d.drawImage(
                shallowMap,
                -cameraX,
                0,
                worldWidth,
                SCREEN_HEIGHT,
                this
            );
        } else {
            g2d.setColor(new Color(70, 180, 210));
            g2d.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        }

        for (ShallowFish f : fishList) {
            int sx = (int) f.x - cameraX;
            int sy = (int) f.y - cameraY;
            long now = System.currentTimeMillis();
            int struggleX = 0;
            int struggleY = 0;

            if (f.netted && now - f.nettedAt < 2500) {
                struggleX = (int) Math.round(Math.sin(now * 0.04) * 6);
                struggleY = (int) Math.round(Math.cos(now * 0.05) * 4);
            }

            sx += struggleX;
            sy += struggleY;

            ImageIcon icon = new ImageIcon(f.imagePath);

            if (f.dead) {
                g2d.setComposite(
                    AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER,
                        0.5f
                    )
                );
            }

            if (f.facingRight) {
                g2d.drawImage(
                    icon.getImage(),
                    sx,
                    sy,
                    f.size,
                    (int) (f.size * 0.75),
                    this
                );
            } else {
                g2d.drawImage(
                    icon.getImage(),
                    sx + f.size,
                    sy,
                    -f.size,
                    (int) (f.size * 0.75),
                    this
                );
            }

            g2d.setComposite(
                AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER,
                    1.0f
                )
            );

            drawFishWeaponEffects(g2d, sx, sy, f);
        }

        drawExplosionEffects(g2d);

        for (Bullet b : bullets) {
            b.draw(g2d, cameraX, cameraY);
        }

        int sx = (int) playerX - cameraX;
        int sy = (int) playerY - cameraY;

        if (diverSheet != null) {
            if (isFacingLeft) {
                g2d.drawImage(
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
                g2d.drawImage(
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
            g2d.setColor(Color.ORANGE);
            g2d.fillRect(sx, sy, PLAYER_WIDTH, PLAYER_HEIGHT);
        }

        drawCurrentWeapon(g2d, sx, sy);
        drawUI(g2d);
        drawStarfishHint(g2d);

        g2d.dispose();
    }

    private BufferedImage getWeaponImage(Weapon weapon) {
        String imagePath = WeaponManager.getImagePath(weapon);

        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }

        if (weaponImageCache.containsKey(imagePath)) {
            return weaponImageCache.get(imagePath);
        }

        try {
            File weaponFile = new File(imagePath);

            if (!weaponFile.exists()) {
                System.out.println("找不到武器圖片：" + weaponFile.getAbsolutePath());
                return null;
            }

            BufferedImage original = ImageIO.read(weaponFile);
            BufferedImage transparent = makeEdgeBackgroundTransparent(original);

            weaponImageCache.put(imagePath, transparent);
            return transparent;

        } catch (Exception e) {
            System.out.println("武器圖片載入失敗：" + imagePath);
            e.printStackTrace();
            return null;
        }
    }

    private BufferedImage makeEdgeBackgroundTransparent(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();

        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.drawImage(src, 0, 0, null);
        g2.dispose();

        boolean[][] visited = new boolean[w][h];
        ArrayDeque<int[]> queue = new ArrayDeque<>();

        for (int x = 0; x < w; x++) {
            queue.add(new int[] { x, 0 });
            queue.add(new int[] { x, h - 1 });
        }

        for (int y = 0; y < h; y++) {
            queue.add(new int[] { 0, y });
            queue.add(new int[] { w - 1, y });
        }

        int[] dx = { 1, -1, 0, 0 };
        int[] dy = { 0, 0, 1, -1 };

        while (!queue.isEmpty()) {
            int[] p = queue.removeFirst();
            int x = p[0];
            int y = p[1];

            if (x < 0 || x >= w || y < 0 || y >= h) {
                continue;
            }

            if (visited[x][y]) {
                continue;
            }

            visited[x][y] = true;

            int argb = result.getRGB(x, y);

            if (!isBackgroundLike(argb)) {
                continue;
            }

            result.setRGB(x, y, argb & 0x00FFFFFF);

            for (int i = 0; i < 4; i++) {
                queue.add(new int[] { x + dx[i], y + dy[i] });
            }
        }

        return result;
    }

    private boolean isBackgroundLike(int argb) {
        int a = (argb >> 24) & 0xff;
        int r = (argb >> 16) & 0xff;
        int g = (argb >> 8) & 0xff;
        int b = argb & 0xff;

        if (a < 10) {
            return true;
        }

        if (r > 235 && g > 235 && b > 235) {
            return true;
        }

        if (Math.abs(r - g) < 8 && Math.abs(g - b) < 8 && r >= 170 && r <= 245) {
            return true;
        }

        return false;
    }

    private void drawStarfishHint(Graphics2D g2d) {
    ShallowFish near = getNearbyStarfish();

    if (near == null) {
        return;
    }

    int boxW = 360;
    int boxH = 48;
    int boxX = SCREEN_WIDTH / 2 - boxW / 2;
    int boxY = SCREEN_HEIGHT - 120;

    g2d.setColor(new Color(0, 0, 0, 170));
    g2d.fillRoundRect(boxX, boxY, boxW, boxH, 16, 16);

    g2d.setColor(new Color(255, 235, 150));
    g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 20));
    g2d.drawString("按 ENTER 補貨：海星", boxX + 75, boxY + 31);
}
private ShallowFish getNearbyStarfish() {
    Rectangle pRect = new Rectangle(
        (int) playerX,
        (int) playerY,
        PLAYER_WIDTH,
        PLAYER_HEIGHT
    );

    for (ShallowFish f : fishList) {
        if (!f.name.equals("海星")) {
            continue;
        }

        Rectangle collectBox = f.getBounds();
        collectBox.grow(70, 70);

        if (pRect.intersects(collectBox)) {
            return f;
        }
    }

    return null;
}
    private void drawUI(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRoundRect(30, 80, 380, 200, 20, 20);

        g2d.setFont(new Font("Monospaced", Font.BOLD, 18));

        g2d.setColor(Color.CYAN);
        g2d.drawString(
            "Area  : Shallow Ocean",
            50,
            110
        );

        g2d.setColor(Color.CYAN);
        g2d.drawString(
            "Depth : " + Math.max(0, (int) playerY - 120) + " m",
            50,
            135
        );

        g2d.setColor(Color.WHITE);
        g2d.drawString("Weapon: " + currentWeapon.getName(), 50, 160);

        g2d.setColor(Color.ORANGE);
        g2d.drawString("Oxygen: Lv." + oxygenLevel, 50, 185);

        if (currentOxygen <= 10) {
            g2d.setColor(Color.RED);
        } else {
            g2d.setColor(Color.GREEN);
        }

        g2d.drawString(
            "Time  : " + String.format("%.1f", currentOxygen) + " s",
            50,
            210
        );

        g2d.drawRect(50, 220, 200, 10);
        g2d.fillRect(
            50,
            220,
            (int) ((currentOxygen / maxOxygenTime) * 200),
            10
        );

        g2d.setColor(Color.PINK);
        g2d.drawString(
            "Bag   : " + InventoryManager.getMyBackpack().size()
            + " / " + InventoryManager.getBackpackCapacity(),
            50,
            255
        );

        g2d.setColor(new Color(255, 240, 180));
        g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
        g2d.drawString("往上游到水面可返回陸地", 50, 305);
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(v, max));
    }

    private class ShallowFish {
        String name;
        String imagePath;
        double weight;
        int price;
        int maxHp;
        int hp;
        int rarityStars;
        int size;

        double x;
        double y;
        double vx;
        double vy;

        boolean dead;
        boolean facingRight = true;
        boolean isScared;

        long scaredStartTime;
        long sleepUntil;
        long tranquilizedUntil;
        long slowUntil;
        long nettedAt;
        boolean netted;

        private final double MAX_SCARE_BOOST = 3.5;

        ShallowFish(
            String n,
            double w,
            int p,
            String img,
            int m,
            int r,
            double x,
            double y
        ) {
            this.name = n;
this.weight = w;
this.price = p;
this.imagePath = img;
this.maxHp = m;
this.hp = m;
this.rarityStars = r;
this.x = x;
this.y = y;

// 淺水區魚放大
this.size = 125 + r * 30;

// 特定魚種大小微調
if (name.equals("海馬")) {
    this.size = 165;
} else if (name.equals("海星")) {
    this.size = 170;
} else if (name.equals("礁岩蟹")) {
    this.size = 180;
} else if (name.equals("箱魨")) {
    this.size = 165;
}

// 預設游動速度
vx = random.nextBoolean() ? 1.5 : -1.5;
vy = random.nextDouble() - 0.5;

// 特定魚種移動方式
if (name.equals("礁岩蟹")) {
    // 螃蟹只在海底左右走
    vx = random.nextBoolean() ? 0.8 : -0.8;
    vy = 0;
} else if (name.equals("海星")) {
    // 海星固定在原位，不移動
    vx = 0;
    vy = 0;
} else if (name.equals("海馬")) {
    // 海馬慢慢漂
    vx = random.nextBoolean() ? 0.55 : -0.55;
    vy = random.nextDouble() - 0.5;
}
        }

        void update() {
	    if (dead) {
	        return;
	    }

	    if (name.equals("海星")) {
	        // 海星固定在生成的位置，不移動
	        return;
	    }

            long now = System.currentTimeMillis();

            if (now < sleepUntil || now < tranquilizedUntil) {
                return;
            }

	            double slowMult = now < slowUntil ? 0.35 : 1.0;
	            double mult = (isScared ? MAX_SCARE_BOOST : 1.0) * slowMult;

	            if (
	                isScared
	                && now - scaredStartTime > 1000
	            ) {
	                isScared = false;
	            }

            x += vx * mult;

            if (name.equals("螃蟹")) {
                y = worldHeight - 145;
            } else {
                y += vy;

                if (y < 170 || y > worldHeight - 110) {
                    vy *= -1;
                }
            }

            if (x < 60 || x > worldWidth - 100) {
                vx *= -1;
            }

            facingRight = (vx * mult) >= 0;
        }

        void triggerScared() {
            if (!isScared) {
                isScared = true;
                vx *= -1;
                scaredStartTime = System.currentTimeMillis();
            }
        }

        void takeDamage(int d) {
            hp -= d;
            triggerScared();

            if (hp <= 0) {
                dead = true;
            }
        }

        Rectangle getBounds() {
            return new Rectangle((int) x, (int) y, size, size);
        }
    }

    private void drawFishWeaponEffects(Graphics2D g2d, int sx, int sy, ShallowFish fish) {
        int fishW = fish.size;
        int fishH = (int) (fish.size * 0.75);
        long now = System.currentTimeMillis();

        if (now < fish.sleepUntil) {
            Graphics2D sleepG = (Graphics2D) g2d.create();
            sleepG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            sleepG.setFont(new Font("Monospaced", Font.BOLD, 28));
            sleepG.setColor(new Color(225, 225, 255, 225));

            int bob = (int) Math.round(Math.sin(now * 0.006) * 6);
            sleepG.drawString("Zzz", sx + fishW / 2 - 24, sy - 16 + bob);
            sleepG.dispose();
        }

        if (now < fish.tranquilizedUntil) {
            Graphics2D tranqG = (Graphics2D) g2d.create();
            tranqG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int pulse = (int) Math.round(Math.sin(now * 0.008) * 8);
            tranqG.setColor(new Color(255, 120, 210, 90));
            tranqG.fillOval(sx - 10 - pulse, sy - 10 - pulse, fishW + 20 + pulse * 2, fishH + 20 + pulse * 2);

            tranqG.setStroke(new BasicStroke(4));
            tranqG.setColor(new Color(255, 170, 235, 210));
            tranqG.drawOval(sx - 8 - pulse, sy - 8 - pulse, fishW + 16 + pulse * 2, fishH + 16 + pulse * 2);

            tranqG.setFont(new Font("Monospaced", Font.BOLD, 28));
            tranqG.setColor(new Color(255, 215, 245, 230));
            int markX = sx + fishW / 2 - 24;
            int markY = sy - 16;
            tranqG.drawString("+ +", markX, markY);

            int cx = sx + fishW / 2;
            int cy = sy + fishH / 2;
            for (int i = 0; i < 6; i++) {
                double angle = now * 0.006 + i * Math.PI * 2 / 6.0;
                int dotX = cx + (int) (Math.cos(angle) * (fishW * 0.48));
                int dotY = cy + (int) (Math.sin(angle) * (fishH * 0.48));
                tranqG.fillOval(dotX - 5, dotY - 5, 10, 10);
            }

            tranqG.dispose();
        }

        if (now < fish.slowUntil) {
            Graphics2D iceG = (Graphics2D) g2d.create();
            iceG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            iceG.setColor(new Color(120, 225, 255, 95));
            iceG.fillOval(sx - 10, sy - 10, fishW + 20, fishH + 20);

            iceG.setColor(new Color(210, 250, 255, 190));
            iceG.setStroke(new BasicStroke(4));
            iceG.drawOval(sx - 8, sy - 8, fishW + 16, fishH + 16);

            int cx = sx + fishW / 2;
            int cy = sy + fishH / 2;
            for (int i = 0; i < 7; i++) {
                double angle = Math.PI * 2 * i / 7.0 + now * 0.004;
                int x1 = cx + (int) (Math.cos(angle) * fishW * 0.16);
                int y1 = cy + (int) (Math.sin(angle) * fishH * 0.16);
                int x2 = cx + (int) (Math.cos(angle) * fishW * 0.55);
                int y2 = cy + (int) (Math.sin(angle) * fishH * 0.55);
                iceG.drawLine(x1, y1, x2, y2);
            }

            iceG.dispose();
        }

        if (fish.netted) {
            Graphics2D netG = (Graphics2D) g2d.create();
            netG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            netG.setColor(new Color(235, 245, 225, 185));
            netG.setStroke(new BasicStroke(3));
            int netSize = Math.max(fishW, fishH) + 42;
            int netX = sx + fishW / 2 - netSize / 2;
            int netY = sy + fishH / 2 - netSize / 2;
            netG.drawOval(netX, netY, netSize, netSize);
            netG.drawOval(netX + netSize / 4, netY + netSize / 4, netSize / 2, netSize / 2);

            int cx = netX + netSize / 2;
            int cy = netY + netSize / 2;
            for (int i = 0; i < 9; i++) {
                double angle = Math.PI * 2 * i / 9.0;
                int ex = cx + (int) (Math.cos(angle) * netSize / 2);
                int ey = cy + (int) (Math.sin(angle) * netSize / 2);
                netG.drawLine(cx, cy, ex, ey);
            }

            for (int offset = -netSize / 3; offset <= netSize / 3; offset += 24) {
                netG.drawArc(netX + offset, netY, netSize, netSize, 70, 40);
                netG.drawArc(netX + offset, netY, netSize, netSize, 250, 40);
                netG.drawArc(netX, netY + offset, netSize, netSize, -20, 40);
                netG.drawArc(netX, netY + offset, netSize, netSize, 160, 40);
            }

            netG.dispose();
        }
    }

    private void drawExplosionEffects(Graphics2D g2d) {
        long now = System.currentTimeMillis();

        for (int i = explosionEffects.size() - 1; i >= 0; i--) {
            ExplosionEffect effect = explosionEffects.get(i);
            double progress = (now - effect.startTime) / 520.0;

            if (progress >= 1.0) {
                explosionEffects.remove(i);
                continue;
            }

            int x = effect.x - cameraX;
            int y = effect.y - cameraY;
            int radius = (int) (effect.radius * (0.35 + progress * 0.85));
            int alpha = (int) (220 * (1.0 - progress));

            Graphics2D fireG = (Graphics2D) g2d.create();
            fireG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            fireG.setColor(new Color(255, 80, 20, Math.max(0, alpha)));
            fireG.fillOval(x - radius, y - radius, radius * 2, radius * 2);

            fireG.setColor(new Color(255, 190, 40, Math.max(0, alpha)));
            fireG.fillOval(x - radius / 2, y - radius / 2, radius, radius);

            fireG.setStroke(new BasicStroke(5));
            fireG.setColor(new Color(255, 230, 120, Math.max(0, alpha)));
            for (int spark = 0; spark < 12; spark++) {
                double angle = Math.PI * 2 * spark / 12.0 + progress * 2.0;
                int x1 = x + (int) (Math.cos(angle) * radius * 0.35);
                int y1 = y + (int) (Math.sin(angle) * radius * 0.35);
                int x2 = x + (int) (Math.cos(angle) * radius * 1.15);
                int y2 = y + (int) (Math.sin(angle) * radius * 1.15);
                fireG.drawLine(x1, y1, x2, y2);
            }

            fireG.dispose();
        }
    }

    private class ExplosionEffect {
        int x;
        int y;
        int radius;
        long startTime;

        ExplosionEffect(int x, int y, int radius) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.startTime = System.currentTimeMillis();
        }
    }
}
