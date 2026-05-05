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

    private int cameraX = 0, cameraY = 0;
    private double playerX, playerY;
    private final int PLAYER_WIDTH = 110, PLAYER_HEIGHT = 135; 
    private final double PLAYER_SPEED = 7.0;

    // --- 氧氣系統：60 秒 ---
    private int oxygenLevel = 1;         
    private double maxOxygenTime = 60.0; 
    private double currentOxygen;        

    private boolean upPressed = false, downPressed = false, leftPressed = false, rightPressed = false;
    private double aimAngle = 0;
    private boolean isFacingLeft = false; 

    // --- 武器資訊 (補回顯示邏輯) ---
    private ArrayList<Weapon> weaponList = new ArrayList<>();
    private int currentWeaponIndex = 0;
    private Weapon currentWeapon;
    private ArrayList<Bullet> bullets = new ArrayList<>();
    private ArrayList<OceanFish> fishList = new ArrayList<>();

    private Timer gameTimer;
    private Random random = new Random();
    private BufferedImage oceanMap, diverSheet;
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
        this.playerX = SCREEN_WIDTH / 2.0 - (PLAYER_WIDTH / 2.0);
        this.playerY = 600; 
        this.currentOxygen = maxOxygenTime; 
        this.isShowingReturnDialog = false;
        stopMovement();
        updateCamera();
    }

    private void stopMovement() {
        this.upPressed = false; this.downPressed = false;
        this.leftPressed = false; this.rightPressed = false;
    }

    private void loadImages() {
        try {
            oceanMap = ImageIO.read(new File("assets/ocean_map.png"));
            diverSheet = ImageIO.read(new File("assets/diver_clean.png"));
        } catch (IOException e) {
            System.out.println("❌ 圖片載入失敗");
        }
    }
    private void switchWeapon(int direction) {
        if (weaponList.isEmpty()) return;
    
        currentWeaponIndex += direction;
    
        if (currentWeaponIndex < 0) {
            currentWeaponIndex = weaponList.size() - 1;
        } else if (currentWeaponIndex >= weaponList.size()) {
            currentWeaponIndex = 0;
        }
    
        currentWeapon = weaponList.get(currentWeaponIndex);
    }

    private void setupButtons() {
        JButton bagBtn = new JButton("Backpack");
        bagBtn.setBounds(30, 30, 100, 35);
        bagBtn.setFocusable(false);
        bagBtn.addActionListener(e -> { stopMovement(); new BackpackView(); requestFocusInWindow(); });
        add(bagBtn);

        JButton colBtn = new JButton("Collection");
        colBtn.setBounds(140, 30, 120, 35);
        colBtn.setFocusable(false);
        colBtn.addActionListener(e -> { stopMovement(); new CollectionView(); requestFocusInWindow(); });
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
                else if (code == KeyEvent.VK_Q) switchWeapon(-1);
                else if (code == KeyEvent.VK_E) switchWeapon(1);
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
            @Override public void mouseMoved(MouseEvent e) { updateAimAngle(e.getX(), e.getY()); }
        });
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { requestFocusInWindow(); updateAimAngle(e.getX(), e.getY()); fire(); }
        });
    }

    private void setupGameLoop(ActionListener backToLandAction) {
        gameTimer = new Timer(16, e -> { if (isShowing()) { updateGame(backToLandAction); repaint(); } });
        gameTimer.start();
    }
    private void setupWeapons() {
        weaponList.clear();
    
        weaponList.add(new Weapon("初級魚槍", 1, 600));
        weaponList.add(new Weapon("水下步槍", 2, 750));
        weaponList.add(new Weapon("狙擊槍", 6, 1300));
        weaponList.add(new Weapon("網槍", 1, 500));
        weaponList.add(new Weapon("睡眠槍", 1, 650));
        weaponList.add(new Weapon("麻醉槍", 1, 650));
        weaponList.add(new Weapon("榴彈發射器", 8, 450));
        weaponList.add(new Weapon("寒冰槍", 2, 700));
    
        currentWeaponIndex = 0;
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
                double y = fishData[0].equals("螃蟹") ? worldHeight - 150 : 600 + random.nextInt(1600);
                fishList.add(new OceanFish(fishData[0], Double.parseDouble(fishData[1]), Integer.parseInt(fishData[2]), fishData[3], Integer.parseInt(fishData[4]), Integer.parseInt(fishData[5]), x, y));
            }
        }
    }

    private void updateGame(ActionListener backAction) {
        updatePlayerMovement();
        updateCamera();
        if (!isShowingReturnDialog) {
            currentOxygen -= 0.016; 
            if (currentOxygen <= 0) { currentOxygen = 0; handleOxygenOut(backAction); }
        }
        for (OceanFish f : fishList) {
            double dist = Math.sqrt(Math.pow(f.x - playerX, 2) + Math.pow(f.y - playerY, 2));
            if (dist < 180) f.triggerScared(); 
            f.update(); 
        }
        updateBullets();
        checkCatchFish();
        checkSurfaceInteraction(backAction);
    }

    private void handleOxygenOut(ActionListener backAction) {
        isShowingReturnDialog = true;
        stopMovement();
        JOptionPane.showMessageDialog(this, "氧氣耗盡！物資遺失。", "緊急情況", JOptionPane.ERROR_MESSAGE);
        InventoryManager.clearCurrentDive(); 
        backAction.actionPerformed(new ActionEvent(this, 0, "backToLand"));
        isShowingReturnDialog = false;
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
        playerY = clamp(playerY, 400, worldHeight - PLAYER_HEIGHT); 
    }

    private void updateCamera() {
        cameraX = (int) clamp(playerX + PLAYER_WIDTH/2.0 - SCREEN_WIDTH/2.0, 0, worldWidth - SCREEN_WIDTH);
        cameraY = (int) clamp(playerY + PLAYER_HEIGHT/2.0 - SCREEN_HEIGHT/2.0, 0, worldHeight - SCREEN_HEIGHT);
    }

    private void updateBullets() {
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i); b.move();
            if (b.isOutOfRange()) { bullets.remove(i); continue; }
            for (OceanFish f : fishList) {
                if (!f.dead && b.getBounds().intersects(f.getBounds())) {
                    f.takeDamage(b.getDamage()); bullets.remove(i); break;
                }
            }
        }
    }

    private void checkCatchFish() {
        Rectangle pRect = new Rectangle((int)playerX, (int)playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
        for (int i = fishList.size()-1; i >= 0; i--) {
            OceanFish f = fishList.get(i);
            if (f.dead && pRect.intersects(f.getBounds())) {
                stopMovement(); 
                Fish caught = new Fish(f.name, f.weight, f.price, f.imagePath, f.maxHp, f.rarityStars);
                InventoryManager.addFish(caught);
                new CatchFishGame(caught);
                fishList.remove(i);
                break; 
            }
        }
    }

    private void checkSurfaceInteraction(ActionListener backAction) {
        if (isShowingReturnDialog) return;
        if (playerY <= 415 && upPressed) {
            isShowingReturnDialog = true; stopMovement();
            int result = JOptionPane.showConfirmDialog(this, "成功返回陸地？", "結算上岸", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) { InventoryManager.moveToStorage(); backAction.actionPerformed(null); }
            else { playerY = 550; }
            isShowingReturnDialog = false;
            requestFocusInWindow(); 
        }
    }

    private void fire() { bullets.add(new Bullet((int)(playerX+PLAYER_WIDTH/2), (int)(playerY+PLAYER_HEIGHT/2), aimAngle, currentWeapon)); }
    private void updateAimAngle(int mx, int my) {
        double worldMY = my + cameraY; double baseDX = isFacingLeft ? -100.0 : 100.0;
        double dy = worldMY - (playerY + PLAYER_HEIGHT / 2.0);
        aimAngle = Math.toDegrees(Math.atan2(dy, baseDX));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        if (oceanMap != null) g2d.drawImage(oceanMap, -cameraX, -cameraY, worldWidth, worldHeight, this);
        for (OceanFish f : fishList) {
            int sx = (int)f.x - cameraX; int sy = (int)f.y - cameraY;
            ImageIcon icon = new ImageIcon(f.imagePath);
            if (f.dead) g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            if (f.facingRight) g2d.drawImage(icon.getImage(), sx, sy, f.size, (int)(f.size * 0.75), this);
            else g2d.drawImage(icon.getImage(), sx + f.size, sy, -f.size, (int)(f.size * 0.75), this);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
        g.setColor(Color.YELLOW);
        for (Bullet b : bullets) g.fillOval(b.getX() - cameraX, b.getY() - cameraY, 10, 10);
        int sx = (int)playerX - cameraX; int sy = (int)playerY - cameraY;
        if (diverSheet != null) {
            if (isFacingLeft) g2d.drawImage(diverSheet, sx + PLAYER_WIDTH, sy, sx, sy + PLAYER_HEIGHT, 0, 0, 128, 140, this);
            else g2d.drawImage(diverSheet, sx, sy, sx + PLAYER_WIDTH, sy + PLAYER_HEIGHT, 0, 0, 128, 140, this);
        }
        g2d.setColor(Color.WHITE);
        g2d.drawLine(sx+PLAYER_WIDTH/2, sy+PLAYER_HEIGHT/2, sx+PLAYER_WIDTH/2+(int)(Math.cos(Math.toRadians(aimAngle))*50), sy+PLAYER_HEIGHT/2+(int)(Math.sin(Math.toRadians(aimAngle))*50));
        drawUI(g2d);
    }

    private void drawUI(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 180)); g2d.fillRoundRect(30, 80, 280, 130, 20, 20);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 18));
        g2d.setColor(Color.CYAN); g2d.drawString("Depth : " + Math.max(0, (int)playerY - 400) + " m", 50, 110);
        
        // --- 補回武器顯示 ---
        g2d.setColor(Color.WHITE);
        g2d.drawString("Weapon: " + currentWeapon.getName(), 50, 135);
        
        g2d.setColor(Color.ORANGE); g2d.drawString("Oxygen: Lv." + oxygenLevel, 50, 160);
        if (currentOxygen <= 10) g2d.setColor(Color.RED); else g2d.setColor(Color.GREEN);
        g2d.drawString("Time  : " + String.format("%.1f", currentOxygen) + " s", 50, 185);
        g2d.drawRect(50, 195, 200, 10); g2d.fillRect(50, 195, (int)((currentOxygen / maxOxygenTime) * 200), 10);
    }

    private double clamp(double v, double min, double max) { return Math.max(min, Math.min(v, max)); }

    private class OceanFish {
        String name, imagePath; double weight; int price, maxHp, hp, rarityStars, size;
        double x, y, vx, vy; boolean dead, facingRight = true, isScared; long scaredStartTime;
        private final double MAX_SCARE_BOOST = 3.5;

        OceanFish(String n, double w, int p, String img, int m, int r, double x, double y) {
            this.name=n; this.weight=w; this.price=p; this.imagePath=img; this.maxHp=m; this.hp=m; this.rarityStars=r; 
            this.x=x; this.y=y; this.size = 35 + r * 12; 
            vx=random.nextBoolean()?1.5:-1.5; vy=random.nextDouble()-0.5;
        }
        void update() {
            if (dead) return;
            double mult = isScared ? MAX_SCARE_BOOST : 1.0;
            if (isScared && System.currentTimeMillis() - scaredStartTime > 1000) isScared = false;
            x += vx * mult; 
            if (name.equals("螃蟹")) y = worldHeight - 150; 
            else {
                y += vy;
                if (y < 460 || y > worldHeight - 100) vy *= -1;
            }
            if (x < 60 || x > worldWidth - 100) vx *= -1;
            facingRight = (vx * mult) >= 0;
        }
        void triggerScared() { if (!isScared) { isScared = true; vx *= -1; scaredStartTime = System.currentTimeMillis(); } }
        void takeDamage(int d) { hp -= d; triggerScared(); if (hp <= 0) dead = true; }
        Rectangle getBounds() { return new Rectangle((int)x, (int)y, size, size); }
    }
}