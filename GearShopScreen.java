import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.imageio.ImageIO;

public class GearShopScreen extends JPanel {
    private Image bg;

    public GearShopScreen(ActionListener backAction) {
        setLayout(null);
        try {
            bg = ImageIO.read(new File("assets/shop_gear_interior.png"));
        } catch (Exception e) {
            System.out.println("❌ 找不到商店背景圖");
        }

        // 離開按鈕
        JButton exitBtn = new JButton("EXIT");
        exitBtn.setBounds(50, 800, 150, 50);
        exitBtn.addActionListener(backAction);
        add(exitBtn);

        // 這裡可以後續加入升級按鈕，放在櫃檯上方
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bg != null) g.drawImage(bg, 0, 0, 1600, 900, this);
        
        // 可以在這裡畫 UI 遮罩或對話框
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(1000, 100, 500, 700, 20, 20);
        g.setColor(Color.CYAN);
        g.drawString("MECHANIC SHOP - UPGRADE YOUR GEAR", 1050, 150);
    }
}
