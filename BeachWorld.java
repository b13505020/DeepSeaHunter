import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import javax.imageio.ImageIO;

public class BeachWorld extends JPanel {

    public static final int SCREEN_WIDTH = 1600;
    public static final int SCREEN_HEIGHT = 900;

    private BufferedImage beachMap;
    private BufferedImage diverSheet;

    private int worldWidth;
    private int worldHeight;

    private int cameraX = 0;

    private double playerX = 300;
    private double playerY = 610;

    private final int PLAYER_WIDTH = 110;
    private final int PLAYER_HEIGHT = 135;
    private final double PLAYER_SPEED = 6.0;

    // 玩家可走範圍
    // WALK_MIN_Y 越小，角色可以越往上走
    private final int WALK_MIN_Y = 260;
    private final int WALK_MAX_Y = 820;

    // 素材隨機生成範圍
    private final int MATERIAL_MIN_Y = 330;
    private final int MATERIAL_MAX_Y = 690;
    private final int MATERIAL_EDGE_PADDING = 120;

    private boolean leftPressed;
    private boolean rightPressed;
    private boolean upPressed;
    private boolean downPressed;

    private boolean facingLeft = false;
    private boolean collectPressed = false;

    private ArrayList<BeachMaterial> materials = new ArrayList<>();

    private Timer gameTimer;
    private ActionListener backToLandAction;

    private String message = "";
    private int messageTimer = 0;

    private JButton backButton;

    private Random random = new Random();

