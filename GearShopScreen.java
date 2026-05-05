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
    
    // 商店按鈕
    private JButton upgradeOxygenBtn;
    private JButton upgradeSuitBtn;
    private JButton exitBtn;

    // 升級狀態 (這裡可以根據需求串接全域變數)
    private int oxygenLevel = 1;
    private int suitLevel = 1;

    public GearShopScreen(ActionListener backToLandAction) {
        // 設定 Layout 為 null 以便精確放置按鈕
        setLayout(null);
        setPreferredSize(new Dimension(1600, 900));
        loadImages();

        // --- 1. 升級氧氣瓶按鈕 ---
        upgradeOxygenBtn = new JButton("升級氧氣瓶 (Lv." + oxygenLevel + " -> " + (oxygenLevel + 1) + ")");
        upgradeOxygenBtn.setBounds(980, 380, 300, 60);
        upgradeOxygenBtn.setFont(new Font("Monospaced", Font.BOLD, 18));
        upgradeOxygenBtn.setFocusable(false);
        upgradeOxygenBtn.setBackground(new Color(60, 40, 30));
        upgradeOxygenBtn.setForeground(Color.WHITE);
        upgradeOxygenBtn.addActionListener(e -> {
            // 升級邏輯：假設等級提升，並增加 maxOxygenTime
            oxygenLevel++;
            upgradeOxygenBtn.setText("升級氧氣瓶 (Lv." + oxygenLevel + " -> " + (oxygenLevel + 1) + ")");
            
            // 彈出視窗通知
            JOptionPane.showMessageDialog(this, 
                "老闆：「嘿！這罐新的氧氣瓶可以讓你多撐 30 秒！」\n氧氣上限已提升。", 
                "升級成功", JOptionPane.INFORMATION_MESSAGE);
            
            // 這裡未來應呼叫 InventoryManager 或全域設定來改變 OceanWorld 的初始值
        });
        add(upgradeOxygenBtn);

        // --- 2. 升級潛水衣按鈕 ---
        upgradeSuitBtn = new JButton("強化潛水衣 (Lv." + suitLevel + " -> " + (suitLevel + 1) + ")");
        upgradeSuitBtn.setBounds(980, 460, 300, 60);
        upgradeSuitBtn.setFont(new Font("Monospaced", Font.BOLD, 18));
        upgradeSuitBtn.setFocusable(false);
        upgradeSuitBtn.setBackground(new Color(40, 60, 30));
        upgradeSuitBtn.setForeground(Color.WHITE);
        upgradeSuitBtn.addActionListener(e -> {
            suitLevel++;
            upgradeSuitBtn.setText("強化潛水衣 (Lv." + suitLevel + " -> " + (suitLevel + 1) + ")");
            JOptionPane.showMessageDialog(this, "老闆：「這套衣服更耐壓了，你可以去更深的地方。」");
        });
        add(upgradeSuitBtn);

        // --- 3. 離開商店按鈕 ---
        exitBtn = new JButton("離開商店");
        exitBtn.setBounds(1100, 600, 150, 45);
        exitBtn.setFont(new Font("Monospaced", Font.BOLD, 16));
        exitBtn.setFocusable(false);
        exitBtn.addActionListener(backToLandAction); // 點擊後回到 LandWorld
        add(exitBtn);
    }

    private void loadImages() {
        try {
            // 對齊妳命名的檔案
            shopBg = ImageIO.read(new File("assets/shop_background.png"));
            npcBoss = ImageIO.read(new File("assets/npc_boss.png"));
            System.out.println("✅ 商店資源載入成功");
        } catch (IOException e) {
            System.out.println("❌ 商店圖片載入失敗，請檢查 assets 資料夾下的 shop_background.png 與 npc_boss.png");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // 1. 繪製背景
        if (shopBg != null) {
            g2d.drawImage(shopBg, 0, 0, getWidth(), getHeight(), this);
        }

        // 2. 繪製 NPC 老闆 (根據妳的圖片比例調整位置)
        if (npcBoss != null) {
            // 將老闆畫在左側櫃檯後方
            g2d.drawImage(npcBoss, 150, 250, 450, 550, this);
        }

        // 3. 繪製裝飾對話框
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRoundRect(50, 750, 1500, 120, 25, 25);
        
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(50, 750, 1500, 120, 25, 25);

        g2d.setFont(new Font("Monospaced", Font.BOLD, 22));
        g2d.drawString("老闆：歡迎來到深海工業。想要潛得更深、待得更久，就把裝備交給我處理吧！", 100, 810);
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 18));
        g2d.setColor(Color.YELLOW);
        g2d.drawString("( 點擊按鈕來升級你的裝備 )", 100, 845);
    }
}