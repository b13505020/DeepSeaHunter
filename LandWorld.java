import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;

public class LandWorld extends JPanel {
    private Image bgImage;
    private Image walkSheet; 
    
    // 起始座標設定在右下角 Dive Zone
    private int px = 1380; 
    private int py = 780; 
    
    private String currentPrompt = ""; 

    // 動態與動畫變數
    private int frameCounter = 0; 
    private int currentFrame = 0; 
    private boolean isMoving = false; // 現在這個變數會被拿來判斷動畫了！
    
    /**
     * 0: 朝前, 1: 朝後, 2: 朝左, 3: 朝右
     */
    private int facingDirection = 0; 
    
    private final int FRAME_SPEED = 8; 
    private final int SPRITE_SIZE = 256; 

    public LandWorld(ActionListener diveAction) {
        setLayout(null);
        setFocusable(true);

        try {
            bgImage = ImageIO.read(new File("assets/land_base.png"));
            walkSheet = ImageIO.read(new File("assets/diver.png"));
            System.out.println("✅ LandWorld 資源載入成功！");
        } catch (IOException e) {
            System.out.println("❌ 資源載入失敗，請檢查 assets 資料夾");
            e.printStackTrace();
        }

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                boolean moved = false;
                int speed = 12; 

                if (code == KeyEvent.VK_LEFT) {
                    px -= speed;
                    facingDirection = 2;
                    moved = true;
                } else if (code == KeyEvent.VK_RIGHT) {
                    px += speed;
                    facingDirection = 3;
                    moved = true;
                } else if (code == KeyEvent.VK_UP) {
                    py = Math.max(750, py - speed); 
                    facingDirection = 1;
                    moved = true;
                } else if (code == KeyEvent.VK_DOWN) {
                    py = Math.min(840, py + speed); 
                    facingDirection = 0;
                    moved = true;
                }

                if (moved) {
                    isMoving = true; // 設定為移動中
                    frameCounter++;
                    currentFrame = (frameCounter / FRAME_SPEED) % 3;
                }

                checkInteractions(code, diveAction);
                repaint();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // 當手指放開按鍵時，人物停止
                isMoving = false; 
                currentFrame = 0; 
                repaint();
            }
        });
    }

    private void checkInteractions(int keyCode, ActionListener diveAction) {
        Rectangle blacksmithRect = new Rectangle(100, 700, 200, 200); 
        Rectangle hqRect = new Rectangle(750, 700, 200, 200);        
        Rectangle diveRect = new Rectangle(1300, 750, 200, 150);     

        Rectangle playerRect = new Rectangle(px, py, 100, 100);

        if (playerRect.intersects(blacksmithRect)) {
            currentPrompt = "Press F to Upgrade";
        } else if (playerRect.intersects(hqRect)) {
            currentPrompt = "Press F to talk to Commander";
        } else if (playerRect.intersects(diveRect)) {
            currentPrompt = "Press Enter to DIVE!";
            if (keyCode == KeyEvent.VK_ENTER) {
                diveAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "dive"));
            }
        } else {
            currentPrompt = "";
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (bgImage != null) {
            g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
        }

        if (walkSheet != null) {
            // --- 這裡使用了 isMoving 變數來解決警告 ---
            // 如果 isMoving 是 true，就用計算出來的格數；如果是 false，就強制顯示第 0 格（站立姿勢）
            int actualFrame = isMoving ? currentFrame : 0;

            int srcX1 = actualFrame * SPRITE_SIZE;
            int srcY1 = facingDirection * SPRITE_SIZE;
            int srcX2 = srcX1 + SPRITE_SIZE;
            int srcY2 = srcY1 + SPRITE_SIZE;

            g.drawImage(walkSheet, 
                        px, py, px + 100, py + 100, 
                        srcX1, srcY1, srcX2, srcY2, 
                        this);
        }

        if (!currentPrompt.isEmpty()) {
            g.setColor(new Color(0, 0, 0, 150)); 
            g.fillRect(px - 20, py - 40, 250, 30);
            g.setColor(Color.CYAN);
            g.setFont(new Font("Monospaced", Font.BOLD, 18));
            g.drawString(currentPrompt, px - 10, py - 20);
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }
}