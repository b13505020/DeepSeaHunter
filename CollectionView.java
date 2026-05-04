import javax.swing.*;
import java.awt.*;

public class CollectionView extends JFrame {
    public CollectionView() {
        setTitle("海洋生物圖鑑");
        setSize(600, 700);
        setLayout(new BorderLayout());

        JPanel grid = new JPanel(new GridLayout(0, 2, 15, 15));
        grid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        grid.setBackground(new Color(240, 240, 240));

        for (String fishName : CollectionManager.getUnlockedFish()) {
            int stars = CollectionManager.getStars(fishName);
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            
            // --- 強化：五種極具辨識度的淺底色 ---
            Color bgColor;
            Color borderColor;
            switch(stars) {
                case 1: // 暖沙色 (銅)
                    bgColor = new Color(255, 230, 210);
                    borderColor = new Color(200, 160, 130);
                    break;
                case 2: // 冰晶色 (銀)
                    bgColor = new Color(220, 240, 255);
                    borderColor = new Color(150, 180, 210);
                    break;
                case 3: // 亮檸色 (金)
                    bgColor = new Color(255, 255, 200);
                    borderColor = new Color(220, 200, 100);
                    break;
                case 4: // 翡翠色 (白金)
                    bgColor = new Color(210, 255, 230);
                    borderColor = new Color(130, 200, 160);
                    break;
                case 5: // 夢幻紫 (七彩)
                    bgColor = new Color(245, 220, 255);
                    borderColor = new Color(180, 140, 210);
                    break;
                default:
                    bgColor = Color.WHITE;
                    borderColor = Color.LIGHT_GRAY;
            }
            
            card.setBackground(bgColor);
            card.setBorder(BorderFactory.createLineBorder(borderColor, 3, true));

            // 圖片顯示
            ImageIcon icon = new ImageIcon(CollectionManager.getImagePath(fishName));
            if (icon.getIconWidth() > 0) {
                Image img = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
                JLabel pic = new JLabel(new ImageIcon(img));
                pic.setAlignmentX(Component.CENTER_ALIGNMENT);
                card.add(Box.createVerticalStrut(15));
                card.add(pic);
            }

            // 文字與星級
            JLabel nameLabel = new JLabel(fishName);
            nameLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
            nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel starLabel = new JLabel("⭐".repeat(stars));
            starLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
            starLabel.setForeground(new Color(80, 70, 60));
            starLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            card.add(Box.createVerticalStrut(5));
            card.add(nameLabel);
            card.add(starLabel);
            card.add(Box.createVerticalStrut(15));
            
            grid.add(card);
        }

        JScrollPane scrollPane = new JScrollPane(grid);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        
        setLocationRelativeTo(null); 
        setVisible(true);
    }
}