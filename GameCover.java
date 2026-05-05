import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import javax.imageio.ImageIO;

public class GameCover extends JPanel {
    private Image bg;
    public JButton startBtn;

    public GameCover(ActionListener startAction) {
        setLayout(null);
        try {
            // 載入妳那張漂亮的封面圖
            bg = ImageIO.read(new File("assets/cover.png")); 
        } catch (Exception e) {
            System.out.println("❌ 找不到封面圖 assets/cover.png");
        }

        // 1. 初始化按鈕
        startBtn = new JButton("START MISSION"); // 配合妳截圖的文字
        
        // 2. 設定按鈕位置 (根據妳截圖的位置大約在中間偏下)
        startBtn.setBounds(650, 650, 300, 70);
        
        // 3. 按鈕基礎設定
        startBtn.setContentAreaFilled(false); 
        startBtn.setFocusPainted(false);      
        startBtn.setBorderPainted(false); // 我們自己用畫的，所以關掉預設邊框
        
        // 4. 文字顏色與字體 (使用青色/天藍色配合遊戲風格)
        startBtn.setForeground(new Color(0, 255, 255)); 
        startBtn.setFont(new Font("Monospaced", Font.BOLD, 26));

        startBtn.addActionListener(startAction);
        add(startBtn);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // 開啟抗鋸齒，讓圓角跟邊框更平滑
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 繪製背景圖
        if (bg != null) {
            g2d.drawImage(bg, 0, 0, 1600, 900, this);
        }

        // 5. 繪製半透明按鈕背景
        Rectangle b = startBtn.getBounds();
        
        // 白色半透明背景 (Alpha 設為 160，讓它有厚度感)
        g2d.setColor(new Color(255, 255, 255, 160)); 
        g2d.fillRoundRect(b.x, b.y, b.width, b.height, 10, 10);
        
        // 6. 繪製按鈕外框 (增加立體感)
        g2d.setStroke(new BasicStroke(3)); // 線條粗細
        g2d.setColor(new Color(0, 255, 255, 200)); // 青色外框
        g2d.drawRoundRect(b.x, b.y, b.width, b.height, 10, 10);
    }
}