    public BeachWorld(ActionListener backToLandAction) {
        this.backToLandAction = backToLandAction;

        setLayout(null);
        setFocusable(true);
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));

        loadImages();
        setupButtons();
        setupControls();

        // 第一次建立沙灘畫面時先生成一次素材
        spawnMaterialsRandomly();

        gameTimer = new Timer(16, e -> {
            updateGame();
            repaint();
        });

        gameTimer.start();
    }

    private void loadImages() {
        try {
            // 沙灘圖片直接放在 assets 資料夾
            beachMap = ImageIO.read(new File("assets/beach_clean.png"));

            double scale = (double) SCREEN_HEIGHT / beachMap.getHeight();
            worldHeight = SCREEN_HEIGHT;
            worldWidth = (int) (beachMap.getWidth() * scale);

        } catch (Exception e) {
            System.out.println("找不到沙灘地圖圖片：assets/beach_clean.png");
            beachMap = null;
            worldWidth = 2700;
            worldHeight = SCREEN_HEIGHT;
        }

        try {
            diverSheet = ImageIO.read(new File("assets/diver_clean.png"));
        } catch (Exception e) {
            System.out.println("找不到玩家圖片：assets/diver_clean.png");
            diverSheet = null;
        }
    }

    private void setupButtons() {
        backButton = new JButton("返回陸地");
        backButton.setBounds(1420, 30, 130, 45);
        backButton.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
        backButton.setForeground(Color.WHITE);
        backButton.setBackground(new Color(35, 70, 90));
        backButton.setBorder(BorderFactory.createLineBorder(new Color(180, 230, 255), 2));
        backButton.setFocusable(false);

        // 點擊右上角按鈕返回陸地
        backButton.addActionListener(e -> finishBeachAndReturn());

        add(backButton);
    }

    private void setupControls() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();

                // 只保留方向鍵移動
                if (code == KeyEvent.VK_LEFT) {
                    leftPressed = true;
                    facingLeft = true;
                }

                if (code == KeyEvent.VK_RIGHT) {
                    rightPressed = true;
                    facingLeft = false;
                }

                if (code == KeyEvent.VK_UP) {
                    upPressed = true;
                }

                if (code == KeyEvent.VK_DOWN) {
                    downPressed = true;
                }

                // E 採集素材
                if (code == KeyEvent.VK_E) {
                    collectPressed = true;
                }

                // B 返回陸地
                if (code == KeyEvent.VK_B) {
                    finishBeachAndReturn();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int code = e.getKeyCode();

                if (code == KeyEvent.VK_LEFT) {
                    leftPressed = false;
                }

                if (code == KeyEvent.VK_RIGHT) {
                    rightPressed = false;
                }

                if (code == KeyEvent.VK_UP) {
                    upPressed = false;
                }

                if (code == KeyEvent.VK_DOWN) {
                    downPressed = false;
                }
            }
        });
    }

    public void resetForEntry() {
        playerX = 300;
        playerY = 610;

        cameraX = 0;

        leftPressed = false;
        rightPressed = false;
        upPressed = false;
        downPressed = false;
        collectPressed = false;

        message = "";
        messageTimer = 0;

        // 每次從陸地進入沙灘，都重新隨機分佈素材
        spawnMaterialsRandomly();

        requestFocusInWindow();
    }

    // =========================
    // 隨機生成素材
    // =========================
    private void spawnMaterialsRandomly() {
        materials.clear();

        ArrayList<Rectangle> occupiedAreas = new ArrayList<>();

        // 木頭 x2
        addRandomMaterial(
            occupiedAreas,
            "潮蝕木材",
            "assets/tideworn_wood.png",
            115,
            82
        );

        addRandomMaterial(
            occupiedAreas,
            "潮蝕木材",
            "assets/tideworn_wood.png",
            115,
            82
        );

        // 巨螺殼 x2
        addRandomMaterial(
            occupiedAreas,
            "巨螺殼",
            "assets/giant_conch.png",
            90,
            90
        );

        addRandomMaterial(
            occupiedAreas,
            "巨螺殼",
            "assets/giant_conch.png",
            90,
            90
        );

        // 貝殼碎片 x2
        addRandomMaterial(
            occupiedAreas,
            "貝殼碎片",
            "assets/shell_fragments.png",
            115,
            90
        );

        addRandomMaterial(
            occupiedAreas,
            "貝殼碎片",
            "assets/shell_fragments.png",
            115,
            90
        );

        // 珊瑚碎枝 x2
        addRandomMaterial(
            occupiedAreas,
            "珊瑚碎枝",
            "assets/coral_branch.png",
            105,
            105
        );

        addRandomMaterial(
            occupiedAreas,
            "珊瑚碎枝",
            "assets/coral_branch.png",
            105,
            105
        );

        // 纜繩鉤環 x1
        addRandomMaterial(
            occupiedAreas,
            "纜繩鉤環",
            "assets/hooked_rope.png",
            120,
            100
        );

        // 鏽蝕齒輪 x1
        addRandomMaterial(
            occupiedAreas,
            "鏽蝕齒輪",
            "assets/rusted_gear.png",
            120,
            100
        );

        // 海蝕石 x2
        addRandomMaterial(
            occupiedAreas,
            "海蝕石",
            "assets/sea_worn_stone.png",
            105,
            85
        );

        addRandomMaterial(
            occupiedAreas,
            "海蝕石",
            "assets/sea_worn_stone.png",
            105,
            85
        );
    }

    private void addRandomMaterial(
        ArrayList<Rectangle> occupiedAreas,
        String name,
        String imagePath,
        int width,
        int height
    ) {
        Rectangle rect = generateNonOverlappingRect(occupiedAreas, width, height);

        materials.add(
            new BeachMaterial(
                name,
                imagePath,
                rect.x,
                rect.y,
                width,
                height
            )
        );

        // 加大佔用範圍，避免素材太擠
        Rectangle paddedRect = new Rectangle(
            rect.x - 45,
            rect.y - 35,
            rect.width + 90,
            rect.height + 70
        );

        occupiedAreas.add(paddedRect);
    }

    private Rectangle generateNonOverlappingRect(ArrayList<Rectangle> occupiedAreas, int width, int height) {
        int minX = MATERIAL_EDGE_PADDING;
        int maxX = Math.max(minX + 1, worldWidth - width - MATERIAL_EDGE_PADDING);

        int minY = MATERIAL_MIN_Y;
        int maxY = MATERIAL_MAX_Y;

        // 避免素材一開始生成在玩家出生點附近
        Rectangle playerStartArea = new Rectangle(
            220,
            540,
            260,
            260
        );

        // 最多嘗試 100 次找不重疊的位置
        for (int i = 0; i < 100; i++) {
            int x = randomInt(minX, maxX);
            int y = randomInt(minY, maxY);

            Rectangle candidate = new Rectangle(x, y, width, height);

            boolean overlaps = false;

            if (candidate.intersects(playerStartArea)) {
                overlaps = true;
            }

            for (Rectangle occupied : occupiedAreas) {
                if (candidate.intersects(occupied)) {
                    overlaps = true;
                    break;
                }
            }

            if (!overlaps) {
                return candidate;
            }
        }

        // 如果真的找不到不重疊位置，就給一個備用位置
        int fallbackX = randomInt(minX, maxX);
        int fallbackY = randomInt(minY, maxY);

        return new Rectangle(fallbackX, fallbackY, width, height);
    }

    private int randomInt(int min, int max) {
        if (max <= min) {
            return min;
        }

        return min + random.nextInt(max - min + 1);
    }

    // =========================
    // 更新邏輯
    // =========================
    private void updateGame() {
        updatePlayerMovement();
        updateCamera();
        updateCollect();
        updateMessage();
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
        playerY = clamp(playerY, WALK_MIN_Y, WALK_MAX_Y - PLAYER_HEIGHT);
    }

    private void updateCamera() {
        int maxCameraX = Math.max(0, worldWidth - SCREEN_WIDTH);

        cameraX = (int) clamp(
            playerX + PLAYER_WIDTH / 2.0 - SCREEN_WIDTH / 2.0,
            0,
            maxCameraX
        );
    }

    private void updateCollect() {
        if (!collectPressed) {
            return;
        }

        BeachMaterial near = getNearMaterial();

        if (near != null) {
            near.setCollected(true);
            InventoryManager.addCurrentMaterial(near.getName(), 1);

            message = "取得素材：" + near.getName();
            messageTimer = 90;
        }

        collectPressed = false;
    }

    private void updateMessage() {
        if (messageTimer > 0) {
            messageTimer--;
        }
    }

    private BeachMaterial getNearMaterial() {
        Rectangle playerBox = new Rectangle(
            (int) playerX,
            (int) playerY,
            PLAYER_WIDTH,
            PLAYER_HEIGHT
        );

        for (BeachMaterial material : materials) {
            if (material.isCollected()) {
                continue;
            }

            if (playerBox.intersects(material.getCollectBox())) {
                return material;
            }
        }

        return null;
    }

    private void finishBeachAndReturn() {
        int total = InventoryManager.getCurrentMaterialTotalCount();

        // 把本次沙灘背包的素材歸進永久儲藏箱
        InventoryManager.moveCurrentMaterialsToStorage();

        // 自動存檔
        InventoryManager.saveGame();

        if (total > 0) {
            System.out.println("已將本次沙灘採集素材歸入儲藏箱，共 " + total + " 個。");
        }

        if (backToLandAction != null) {
            backToLandAction.actionPerformed(null);
        }
    }

    // =========================
    // 畫面繪製
    // =========================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR
        );

        drawMap(g2);
        drawMaterials(g2);
        drawPlayer(g2);
        drawInteractionHint(g2);
        drawInventoryUI(g2);
        drawMessage(g2);
    }

    private void drawMap(Graphics2D g2) {
        if (beachMap != null) {
            g2.drawImage(
                beachMap,
                -cameraX,
                0,
                worldWidth,
                worldHeight,
                this
            );
        } else {
            // 圖片讀不到時的替代背景
            g2.setColor(new Color(235, 205, 135));
            g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

            g2.setColor(new Color(65, 180, 210));
            g2.fillRect(0, 620, SCREEN_WIDTH, 280);
        }
    }

    private void drawMaterials(Graphics2D g2) {
        for (BeachMaterial material : materials) {
            material.draw(g2, cameraX);
        }
    }

    private void drawPlayer(Graphics2D g2) {
        int sx = (int) playerX - cameraX;
        int sy = (int) playerY;

        if (diverSheet != null) {
            if (facingLeft) {
                g2.drawImage(
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
                g2.drawImage(
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
            g2.setColor(new Color(60, 90, 120));
            g2.fillRoundRect(sx, sy, PLAYER_WIDTH, PLAYER_HEIGHT, 15, 15);
        }
    }

    private void drawInteractionHint(Graphics2D g2) {
        BeachMaterial near = getNearMaterial();

        if (near == null) {
            return;
        }

        int boxWidth = 430;
        int boxHeight = 50;
        int boxX = SCREEN_WIDTH / 2 - boxWidth / 2;
        int boxY = SCREEN_HEIGHT - 90;

        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 15, 15);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 22));
        g2.drawString("按 E 採集：" + near.getName(), boxX + 35, boxY + 33);
    }

    private void drawInventoryUI(Graphics2D g2) {
        int x = 25;
        int y = 25;
        int width = 280;
        int height = 315;

        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRoundRect(x, y, width, height, 20, 20);

        g2.setColor(new Color(255, 230, 150));
        g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 22));
        g2.drawString("本次沙灘背包", x + 25, y + 38);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));

        Map<String, Integer> currentMaterials = InventoryManager.getCurrentMaterials();

        int lineY = y + 75;

        for (Map.Entry<String, Integer> entry : currentMaterials.entrySet()) {
            g2.drawString(entry.getKey() + " x " + entry.getValue(), x + 30, lineY);
            lineY += 27;
        }

        g2.setColor(new Color(220, 240, 255));
        g2.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));

        // 這裡已經拿掉 WASD 和 ESC
        g2.drawString("方向鍵：移動", x + 25, y + 270);
        g2.drawString("E：採集素材", x + 25, y + 292);
        g2.drawString("B：返回陸地並存入箱子", x + 25, y + 314);
    }

    private void drawMessage(Graphics2D g2) {
        if (messageTimer <= 0 || message.equals("")) {
            return;
        }

        int boxWidth = 460;
        int boxHeight = 50;
        int boxX = SCREEN_WIDTH / 2 - boxWidth / 2;
        int boxY = 30;

        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 15, 15);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 22));
        g2.drawString(message, boxX + 35, boxY + 33);
    }

    private double clamp(double value, double min, double max) {
        if (max < min) {
            return min;
        }

        return Math.max(min, Math.min(value, max));
    }
}