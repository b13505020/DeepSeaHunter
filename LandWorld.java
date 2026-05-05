import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.imageio.ImageIO;

public class LandWorld extends JPanel implements KeyListener, Runnable {

    private Image background;
    private Image diverSheet; // 角色圖片
    
    private double playerX = 800; 
    private double playerY = 600;
    private final int PLAYER_WIDTH = 110; 
    private final int PLAYER_HEIGHT = 135;
    
    private boolean left, right;
    private final double SPEED = 6.0;

    // 互動區域
    private Rectangle equipmentZone = new Rectangle(20, 450, 180, 350); 
    private Rectangle diveZone = new Rectangle(1150, 600, 250, 250);

    private ActionListener onDive;
    private ActionListener onEnterShop;

    public LandWorld(ActionListener onDive, ActionListener onEnterShop) {
        this.onDive = onDive;
        this.onEnterShop = onEnterShop;
        
        setLayout(null);
        setFocusable(true);
        addKeyListener(this);

        loadImages();
        setupUIButtons();
        new Thread(this).start();
    }

    private void loadImages() {
        try {
            background = ImageIO.read(new File("assets/land_base.png"));
            diverSheet = ImageIO.read(new File("assets/diver_clean.png"));
        } catch (Exception e) {
            System.out.println("❌ 圖片載入失敗，請檢查 assets 資料夾");
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
    }

    public void resetPlayerPosition() {
        this.playerX = 800;
        this.playerY = 600;
        this.left = false;
        this.right = false;
        requestFocusInWindow();
    }

    @Override
    public void run() {
        while (true) {
            if (left && playerX > 0) playerX -= SPEED;
            if (right && playerX < 1600 - PLAYER_WIDTH) playerX += SPEED;
            repaint();
            try { Thread.sleep(16); } catch (InterruptedException e) {}
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (background != null) g.drawImage(background, 0, 0, 1600, 900, this);

        if (diverSheet != null) {
            int sx = (int)playerX;
            int sy = (int)playerY;
            if (left) { 
                g.drawImage(diverSheet, sx + PLAYER_WIDTH, sy, sx, sy + PLAYER_HEIGHT, 0, 0, 128, 140, this);
            } else { 
                g.drawImage(diverSheet, sx, sy, sx + PLAYER_WIDTH, sy + PLAYER_HEIGHT, 0, 0, 128, 140, this);
            }
        } else {
            g.setColor(new Color(200, 100, 50));
            g.fillRect((int)playerX, (int)playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
        }

        Rectangle pRect = new Rectangle((int)playerX, (int)playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 22));

        if (pRect.intersects(equipmentZone)) {
            g.drawString("Press ENTER to Shop", (int)playerX - 30, (int)playerY - 20);
        } else if (pRect.intersects(diveZone)) {
            g.drawString("Press ENTER to Dive", (int)playerX - 30, (int)playerY - 20);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) left = true;
        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) right = true;
        
        if (code == KeyEvent.VK_ENTER) {
            Rectangle pRect = new Rectangle((int)playerX, (int)playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
            if (pRect.intersects(equipmentZone)) onEnterShop.actionPerformed(null);
            else if (pRect.intersects(diveZone)) onDive.actionPerformed(null);
        }
    }

    @Override public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) left = false;
        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) right = false;
    }
    @Override public void keyTyped(KeyEvent e) {}
}