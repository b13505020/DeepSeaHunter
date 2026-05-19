import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class BackpackView extends JFrame {

    private DefaultListModel<FishItem> listModel;
    private JList<FishItem> list;
    private JLabel totalLabel;

    public BackpackView() {
        setTitle("目前背包內容物");
        setSize(540, 650);
        setLayout(new BorderLayout());

        listModel = new DefaultListModel<>();
        list = new JList<>(listModel);
        list.setCellRenderer(new FishCellRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        reloadBackpackList();

        add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());

        JButton aquariumBtn = new JButton("放入水族館");
        aquariumBtn.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
        aquariumBtn.addActionListener(e -> putSelectedFishIntoAquarium());

        totalLabel = new JLabel("", SwingConstants.CENTER);
        totalLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 20));
        totalLabel.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

        bottomPanel.add(aquariumBtn, BorderLayout.NORTH);
        bottomPanel.add(totalLabel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);
        refreshTotalLabel();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void putSelectedFishIntoAquarium() {
        FishItem selected = list.getSelectedValue();

        if (selected == null) {
            JOptionPane.showMessageDialog(
                this,
                "請先選一種魚。",
                "尚未選擇",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        Fish targetFish = findOneFishByName(selected.fish.getName());

        if (targetFish == null) {
            JOptionPane.showMessageDialog(
                this,
                "背包裡找不到這隻魚，可能已經被移走了。",
                "放入失敗",
                JOptionPane.WARNING_MESSAGE
            );
            reloadBackpackList();
            refreshTotalLabel();
            return;
        }

        InventoryManager.getMyBackpack().remove(targetFish);
        AquariumManager.addFish(targetFish);

        reloadBackpackList();
        refreshTotalLabel();

        JOptionPane.showMessageDialog(
            this,
            "已將「" + targetFish.getName() + "」放入水族館。",
            "放入成功",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private Fish findOneFishByName(String fishName) {
        for (Fish fish : InventoryManager.getMyBackpack()) {
            if (fish.getName().equals(fishName)) {
                return fish;
            }
        }

        return null;
    }

    private void reloadBackpackList() {
        listModel.clear();

        HashMap<String, Integer> counts = new HashMap<>();
        HashMap<String, Fish> fishData = new HashMap<>();

        for (Fish fish : InventoryManager.getMyBackpack()) {
            counts.put(fish.getName(), counts.getOrDefault(fish.getName(), 0) + 1);
            fishData.putIfAbsent(fish.getName(), fish);
        }

        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            listModel.addElement(new FishItem(fishData.get(entry.getKey()), entry.getValue()));
        }
    }

    private void refreshTotalLabel() {
        totalLabel.setText("總資產價值：$" + InventoryManager.getTotalPrice() + "　｜　水族館：" + AquariumManager.getTotalCount() + " 隻");
    }

    private class FishItem {
        Fish fish;
        int count;

        FishItem(Fish fish, int count) {
            this.fish = fish;
            this.count = count;
        }

        @Override
        public String toString() {
            return fish.getName() + " x" + count;
        }
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
        public Component getListCellRendererComponent(
            JList<? extends FishItem> list,
            FishItem value,
            int index,
            boolean isSelected,
            boolean cellHasFocus
        ) {
            ImageIcon icon = new ImageIcon(value.fish.getImagePath());

            if (icon.getIconWidth() > 0) {
                Image img = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                iconLabel.setIcon(new ImageIcon(img));
            } else {
                iconLabel.setIcon(null);
            }

            nameLabel.setText(value.fish.getName() + " x" + value.count);
            infoLabel.setText("難易度：" + "⭐".repeat(value.fish.getRarityStars()) + "｜總價值：$" + (value.fish.getPrice() * value.count));

            setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            setOpaque(true);

            return this;
        }
    }
}
