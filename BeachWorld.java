import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
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

    private int worldWidth = SCREEN_WIDTH;
    private int worldHeight = SCREEN_HEIGHT;
    private int cameraX = 0;

    private double playerX = 300;
    private double playerY = 610;

    private final int PLAYER_WIDTH = 110;
    private final int PLAYER_HEIGHT = 135;
    private final double PLAYER_SPEED = 6.0;

    private final int WALK_MIN_Y = 260;
    private final int WALK_MAX_Y = 820;

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

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layoutButtons();
            }
        });

        gameTimer = new Timer(16, e -> {
            if (isShowing()) {
                updateGame();
                repaint();
            }
        });

        gameTimer.start();
    }

    private void loadImages() {
        try {
            beachMap = ImageIO.read(new File("assets/beach_clean.png"));

            if (beachMap != null) {
                worldWidth = Math.max(SCREEN_WIDTH, beachMap.getWidth());

                // 重點：
                // 不要用 beachMap.getHeight()
                // 因為你的沙灘圖高度可能不到 900，會造成下面白一塊
                worldHeight = SCREEN_HEIGHT;
            }

            diverSheet = ImageIO.read(new File("assets/diver_clean.png"));
        } catch (Exception e) {
            System.out.println("❌ BeachWorld 圖片載入失敗，請確認 assets 資料夾");
        }
    }

    private void setupButtons() {
        backButton = new JButton("Back to Land");
        backButton.setFocusable(false);
        backButton.addActionListener(e -> finishBeachAndReturn());
        add(backButton);

        layoutButtons();
    }

    private void layoutButtons() {
        int panelW = getWidth();

        if (panelW <= 0) {
            panelW = SCREEN_WIDTH;
        }

        backButton.setBounds(panelW - 190, 30, 150, 40);
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

    private void setupControls() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();

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

                if (code == KeyEvent.VK_E) {
                    collectPressed = true;
                }

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
        facingLeft = false;

        message = "";
        messageTimer = 0;

        spawnMaterialsRandomly();

        requestFocusInWindow();
    }

    private void spawnMaterialsRandomly() {
        materials.clear();

        addRandomMaterial("潮蝕木材", "assets/tideworn_wood.png", 115, 82);
        addRandomMaterial("潮蝕木材", "assets/tideworn_wood.png", 115, 82);

        addRandomMaterial("巨螺殼", "assets/giant_conch.png", 90, 90);
        addRandomMaterial("巨螺殼", "assets/giant_conch.png", 90, 90);

        addRandomMaterial("貝殼碎片", "assets/shell_fragments.png", 115, 90);
        addRandomMaterial("貝殼碎片", "assets/shell_fragments.png", 115, 90);

        addRandomMaterial("珊瑚碎枝", "assets/coral_branch.png", 105, 105);
        addRandomMaterial("珊瑚碎枝", "assets/coral_branch.png", 105, 105);

        addRandomMaterial("纜繩鉤環", "assets/hooked_rope.png", 120, 100);
        addRandomMaterial("鏽蝕齒輪", "assets/rusted_gear.png", 120, 100);

        addRandomMaterial("海蝕石", "assets/sea_worn_stone.png", 105, 85);
        addRandomMaterial("海蝕石", "assets/sea_worn_stone.png", 105, 85);
    }

    private void addRandomMaterial(String name, String imagePath, int width, int height) {
        Rectangle rect = generateNonOverlappingRect(width, height);

        BeachMaterial material = new BeachMaterial(
            name,
            imagePath,
            rect.x,
            rect.y,
            width,
            height
        );

        materials.add(material);
    }

    private Rectangle generateNonOverlappingRect(int width, int height) {
        int attempts = 0;

        while (attempts < 200) {
            int x = randomInt(
                MATERIAL_EDGE_PADDING,
                Math.max(MATERIAL_EDGE_PADDING + 1, worldWidth - MATERIAL_EDGE_PADDING - width)
            );

            int y = randomInt(
                MATERIAL_MIN_Y,
                Math.max(MATERIAL_MIN_Y + 1, MATERIAL_MAX_Y - height)
            );

            Rectangle candidate = new Rectangle(x, y, width, height);
            Rectangle safeCandidate = new Rectangle(x - 30, y - 30, width + 60, height + 60);

            boolean overlap = false;

            for (BeachMaterial material : materials) {
                Rectangle existing = new Rectangle(
                    material.x - 30,
                    material.y - 30,
                    material.width + 60,
                    material.height + 60
                );

                if (safeCandidate.intersects(existing)) {
                    overlap = true;
                    break;
                }
            }

            if (!overlap) {
                return candidate;
            }

            attempts++;
        }

        int fallbackX = randomInt(
            MATERIAL_EDGE_PADDING,
            Math.max(MATERIAL_EDGE_PADDING + 1, worldWidth - MATERIAL_EDGE_PADDING - width)
        );

        int fallbackY = randomInt(
            MATERIAL_MIN_Y,
            Math.max(MATERIAL_MIN_Y + 1, MATERIAL_MAX_Y - height)
        );

        return new Rectangle(fallbackX, fallbackY, width, height);
    }

    private int randomInt(int min, int max) {
        if (max <= min) {
            return min;
        }

        return min + random.nextInt(max - min);
    }

    private void updateGame() {
        updatePlayerMovement();
        updateCamera();
        updateCollect();
        updateMessage();
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
        playerY = clamp(playerY, WALK_MIN_Y, WALK_MAX_Y - PLAYER_HEIGHT);
    }

    private void updateCamera() {
        cameraX = (int) clamp(
            playerX + PLAYER_WIDTH / 2.0 - SCREEN_WIDTH / 2.0,
            0,
            Math.max(0, worldWidth - SCREEN_WIDTH)
        );
    }

    private void updateCollect() {
        if (!collectPressed) {
            return;
        }

        collectPressed = false;

        BeachMaterial near = getNearMaterial();

        if (near == null) {
            message = "附近沒有可採集素材";
            messageTimer = 90;
            return;
        }

        InventoryManager.addCurrentMaterial(near.name, 1);
        materials.remove(near);

        message = "取得：" + near.name;
        messageTimer = 120;
    }

    private void updateMessage() {
        if (messageTimer > 0) {
            messageTimer--;
        }
    }

    private BeachMaterial getNearMaterial() {
        Rectangle playerRect = new Rectangle(
            (int) playerX,
            (int) playerY,
            PLAYER_WIDTH,
            PLAYER_HEIGHT
        );

        for (BeachMaterial material : materials) {
            if (playerRect.intersects(material.getCollectBox())) {
                return material;
            }
        }

        return null;
    }

    private void finishBeachAndReturn() {
        InventoryManager.moveCurrentMaterialsToStorage();
        InventoryManager.saveGame();

        leftPressed = false;
        rightPressed = false;
        upPressed = false;
        downPressed = false;
        collectPressed = false;

        if (backToLandAction != null) {
            backToLandAction.actionPerformed(null);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        layoutButtons();

        Graphics2D g2 = (Graphics2D) g.create();

        double scaleX = getScaleX();
        double scaleY = getScaleY();

        g2.scale(scaleX, scaleY);

        drawMap(g2);
        drawMaterials(g2);
        drawPlayer(g2);
        drawInteractionHint(g2);
        drawInventoryUI(g2);
        drawMessage(g2);

        g2.dispose();
    }

    private void drawMap(Graphics2D g2) {
        if (beachMap != null) {
            // 重點：
            // 強制把沙灘圖畫滿 1600 x 900 遊戲畫面高度
            // 不再使用圖片原本高度，避免底部白色空白
            g2.drawImage(
                beachMap,
                -cameraX,
                0,
                worldWidth,
                SCREEN_HEIGHT,
                this
            );
        } else {
            g2.setColor(new Color(230, 200, 120));
            g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

            g2.setColor(new Color(80, 170, 210));
            g2.fillRect(0, 0, SCREEN_WIDTH, 260);
        }
    }

    private void drawMaterials(Graphics2D g2) {
        for (BeachMaterial material : materials) {
            material.draw(g2, cameraX, this);
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
            g2.setColor(Color.ORANGE);
            g2.fillRect(sx, sy, PLAYER_WIDTH, PLAYER_HEIGHT);
        }
    }

    private void drawInteractionHint(Graphics2D g2) {
        BeachMaterial near = getNearMaterial();

        if (near == null) {
            return;
        }

        String text = "Press E to collect " + near.name;

        g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 22));
        FontMetrics fm = g2.getFontMetrics();

        int textW = fm.stringWidth(text);
        int x = SCREEN_WIDTH / 2 - textW / 2;
        int y = 130;

        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRoundRect(x - 15, y - 32, textW + 30, 42, 12, 12);

        g2.setColor(Color.WHITE);
        g2.drawString(text, x, y);
    }

    private void drawInventoryUI(Graphics2D g2) {
        int x = 30;
        int y = 80;
        int w = 330;
        int h = 280;

        g2.setColor(new Color(0, 0, 0, 175));
        g2.fillRoundRect(x, y, w, h, 20, 20);

        g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 22));
        g2.setColor(new Color(255, 230, 160));
        g2.drawString("Beach Materials", x + 25, y + 38);

        g2.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 17));
        g2.setColor(Color.WHITE);

        Map<String, Integer> currentMaterials = InventoryManager.getCurrentMaterials();

        int lineY = y + 72;

        for (Map.Entry<String, Integer> entry : currentMaterials.entrySet()) {
            if (entry.getValue() > 0) {
                g2.drawString(
                    entry.getKey() + " x " + entry.getValue(),
                    x + 25,
                    lineY
                );

                lineY += 24;
            }
        }

        if (lineY == y + 72) {
            g2.setColor(new Color(220, 220, 220));
            g2.drawString("目前沒有採集素材", x + 25, lineY);
        }

        g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
        g2.setColor(new Color(180, 240, 255));
        g2.drawString("方向鍵：移動", x + 25, y + 220);
        g2.drawString("E：採集素材", x + 25, y + 245);
        g2.drawString("B：返回陸地並存入箱子", x + 25, y + 270);
    }

    private void drawMessage(Graphics2D g2) {
        if (messageTimer <= 0 || message == null || message.isEmpty()) {
            return;
        }

        g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 28));
        FontMetrics fm = g2.getFontMetrics();

        int textW = fm.stringWidth(message);
        int x = SCREEN_WIDTH / 2 - textW / 2;
        int y = 760;

        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRoundRect(x - 20, y - 42, textW + 40, 54, 15, 15);

        g2.setColor(new Color(255, 240, 160));
        g2.drawString(message, x, y);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    private static class BeachMaterial {
        String name;
        String imagePath;
        int x;
        int y;
        int width;
        int height;
        BufferedImage image;

        BeachMaterial(
            String name,
            String imagePath,
            int x,
            int y,
            int width,
            int height
        ) {
            this.name = name;
            this.imagePath = imagePath;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;

            loadImage();
        }

        private void loadImage() {
            try {
                BufferedImage raw = ImageIO.read(new File(imagePath));
                image = removeBackgroundByFloodFill(raw);
            } catch (Exception e) {
                image = null;
            }
        }

        void draw(Graphics2D g2, int cameraX, ImageObserver observer) {
            int sx = x - cameraX;

            if (image != null) {
                g2.drawImage(image, sx, y, width, height, observer);
            } else {
                g2.setColor(Color.YELLOW);
                g2.fillOval(sx, y, width, height);
                g2.setColor(Color.BLACK);
                g2.drawOval(sx, y, width, height);
            }
        }

        Rectangle getCollectBox() {
            return new Rectangle(x - 25, y - 25, width + 50, height + 50);
        }

        private BufferedImage removeBackgroundByFloodFill(BufferedImage source) {
            if (source == null) {
                return null;
            }

            int w = source.getWidth();
            int h = source.getHeight();

            BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g = result.createGraphics();
            g.drawImage(source, 0, 0, null);
            g.dispose();

            boolean[][] visited = new boolean[w][h];
            ArrayList<Point> queue = new ArrayList<>();

            for (int x = 0; x < w; x++) {
                queue.add(new Point(x, 0));
                queue.add(new Point(x, h - 1));
            }

            for (int y = 0; y < h; y++) {
                queue.add(new Point(0, y));
                queue.add(new Point(w - 1, y));
            }

            int index = 0;

            while (index < queue.size()) {
                Point p = queue.get(index);
                index++;

                if (p.x < 0 || p.x >= w || p.y < 0 || p.y >= h) {
                    continue;
                }

                if (visited[p.x][p.y]) {
                    continue;
                }

                visited[p.x][p.y] = true;

                int argb = result.getRGB(p.x, p.y);

                if (!isBackgroundLike(argb)) {
                    continue;
                }

                result.setRGB(p.x, p.y, 0x00000000);

                queue.add(new Point(p.x + 1, p.y));
                queue.add(new Point(p.x - 1, p.y));
                queue.add(new Point(p.x, p.y + 1));
                queue.add(new Point(p.x, p.y - 1));
            }

            cleanRemainingLightPixels(result);

            return result;
        }

        private boolean isBackgroundLike(int argb) {
            int a = (argb >> 24) & 0xff;
            int r = (argb >> 16) & 0xff;
            int g = (argb >> 8) & 0xff;
            int b = argb & 0xff;

            if (a < 20) {
                return true;
            }

            int max = Math.max(r, Math.max(g, b));
            int min = Math.min(r, Math.min(g, b));

            boolean bright = r > 185 && g > 185 && b > 185;
            boolean lowSaturation = max - min < 70;

            return bright && lowSaturation;
        }

        private void cleanRemainingLightPixels(BufferedImage img) {
            int w = img.getWidth();
            int h = img.getHeight();

            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int argb = img.getRGB(x, y);

                    int a = (argb >> 24) & 0xff;
                    int r = (argb >> 16) & 0xff;
                    int g = (argb >> 8) & 0xff;
                    int b = argb & 0xff;

                    if (a < 20) {
                        continue;
                    }

                    int max = Math.max(r, Math.max(g, b));
                    int min = Math.min(r, Math.min(g, b));

                    boolean veryLight = r > 215 && g > 215 && b > 215;
                    boolean grayOrWhite = max - min < 80;

                    if (veryLight && grayOrWhite) {
                        img.setRGB(x, y, 0x00000000);
                    }
                }
            }
        }
    }
}