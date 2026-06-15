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

public class OceanWorld extends JPanel {

    public static final int SCREEN_WIDTH = 1600;
    public static final int SCREEN_HEIGHT = 900;

    private int worldWidth = SCREEN_WIDTH;
    private int worldHeight = 2800;

    private int cameraX = 0;
    private int cameraY = 0;

    private double playerX;
    private double playerY;

    private final int PLAYER_WIDTH = 110;
    private final int PLAYER_HEIGHT = 135;
    private final double PLAYER_SPEED = 7.0;

    // 氧氣系統
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
    private ArrayList<OceanFish> fishList = new ArrayList<>();

    private Timer gameTimer;
    private Random random = new Random();

    private BufferedImage oceanMap;
    private BufferedImage diverSheet;

    private boolean isShowingReturnDialog = false;

    public OceanWorld(ActionListener backToLandAction) {
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

        this.playerX = SCREEN_WIDTH / 2.0 - (PLAYER_WIDTH / 2.0);
        this.playerY = 600;

        // 每次下水前，重新讀取目前裝備等級
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
            oceanMap = ImageIO.read(new File("assets/ocean_map.png"));
            diverSheet = ImageIO.read(new File("assets/diver_clean.png"));
        } catch (IOException e) {
            System.out.println("❌ 圖片載入失敗，請確認 assets 資料夾");
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

    // =========================
    // 全螢幕縮放用：把真實滑鼠座標轉回 1600 x 900 遊戲座標
    // =========================
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

        String[][] data = {
            {"沙丁魚", "0.1", "50", "assets/fish_anchovy.png", "1", "1"},
            {"小丑魚", "0.5", "200", "assets/fish_clownfish.png", "2", "2"},
            {"螃蟹", "0.8", "400", "assets/fish_crab.png", "3", "2"},
            {"河豚", "1.2", "600", "assets/fish_pufferfish.png", "4", "3"},
            {"刺尾魚", "1.5", "750", "assets/fish_surgefish.png", "5", "3"},
            {"神仙魚", "2.0", "950", "assets/fish_angelfish.png", "6", "4"},
            {"金魚", "0.3", "300", "assets/fish_goldfish.png", "2", "1"},
            {"青魚", "1.8", "800", "assets/fish_green.png", "5", "4"}
        };

        for (String[] fishData : data) {
            for (int i = 0; i < 4; i++) {
                double x = 150 + random.nextInt(Math.max(1, worldWidth - 300));

                double y;
                if (fishData[0].equals("螃蟹")) {
                    y = worldHeight - 150;
                } else {
                    y = 600 + random.nextInt(1600);
                }

                fishList.add(
                    new OceanFish(
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

        for (OceanFish f : fishList) {
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

        // 潛水衣等級會限制最大深度
        int maxDepth = InventoryManager.getMaxDepth();
        double maxYBySuit = 400 + maxDepth;
        double maxYByMap = worldHeight - PLAYER_HEIGHT;

        playerY = clamp(playerY, 400, Math.min(maxYBySuit, maxYByMap));
    }

    private void updateCamera() {
        cameraX = (int) clamp(
            playerX + PLAYER_WIDTH / 2.0 - SCREEN_WIDTH / 2.0,
            0,
            worldWidth - SCREEN_WIDTH
        );

        cameraY = (int) clamp(
            playerY + PLAYER_HEIGHT / 2.0 - SCREEN_HEIGHT / 2.0,
            0,
            worldHeight - SCREEN_HEIGHT
        );
    }

    private void updateBullets() {
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.move();

            if (b.isOutOfRange()) {
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
        Rectangle pRect = new Rectangle(
            (int) playerX,
            (int) playerY,
            PLAYER_WIDTH,
            PLAYER_HEIGHT
        );

        for (int i = fishList.size() - 1; i >= 0; i--) {
            OceanFish f = fishList.get(i);

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

                    // 背包滿時，把玩家稍微推離這隻魚，避免一直觸發碰撞視窗
                    pushPlayerAwayFromFish(f);

                    requestFocusInWindow();
                }

                break;
            }
        }
    }

    private void pushPlayerAwayFromFish(OceanFish f) {
        Rectangle fishRect = f.getBounds();

        double playerCenterX = playerX + PLAYER_WIDTH / 2.0;
        double playerCenterY = playerY + PLAYER_HEIGHT / 2.0;

        double fishCenterX = fishRect.getCenterX();
        double fishCenterY = fishRect.getCenterY();

        double dx = playerCenterX - fishCenterX;
        double dy = playerCenterY - fishCenterY;

        double distance = Math.sqrt(dx * dx + dy * dy);

        // 如果剛好中心重疊，就預設往上推
        if (distance == 0) {
            dx = 0;
            dy = -1;
            distance = 1;
        }

        dx /= distance;
        dy /= distance;

        // 推開距離，可以自己調大或調小
        double pushDistance = 220;

        playerX += dx * pushDistance;
        playerY += dy * pushDistance;

        // 限制玩家不要被推出地圖外
        playerX = clamp(playerX, 0, worldWidth - PLAYER_WIDTH);

        int maxDepth = InventoryManager.getMaxDepth();
        double maxYBySuit = 400 + maxDepth;
        double maxYByMap = worldHeight - PLAYER_HEIGHT;

        playerY = clamp(playerY, 400, Math.min(maxYBySuit, maxYByMap));

        updateCamera();
    }

    private void checkSurfaceInteraction(ActionListener backAction) {
        if (isShowingReturnDialog) {
            return;
        }

        if (playerY <= 415 && upPressed) {
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
                playerY = 550;
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
    
        // 重點：不要再用負寬度翻轉。
        // 圖片本來朝右，rotate(aimAngle) 就能自然轉到左邊、右邊、上方、下方。
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

        if (oceanMap != null) {
            g2d.drawImage(
                oceanMap,
                -cameraX,
                -cameraY,
                worldWidth,
                worldHeight,
                this
            );
        } else {
            g2d.setColor(new Color(0, 40, 90));
            g2d.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        }

        for (OceanFish f : fishList) {
            int sx = (int) f.x - cameraX;
            int sy = (int) f.y - cameraY;

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
        }

        g2d.setColor(Color.YELLOW);

        for (Bullet b : bullets) {
            g2d.fillOval(
                b.getX() - cameraX,
                b.getY() - cameraY,
                10,
                10
            );
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
        
        // 畫出目前手持的武器
        drawCurrentWeapon(g2d, sx, sy);
        
        drawUI(g2d);

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
    
            // 只把從邊緣連進來的背景變透明，避免誤刪武器內部亮點
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
    
        // 白底
        if (r > 235 && g > 235 && b > 235) {
            return true;
        }
    
        // 灰白棋盤格背景
        if (Math.abs(r - g) < 8 && Math.abs(g - b) < 8 && r >= 170 && r <= 245) {
            return true;
        }
    
        return false;
    }    

    private void drawUI(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRoundRect(30, 80, 330, 175, 20, 20);

        g2d.setFont(new Font("Monospaced", Font.BOLD, 18));

        g2d.setColor(Color.CYAN);
        g2d.drawString(
            "Depth : " + Math.max(0, (int) playerY - 400)
            + " / " + InventoryManager.getMaxDepth() + " m",
            50,
            110
        );

        g2d.setColor(Color.WHITE);
        g2d.drawString("Weapon: " + currentWeapon.getName(), 50, 135);

        g2d.setColor(Color.ORANGE);
        g2d.drawString("Oxygen: Lv." + oxygenLevel, 50, 160);

        if (currentOxygen <= 10) {
            g2d.setColor(Color.RED);
        } else {
            g2d.setColor(Color.GREEN);
        }

        g2d.drawString(
            "Time  : " + String.format("%.1f", currentOxygen) + " s",
            50,
            185
        );

        g2d.drawRect(50, 195, 200, 10);
        g2d.fillRect(
            50,
            195,
            (int) ((currentOxygen / maxOxygenTime) * 200),
            10
        );

        g2d.setColor(Color.PINK);
        g2d.drawString(
            "Bag   : " + InventoryManager.getMyBackpack().size()
            + " / " + InventoryManager.getBackpackCapacity(),
            50,
            230
        );
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(v, max));
    }

    private class OceanFish {
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

        private final double MAX_SCARE_BOOST = 3.5;

        OceanFish(
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
            this.size = 35 + r * 12;

            vx = random.nextBoolean() ? 1.5 : -1.5;
            vy = random.nextDouble() - 0.5;
        }

        void update() {
            if (dead) {
                return;
            }

            double mult = isScared ? MAX_SCARE_BOOST : 1.0;

            if (
                isScared
                && System.currentTimeMillis() - scaredStartTime > 1000
            ) {
                isScared = false;
            }

            x += vx * mult;

            if (name.equals("螃蟹")) {
                y = worldHeight - 150;
            } else {
                y += vy;

                if (y < 460 || y > worldHeight - 100) {
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
}