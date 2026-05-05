import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;

public class LandWorld extends JPanel {

    public static final int SCREEN_WIDTH = 1600;
    public static final int WIN_HEIGHT = 900;

    private BufferedImage landBg;
    private BufferedImage playerSheet;

    private double playerX = 800;
    private double playerY = 650;
    private final int PLAYER_WIDTH = 80;
    private final int PLAYER_HEIGHT = 100;
    private final double PLAYER_SPEED = 8.0;

    private boolean leftPressed = false, rightPressed = false;
    private boolean isFacingLeft = false;

    // 區域偵測矩形
    private Rectangle equipmentZone = new Rectangle(120, 400, 300, 400); 
    private Rectangle diveZone = new Rectangle(1100, 600, 320, 250);

    private Timer gameTimer;

    // 更新建構子：接收 enterOcean 與 enterShop 兩個 Action
    public LandWorld(ActionListener enterOceanAction, ActionListener enterShopAction) {
        setLayout(null);
        setFocusable(true);
        setPreferredSize(new Dimension(SCREEN_WIDTH, WIN_HEIGHT));

        loadResources();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_LEFT) { leftPressed = true; isFacingLeft = true; }
                else if (code == KeyEvent.VK_RIGHT) { rightPressed = true; isFacingLeft = false; }
                
                else if (code == KeyEvent.VK_ENTER) {
                    Rectangle pRect = new Rectangle((int)playerX, (int)playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
                    
                    // 根據位置觸發不同動作
                    if (pRect.intersects(equipmentZone)) {
                        enterShopAction.actionPerformed(new ActionEvent(this, 0, "enterShop"));
                    } 
                    else if (pRect.intersects(diveZone)) {
                        enterOceanAction.actionPerformed(new ActionEvent(this, 0, "enterOcean"));
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_LEFT) leftPressed = false;
                else if (code == KeyEvent.VK_RIGHT) rightPressed = false;
            }
        });

        gameTimer = new Timer(16, e -> {
            update();
            repaint();
        });
        gameTimer.start();
    }

    private void loadResources() {
        try {
            landBg = ImageIO.read(new File("assets/land_base.png")); 
            playerSheet = ImageIO.read(new File("assets/diver_clean.png"));
        } catch (IOException e) {
            System.out.println("❌ 陸地資源載入失敗，請確認 assets/land_base.png 與 assets/diver_clean.png");
        }
    }

    private void update() {
        if (leftPressed) playerX -= PLAYER_SPEED;
        if (rightPressed) playerX += PLAYER_SPEED;
        playerX = Math.max(0, Math.min(playerX, SCREEN_WIDTH - PLAYER_WIDTH));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (landBg != null) {
            g.drawImage(landBg, 0, 0, SCREEN_WIDTH, WIN_HEIGHT, this);
        }

        int sx = (int)playerX, sy = (int)playerY;
        if (playerSheet != null) {
            if (isFacingLeft) g.drawImage(playerSheet, sx + PLAYER_WIDTH, sy, sx, sy + PLAYER_HEIGHT, 0, 0, 128, 140, this);
            else g.drawImage(playerSheet, sx, sy, sx + PLAYER_WIDTH, sy + PLAYER_HEIGHT, 0, 0, 128, 140, this);
        }

        // 提示字樣
        Rectangle pRect = new Rectangle((int)playerX, (int)playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
        if (pRect.intersects(equipmentZone) || pRect.intersects(diveZone)) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Monospaced", Font.BOLD, 18));
            g.drawString("Press ENTER to Interact", sx - 20, sy - 20);
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }
}