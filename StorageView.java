import javax.swing.*;
import java.awt.*;
import java.util.List;

public class StorageView extends JFrame {
    public StorageView() {
        setTitle("永久儲藏箱 (永久保存)");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);

        // 主面板：背景色與邊距
        JPanel container = new JPanel(new BorderLayout(10, 10));
        container.setBackground(new Color(40, 40, 50));
        container.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel header = new JLabel("我的儲藏物資 (成功結算後在此保存)");
        header.setForeground(Color.YELLOW);
        header.setFont(new Font("SansSerif", Font.BOLD, 18));
        container.add(header, BorderLayout.NORTH);

        // 網格面板：每列 4 個格子
        JPanel gridPanel = new JPanel(new GridLayout(0, 4, 10, 10));
        gridPanel.setBackground(new Color(40, 40, 50));

        List<Fish> myStorage = InventoryManager.getStorage();

        if (myStorage.isEmpty()) {
            JLabel empty = new JLabel("目前空空如也...");
            empty.setForeground(Color.LIGHT_GRAY);
            gridPanel.add(empty);
        } else {
            for (Fish f : myStorage) {
                JPanel itemBox = new JPanel(new BorderLayout());
                itemBox.setBackground(new Color(60, 60, 75));
                itemBox.setBorder(BorderFactory.createLineBorder(Color.GRAY));

                // 魚的圖片
                ImageIcon icon = new ImageIcon(new ImageIcon(f.getImagePath())
                                 .getImage().getScaledInstance(80, 70, Image.SCALE_SMOOTH));
                JLabel imgLabel = new JLabel(icon);
                
                // 魚的名字與價格
                JLabel nameLabel = new JLabel(f.getName(), SwingConstants.CENTER);
                nameLabel.setForeground(Color.WHITE);
                nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

                itemBox.add(imgLabel, BorderLayout.CENTER);
                itemBox.add(nameLabel, BorderLayout.SOUTH);
                gridPanel.add(itemBox);
            }
        }

        JScrollPane scroll = new JScrollPane(gridPanel);
        scroll.getViewport().setBackground(new Color(40, 40, 50));
        scroll.setBorder(null);
        container.add(scroll, BorderLayout.CENTER);

        add(container);
        setVisible(true);
    }
}