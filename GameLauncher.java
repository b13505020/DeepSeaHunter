import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;

public class GameLauncher extends JFrame {
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);
    
    // --- 關鍵修改：將 landPanel 定義在這裡，讓所有方法都看得到它 ---
    private LandWorld landPanel;

    public static final int WIN_WIDTH = 1600;
    public static final int WIN_HEIGHT = 900;

    public GameLauncher() {
        setTitle("深海工域 - Deep Sea Industry");
        setSize(WIN_WIDTH, WIN_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // 初始化陸地頁面
        landPanel = new LandWorld(e -> cardLayout.show(mainPanel, "OceanScreen"));
        
        // 建立封面頁面
        JPanel titlePanel = createTitlePanel();

        mainPanel.add(titlePanel, "TitleScreen");
        mainPanel.add(landPanel, "LandScreen");

        add(mainPanel);
        cardLayout.show(mainPanel, "TitleScreen");
        
        setVisible(true);
    }

    private JPanel createTitlePanel() {
        return new JPanel(null) {
            private Image coverImg;
            {
                try {
                    coverImg = ImageIO.read(new File("assets/cover.png"));
                    
                    JButton startBtn = new JButton("START MISSION");
                    startBtn.setBounds(650, 650, 300, 80); 
                    
                    startBtn.setFont(new Font("Monospaced", Font.BOLD, 28));
                    startBtn.setBackground(new Color(20, 50, 70));
                    startBtn.setForeground(new Color(0, 255, 255));
                    startBtn.setFocusPainted(false);
                    startBtn.setBorder(BorderFactory.createLineBorder(new Color(0, 255, 255), 3));
                    
                    startBtn.addActionListener(e -> {
                        cardLayout.show(mainPanel, "LandScreen");
                        // 點擊按鈕後，強制陸地頁面取得焦點，人物才能移動
                        landPanel.requestFocusInWindow();
                    });
                    
                    add(startBtn);
                } catch (IOException e) {
                    System.out.println("❌ 讀取 assets/cover.png 失敗");
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (coverImg != null) {
                    g.drawImage(coverImg, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
    }

    public static void main(String[] args) {
        System.setProperty("sun.java2d.uiScale", "1.0");
        SwingUtilities.invokeLater(() -> new GameLauncher());
    }
}