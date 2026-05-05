import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.io.File;
import javax.imageio.ImageIO;

public class OceanWorld extends JPanel implements Runnable {

    private final int worldWidth = 1600;
    private final int worldHeight = 900; // 原始高度
    
    // 原始主角設定
    private double playerX = 800, playerY = 450;
    private final int playerWidth = 100;
    private final int playerHeight = 100;
    private Image playerImg;

    private ArrayList<OceanFish> fishes = new ArrayList<>();
    private Image bgImage;
    private ActionListener onReturn;

    public OceanWorld(ActionListener onReturn) {
        this.onReturn = onReturn;
        try {
            playerImg = ImageIO.read(new File("assets/diver.png"));
            bgImage = ImageIO.read(new File("assets/ocean_bg.png"));
        } catch (Exception e) {
            System.out.println("❌ 圖片載入失敗");
        }
        spawnFishes();
        new Thread(this).start();
    }

    // 原始簡單的魚類類別
    private class OceanFish {
        double x, y, vx, vy;
        int size;
        boolean dead = false;

        OceanFish() {
            this.size = 50 + (int)(Math.random() * 50);
            this.x = Math.random() * (worldWidth - size);
            this.y = Math.random() * (worldHeight - size);
            this.vx = (Math.random() - 0.5) * 4;
            this.vy = (Math.random() - 0.5) * 4;
        }

        void update() {
            x += vx;
            y += vy;
            // 碰到邊界就反彈
            if (x < 0 || x > worldWidth - size) vx *= -1;
            if (y < 0 || y > worldHeight - size) vy *= -1;
        }
    }

    private void spawnFishes() {
        for (int i = 0; i < 20; i++) {
            fishes.add(new OceanFish());
        }
    }

    @Override
    public void run() {
        while (true) {
            for (OceanFish f : fishes) {
                f.update();
            }
            repaint();
            try {
                Thread.sleep(16);
            } catch (Exception e) {}
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // 畫背景
        if (bgImage != null) {
            g.drawImage(bgImage, 0, 0, 1600, 900, this);
        }

        // 畫主角
        g.drawImage(playerImg, (int)playerX, (int)playerY, playerWidth, playerHeight, this);

        // 畫魚群
        g.setColor(Color.YELLOW);
        for (OceanFish f : fishes) {
            if (!f.dead) {
                g.fillRect((int)f.x, (int)f.y, f.size, f.size / 2);
            }
        }
    }

    public void resetPlayerPosition() {
        this.playerX = 800;
        this.playerY = 450;
    }
}