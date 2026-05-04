import java.awt.*;
import javax.swing.*;

public class NPC {
    private int x, y;
    private String name;
    private String imagePath;

    public NPC(String name, int x, int y, String imagePath) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.imagePath = imagePath;
    }

    public void draw(Graphics g, JPanel observer) {
        ImageIcon icon = new ImageIcon(imagePath);
        if (icon.getIconWidth() > 0) {
            g.drawImage(icon.getImage(), x, y, 80, 80, observer);
        } else {
            // 如果沒圖，畫一個綠色方塊代表商人
            g.setColor(Color.GREEN);
            g.fillRect(x, y, 80, 80);
        }
        g.setColor(Color.BLACK);
        g.drawString(name, x + 20, y - 10);
    }

    // 判斷玩家是否靠近商人
    public boolean isPlayerNear(int px, int py) {
        double dist = Math.sqrt(Math.pow(x - px, 2) + Math.pow(y - py, 2));
        return dist < 120; 
    }
}