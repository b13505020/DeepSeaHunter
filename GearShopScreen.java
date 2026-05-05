import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class GearShopScreen extends JPanel {
    private BufferedImage shopBg;
    private BufferedImage npcBoss;
    private JButton upgradeOxygenBtn, upgradeSuitBtn, exitBtn;
    
    // 假設這些數值儲存在 InventoryManager 中
    private int oxygenLevel = 1;
    private int suitLevel = 1;

    public GearShopScreen(ActionListener backToLandAction) {
        setLayout(null);
        setPreferredSize(new Dimension(1600, 900));
        loadImages();

        // --- 升級按鈕樣式優化 ---
        upgradeOxygenBtn = createStyledButton("升級氧氣瓶 (需 $500)", 950, 350);
        upgradeOxygenBtn.addActionListener(e -> {
            // 這裡加入金幣判斷，例如：if(InventoryManager.getMoney() >= 500)
            oxygenLevel++;
            JOptionPane.showMessageDialog(this, "氧氣瓶升級成功！目前 Lv." + oxygenLevel);
        });
        add(upgradeOxygenBtn);

        upgradeSuitBtn = createStyledButton("強化潛水衣 (需 $800)", 950, 450);
        upgradeSuitBtn.addActionListener(e -> {
            suitLevel++;
            JOptionPane.showMessageDialog(this, "潛水衣強化成功！目前 Lv." + suitLevel);
        });
        add(upgradeSuitBtn);

        exitBtn = new JButton("離開商店");
        exitBtn.setBounds(1100, 600, 150, 50);
        exitBtn.addActionListener(backToLandAction);
        add(exitBtn);
    }

    private JButton createStyledButton(String text, int x, int y) {
        JButton btn = new JButton(text);
        btn.setBounds(x, y, 350, 70);
        btn.setFont(new Font("Monospaced", Font.BOLD, 20));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(50, 30, 20)); // 深褐色背景
        btn.setFocusable(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(200, 150, 50), 3)); // 金色邊框
        return btn;
    }

    private void loadImages() {
        try {
            shopBg = ImageIO.read(new File("assets/shop_background.png"));
            npcBoss = ImageIO.read(new File("assets/npc_boss.png"));
        } catch (IOException e) {
            System.out.println("❌ 商店圖片載入失敗，請確認 assets 資料夾中的檔名");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // 1. 畫背景
        if (shopBg != null) {
            g2d.drawImage(shopBg, 0, 0, 1600, 900, null);
        }

        // 2. 畫老闆 (調整位置讓他在櫃檯後面)
        if (npcBoss != null) {
            g2d.drawImage(npcBoss, 180, 220, 480, 580, null);
        }

        // 3. 對話框裝飾
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRoundRect(50, 750, 1500, 120, 30, 30);
        g2d.setColor(new Color(200, 150, 50));
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRoundRect(50, 750, 1500, 120, 30, 30);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 26));
        g2d.drawString("老闆：嘿，年輕人！想要在水下待久一點，就把你的氧氣瓶拿來升級吧。", 100, 820);
    }
}