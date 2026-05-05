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

    // 初始位置
    private double playerX;
    private double playerY;

    private final int PLAYER_WIDTH = 70;
    private final int PLAYER_HEIGHT = 85;
    private final double PLAYER_SPEED = 7.0;

    private boolean upPressed = false, downPressed = false, leftPressed = false, rightPressed = false;
    private double aimAngle = 0;
    private boolean isFacingLeft = false; 

    private Weapon currentWeapon = new Weapon("初級魚槍", 1, 600);
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
        resetPlayerPosition(); // 初始化位置
        setupButtons();
        setupControls();
        spawnFish();
        setupGameLoop(backToLandAction);
    }

    // 【新增】重置玩家位置的方法，供 GameLauncher 呼叫
    public void resetPlayerPosition() {
        this.playerX = SCREEN_WIDTH / 2.0 - 35;
        this.playerY = 600; // 確保進場時遠離水面觸發線
        this.isShowingReturnDialog = false;
        this.upPressed = false; 
        this.downPressed = false;
        this.leftPressed = false;
        this.rightPressed = false;
        updateCamera();
    }

    private void loadImages() {
        try {
            oceanMap = ImageIO.read(new File("assets/ocean_map.png"));
            double scale = SCREEN_WIDTH / (double) oceanMap.getWidth();
            worldWidth = SCREEN_WIDTH;
            worldHeight = (int) Math.round(oceanMap.getHeight() * scale);
        } catch (IOException e) {
            worldWidth = SCREEN_WIDTH; worldHeight = 2800;
        }
        try {
            diverSheet = ImageIO.read(new File("assets/diver_clean.png"));
        } catch (IOException e) {
            System.out.println("Diver image missing.");
        }
    }

    private void setupButtons() {
        JButton bagBtn = new JButton("Backpack");
        bagBtn.setBounds(30, 30, 100, 35);
        bagBtn.setFocusable(false);
        bagBtn.addActionListener(e -> { new BackpackView(); requestFocusInWindow(); });
        add(bagBtn);

        JButton colBtn = new JButton("Collection");
        colBtn.setBounds(140, 30, 120, 35);
        colBtn.setFocusable(false);
        colBtn.addActionListener(e -> { new CollectionView(); requestFocusInWindow(); });
        add(colBtn);
    }

    private void setupControls() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_LEFT) { leftPressed = true; isFacingLeft = true; }
                else if (code == KeyEvent.VK_RIGHT) { rightPressed = true; isFacingLeft = false; }
                else if (code == KeyEvent.VK_UP) upPressed = true;
                else if (code == KeyEvent.VK_DOWN) downPressed = true;
                else if (code == KeyEvent.VK_SPACE) fire();
            }
            @Override
            public void keyReleased(KeyEvent e) {
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_LEFT) leftPressed = false;
                else if (code == KeyEvent.VK_RIGHT) rightPressed = false;
                else if (code == KeyEvent.VK_UP) upPressed = false;
                else if (code == KeyEvent.VK_DOWN) downPressed = false;
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) { updateAimAngle(e.getX(), e.getY()); }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { requestFocusInWindow(); updateAimAngle(e.getX(), e.getY()); fire(); }
        });
    }

    private void setupGameLoop(ActionListener backToLandAction) {
        gameTimer = new Timer(16, e -> { if (isShowing()) { updateGame(backToLandAction); repaint(); } });
        gameTimer.start();
    }

    private void spawnFish() {
        String[][] data = {
            {"沙丁魚", "0.1", "50", "assets/fish_anchovy.png", "1", "1"},
            {"小丑魚", "0.5", "200", "assets/fish_clownfish.png", "2", "2"},
            {"螃蟹", "0.8", "400", "assets/fish_crab.png", "3", "2"},
            {"鯊魚", "5.0", "1500", "assets/fish_shark.png", "10", "5"}
        };
        for (String[] d : data) {
            for (int i = 0; i < 6; i++) {
                double x = 120 + random.nextInt(Math.max(1, worldWidth - 240));
                double y = 600 + random.nextInt(Math.max(1, worldHeight - 800));
                fishList.add(new OceanFish(d[0], Double.parseDouble(d[1]), Integer.parseInt(d[2]), d[3], Integer.parseInt(d[4]), Integer.parseInt(d[5]), x, y));
            }
        }
    }

    private void updateGame(ActionListener backToLandAction) {
        updatePlayerMovement();
        updateCamera();
        updateFish();
        updateBullets();
        checkCatchFish();
        checkSurfaceInteraction(backToLandAction);
    }

    private void updatePlayerMovement() {
        double dx = 0, dy = 0;
        if (leftPressed) dx -= 1; if (rightPressed) dx += 1;
        if (upPressed) dy -= 1; if (downPressed) dy += 1;
        if (dx != 0 || dy != 0) {
            double len = Math.sqrt(dx*dx + dy*dy);
            playerX += (dx/len) * PLAYER_SPEED;
            playerY += (dy/len) * PLAYER_SPEED;
        }
        playerX = clamp(playerX, 0, worldWidth - PLAYER_WIDTH);
        // 物理邊界設在 400
        playerY = clamp(playerY, 400, worldHeight - PLAYER_HEIGHT); 
    }

    private void updateCamera() {
        cameraX = (int) clamp(playerX + PLAYER_WIDTH/2.0 - SCREEN_WIDTH/2.0, 0, worldWidth - SCREEN_WIDTH);
        cameraY = (int) clamp(playerY + PLAYER_HEIGHT/2.0 - SCREEN_HEIGHT/2.0, 0, worldHeight - SCREEN_HEIGHT);
    }

    private void updateFish() { for (OceanFish f : fishList) f.update(); }

    private void updateBullets() {
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.move();
            if (b.isOutOfRange()) { bullets.remove(i); continue; }
            for (OceanFish f : fishList) {
                if (!f.dead && b.getBounds().intersects(f.getBounds())) {
                    f.takeDamage(b.getDamage());
                    bullets.remove(i); break;
                }
            }
        }
    }

    private void checkCatchFish() {
        Rectangle pRect = new Rectangle((int)playerX, (int)playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
        for (int i = fishList.size()-1; i >= 0; i--) {
            OceanFish f = fishList.get(i);
            if (f.dead && pRect.intersects(f.getBounds())) {
                Fish caught = new Fish(f.name, f.weight, f.price, f.imagePath, f.maxHp, f.rarityStars);
                InventoryManager.addFish(caught);
                new CatchFishGame(caught);
                fishList.remove(i);
            }
        }
    }

    private void checkSurfaceInteraction(ActionListener backToLandAction) {
        if (isShowingReturnDialog) return;
        // 觸發高度設在 415，確保有空間偵測
        if (playerY <= 415 && upPressed) {
            isShowingReturnDialog = true;
            int result = JOptionPane.showConfirmDialog(this, "Do you want to return to the shore?", "Return to Land", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) { backToLandAction.actionPerformed(new ActionEvent(this, 0, "backToLand")); }
            else { playerY = 550; upPressed = false; }
            isShowingReturnDialog = false;
            requestFocusInWindow(); 
        }
    }

    private void fire() {
        bullets.add(new Bullet((int)(playerX+PLAYER_WIDTH/2), (int)(playerY+PLAYER_HEIGHT/2), aimAngle, currentWeapon));
    }

    private void updateAimAngle(int mx, int my) {
        double worldMY = my + cameraY;
        // 完全鎖定水平方向，只取滑鼠的垂直高度
        double baseDX = isFacingLeft ? -100.0 : 100.0;
        double dy = worldMY - (playerY + PLAYER_HEIGHT / 2.0);
        aimAngle = Math.toDegrees(Math.atan2(dy, baseDX));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (oceanMap != null) g.drawImage(oceanMap, -cameraX, -cameraY, worldWidth, worldHeight, this);
        for (OceanFish f : fishList) {
            int sx = (int)f.x - cameraX; int sy = (int)f.y - cameraY;
            if (sx < -100 || sx > SCREEN_WIDTH + 100) continue;
            ImageIcon icon = new ImageIcon(f.imagePath);
            if (f.dead) ((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            if (f.facingRight) g.drawImage(icon.getImage(), sx, sy, f.size, f.size, this);
            else g.drawImage(icon.getImage(), sx + f.size, sy, -f.size, f.size, this);
            ((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
        g.setColor(Color.YELLOW);
        for (Bullet b : bullets) g.fillOval(b.getX() - cameraX, b.getY() - cameraY, 10, 10);
        int sx = (int)playerX - cameraX; int sy = (int)playerY - cameraY;
        if (diverSheet != null) {
            if (isFacingLeft) g.drawImage(diverSheet, sx + PLAYER_WIDTH, sy, sx, sy + PLAYER_HEIGHT, 0, 0, 128, 140, this);
            else g.drawImage(diverSheet, sx, sy, sx + PLAYER_WIDTH, sy + PLAYER_HEIGHT, 0, 0, 128, 140, this);
        }
        g.drawLine(sx+PLAYER_WIDTH/2, sy+PLAYER_HEIGHT/2, sx+PLAYER_WIDTH/2+(int)(Math.cos(Math.toRadians(aimAngle))*50), sy+PLAYER_HEIGHT/2+(int)(Math.sin(Math.toRadians(aimAngle))*50));
        drawUI(g);
    }

    private void drawUI(Graphics g) {
        int depth = Math.max(0, (int)playerY - 400); 
        g.setColor(new Color(0,0,0,150)); g.fillRoundRect(30, 80, 250, 80, 15, 15);
        g.setColor(Color.CYAN); g.drawString("Depth: " + depth + " m", 50, 110);
        g.drawString("Weapon: " + currentWeapon.getName(), 50, 140);
    }

    private double clamp(double v, double min, double max) { return Math.max(min, Math.min(v, max)); }

    private class OceanFish {
        String name, imagePath; double weight; int price, maxHp, hp, rarityStars, size;
        double x, y, vx, vy; boolean dead = false, facingRight = true;
        OceanFish(String n, double w, int p, String img, int m, int r, double x, double y) {
            name=n; weight=w; price=p; imagePath=img; maxHp=m; hp=m; rarityStars=r; this.x=x; this.y=y;
            size=50 + r*10; vx=random.nextBoolean()?1.5:-1.5; vy=random.nextDouble()-0.5;
        }
        void update() {
            if (dead) return;
            x += vx;
            if (name.equals("螃蟹")) { y = worldHeight - size - 20; vy = 0; }
            else { y += vy; if (y < 460 || y > worldHeight - 100) vy *= -1; }
            if (x < 60 || x > worldWidth - 100) vx *= -1;
            facingRight = vx >= 0;
        }
        void takeDamage(int d) { hp-=d; if(hp<=0) dead=true; }
        Rectangle getBounds() { return new Rectangle((int)x, (int)y, size, size); }
    }
}