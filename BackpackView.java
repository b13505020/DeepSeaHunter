import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class BackpackView extends JFrame {
    public BackpackView() {
        setTitle("目前背包內容物");
        setSize(500, 600);
        setLayout(new BorderLayout());

        DefaultListModel<FishItem> listModel = new DefaultListModel<>();
        JList<FishItem> list = new JList<>(listModel);
        list.setCellRenderer(new FishCellRenderer());

        HashMap<String, Integer> counts = new HashMap<>();
        HashMap<String, Fish> fishData = new HashMap<>();
        
        for (Fish f : InventoryManager.getMyBackpack()) {
            counts.put(f.getName(), counts.getOrDefault(f.getName(), 0) + 1);
            fishData.putIfAbsent(f.getName(), f);
        }

        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            listModel.addElement(new FishItem(fishData.get(entry.getKey()), entry.getValue()));
        }

        add(new JScrollPane(list), BorderLayout.CENTER);
        JLabel totalLabel = new JLabel("💰 總資產價值: $" + InventoryManager.getTotalPrice(), SwingConstants.CENTER);
        totalLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 22));
        totalLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        add(totalLabel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private class FishItem {
        Fish fish; int count;
        FishItem(Fish f, int c) { this.fish = f; this.count = c; }
    }

    private class FishCellRenderer extends JPanel implements ListCellRenderer<FishItem> {
        private JLabel iconLabel = new JLabel();
        private JLabel nameLabel = new JLabel();
        private JLabel infoLabel = new JLabel();

        public FishCellRenderer() {
            setLayout(new BorderLayout(15, 0));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            JPanel textPanel = new JPanel(new GridLayout(2, 1));
            textPanel.setOpaque(false);
            nameLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
            infoLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
            infoLabel.setForeground(Color.GRAY);
            textPanel.add(nameLabel);
            textPanel.add(infoLabel);
            add(iconLabel, BorderLayout.WEST);
            add(textPanel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends FishItem> list, FishItem val, int idx, boolean sel, boolean focus) {
            ImageIcon icon = new ImageIcon(val.fish.getImagePath());
            if (icon.getIconWidth() > 0) {
                Image img = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                iconLabel.setIcon(new ImageIcon(img));
            }
            nameLabel.setText(val.fish.getName() + " x" + val.count);
            infoLabel.setText("難易度: " + "⭐".repeat(val.fish.getRarityStars()) + " | 總價值: $" + (val.fish.getPrice() * val.count));
            setBackground(sel ? list.getSelectionBackground() : list.getBackground());
            return this;
        }
    }
}