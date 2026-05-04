import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class OceanWorld extends JPanel {
    private ArrayList<Fish> fishList = new ArrayList<>();
    private ArrayList<Bullet> bullets = new ArrayList<>();
    private Diver player;
    private Timer gameTimer;
    public static final int WIDTH = 1600, HEIGHT = 900;

    // 接收來自 GameLauncher 的切換動作
    public OceanWorld(ActionListener backToLandAction) {
        setLayout(null); 
        setFocusable(true);
        player = new Diver(WIDTH / 2, HEIGHT / 2);

        // --- 完整魚群初始化 (8 種 + 鯊魚) ---
        String[][] data = {
            {"沙丁魚", "0.1", "50", "assets/fish_sardine.png", "1", "1"},
            {"金魚", "0.3", "150", "assets/fish_goldfish.png", "1", "1"},
            {"小丑魚", "0.5", "200", "assets/fish_clownfish.png", "2", "2"},
            {"螃蟹", "0.8", "400", "assets/fish_crab.png", "3", "2"},
            {"神仙魚", "0.7", "450", "assets/fish_angelfish.png", "3", "3"},
            {"河豚", "0.4", "300", "assets/fish_pufferfish.png", "2", "3"},
            {"藍倒吊", "0.6", "350", "assets/fish_surgefish.png", "2", "4"},
            {"綠鰻魚", "1.2", "600", "assets/fish_green.png", "4", "4"},
            {"鯊魚", "250.0", "5000", "assets/fish_shark.png", "10", "5"}
        };

        for (String[] d : data) {
            int spawnCount = d[0].equals("鯊魚") ? 1 : 2;
            for (int i = 0; i < spawnCount; i++) {
                fishList.add(new Fish(d[0], Double.parseDouble(d[1]), Integer.parseInt(d[2]), 
                             d[3], Integer.parseInt(d[4]), Integer.parseInt(d[5])));
            }
        }

        // --- UI 按鈕佈局 ---
        JButton bagBtn = new JButton("🎒 背包");
        bagBtn.setBounds(30, 30, 100, 35);
        bagBtn.setFocusable(false);
        bagBtn.addActionListener(e -> new BackpackView());
        this.add(bagBtn);

        JButton colBtn = new JButton("📜 圖鑑");
        colBtn.setBounds(140, 30, 100, 35);
        colBtn.setFocusable(false);
        colBtn.addActionListener(e -> new CollectionView());
        this.add(colBtn);

        JButton backBtn = new JButton("🏠 返回陸地");
        backBtn.setBounds(250, 30, 110, 35);
        backBtn.setFocusable(false);
        backBtn.setBackground(new Color(255, 200, 200)); // 淺紅色區分
        backBtn.addActionListener(backToLandAction);
        this.add(backBtn);

        // --- 操作監聽 ---
        addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) { player.updateAngle(e.getX(), e.getY()); }
        });
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { fire(); }
        });
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_1) player.setWeapon(new Weapon("初級魚槍", 1, 500));
                if (code == KeyEvent.VK_2) player.setWeapon(new Weapon("重型弩砲", 3, 1000));
                if (code == KeyEvent.VK_UP) player.move(0, -1);
                if (code == KeyEvent.VK_DOWN) player.move(0, 1);
                if (code == KeyEvent.VK_LEFT) player.move(-1, 0);
                if (code == KeyEvent.VK_RIGHT) player.move(1, 0);
                if (code == KeyEvent.VK_SPACE) fire();
            }
        });

        // 遊戲循環 (60 FPS)
        gameTimer = new Timer(16, e -> {
            if (this.isShowing()) { // 只有在畫面顯示時才運作，節省效能
                updateGame();
                repaint();
            }
        });
        gameTimer.start();
    }

    private void fire() {
        bullets.add(new Bullet(player.getX() + 25, player.getY() + 25, player.getAngle(), player.getWeapon()));
    }

    private void updateGame() {
        for (Fish f : fishList) f.updatePhysics(player.getX(), player.getY());
        
        // 子彈與碰撞邏輯
        for (int i = bullets.size()-1; i>=0; i--) {
            Bullet b = bullets.get(i); b.move();
            if (b.getX() < 0 || b.getX() > WIDTH || b.getY() < 0 || b.getY() > HEIGHT || b.isOutOfRange()) {
                bullets.remove(i); continue;
            }
            for (Fish f : fishList) {
                if (!f.isDead() && b.getBounds().intersects(f.getBounds())) {
                    f.takeDamage(b.getDamage());
                    bullets.remove(i); break;
                }
            }
        }

        // 捕獲邏輯
        for (int i = fishList.size()-1; i>=0; i--) {
            Fish f = fishList.get(i);
            if (f.isDead() && player.getBounds().intersects(f.getBounds())) {
                InventoryManager.addFish(f);
                new CatchFishGame(f);
                fishList.remove(i);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 背景色：深海藍
        this.setBackground(new Color(0, 35, 80));
        
        // 畫沙灘底 (水下)
        g.setColor(new Color(194, 178, 128, 100)); 
        g.fillRect(0, 850, WIDTH, 50);

        // 畫子彈
        for (Bullet b : bullets) {
            g.setColor(Color.YELLOW);
            g.fillOval(b.getX(), b.getY(), 10, 10);
        }

        // 畫魚類
        for (Fish f : fishList) {
            ImageIcon icon = new ImageIcon(f.getImagePath());
            int sz = f.getFishSize();
            java.awt.geom.AffineTransform old = g2d.getTransform();
            Composite oldComp = g2d.getComposite();

            if (f.isDead()) {
                // 翻肚且半透明
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                g2d.translate(f.getX() + sz/2, f.getY() + sz/2);
                g2d.rotate(Math.PI); 
                g2d.translate(-sz/2, -sz/2);
                g2d.drawImage(icon.getImage(), 0, 0, sz, sz, this);
            } else {
                // 根據方向繪製
                if (f.isFacingRight()) {
                    g2d.drawImage(icon.getImage(), f.getX(), f.getY(), sz, sz, this);
                } else {
                    g2d.drawImage(icon.getImage(), f.getX() + sz, f.getY(), -sz, sz, this);
                }
            }
            g2d.setTransform(old);
            g2d.setComposite(oldComp);
        }

        // 畫潛水員
        ImageIcon diverIcon = new ImageIcon("assets/diver.png");
        if (diverIcon.getIconWidth() > 0) {
            g.drawImage(diverIcon.getImage(), player.getX(), player.getY(), 50, 50, this);
        } else {
            g.setColor(Color.YELLOW);
            g.fillRect(player.getX(), player.getY(), 50, 50);
        }
    }
}