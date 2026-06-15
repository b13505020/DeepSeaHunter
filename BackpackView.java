import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class BackpackView extends JFrame {

    private DefaultListModel<FishItem> listModel;
    private JList<FishItem> list;
    private JLabel totalLabel;
    private JLabel capacityLabel;

    public BackpackView() {
        setTitle("目前背包內容物");
        setSize(560, 640);
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        listModel = new DefaultListModel<>();
        list = new JList<>(listModel);
        list.setCellRenderer(new FishCellRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(8, 8));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        JButton discardOneBtn = new JButton("丟棄選取魚類 x1");
        discardOneBtn.setFont(new Font("Microsoft JhengHei", Font.BOLD, 15));
        discardOneBtn.addActionListener(e -> discardSelectedFishOne());

        JButton discardAllBtn = new JButton("丟棄此種類全部");
        discardAllBtn.setFont(new Font("Microsoft JhengHei", Font.BOLD, 15));
        discardAllBtn.addActionListener(e -> discardSelectedFishAll());

        buttonPanel.add(discardOneBtn);
        buttonPanel.add(discardAllBtn);

        totalLabel = new JLabel("", SwingConstants.CENTER);
        totalLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 20));

        capacityLabel = new JLabel("", SwingConstants.CENTER);
        capacityLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
        capacityLabel.setForeground(new Color(70, 70, 70));

        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.add(totalLabel);
        infoPanel.add(capacityLabel);

        bottomPanel.add(buttonPanel, BorderLayout.NORTH);
        bottomPanel.add(infoPanel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        reloadList();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void reloadList() {
        listModel.clear();

        HashMap<String, Integer> counts = new HashMap<>();
        HashMap<String, Fish> fishData = new HashMap<>();

        for (Fish f : InventoryManager.getMyBackpack()) {
            counts.put(f.getName(), counts.getOrDefault(f.getName(), 0) + 1);
            fishData.putIfAbsent(f.getName(), f);
        }

        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            listModel.addElement(
                new FishItem(
                    fishData.get(entry.getKey()),
                    entry.getValue()
                )
            );
        }

        totalLabel.setText("本次背包價值：$" + getCurrentBackpackValue());

        capacityLabel.setText(
            "背包容量："
            + InventoryManager.getMyBackpack().size()
            + " / "
            + InventoryManager.getBackpackCapacity()
        );
    }

    private int getCurrentBackpackValue() {
        int total = 0;

        for (Fish f : InventoryManager.getMyBackpack()) {
            total += f.getPrice();
        }

        return total;
    }

    private void discardSelectedFishOne() {
        FishItem selected = list.getSelectedValue();

        if (selected == null) {
            JOptionPane.showMessageDialog(
                this,
                "請先選擇要丟棄的魚類。"
            );
            return;
        }

        int result = JOptionPane.showConfirmDialog(
            this,
            "確定要丟棄 1 隻「" + selected.fish.getName() + "」嗎？\n丟棄後無法取回。",
            "確認丟棄",
            JOptionPane.YES_NO_OPTION
        );

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        boolean removed = removeOneFishFromBackpack(selected.fish.getName());

        if (removed) {
            reloadList();
            JOptionPane.showMessageDialog(
                this,
                "已丟棄 1 隻「" + selected.fish.getName() + "」。"
            );
        } else {
            reloadList();
            JOptionPane.showMessageDialog(
                this,
                "丟棄失敗，這隻魚可能已經不在背包裡。"
            );
        }
    }

    private void discardSelectedFishAll() {
        FishItem selected = list.getSelectedValue();

        if (selected == null) {
            JOptionPane.showMessageDialog(
                this,
                "請先選擇要丟棄的魚類。"
            );
            return;
        }

        int result = JOptionPane.showConfirmDialog(
            this,
            "確定要丟棄全部 "
            + selected.count
            + " 隻「"
            + selected.fish.getName()
            + "」嗎？\n丟棄後無法取回。",
            "確認丟棄全部",
            JOptionPane.YES_NO_OPTION
        );

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        int removedCount = removeAllFishFromBackpack(selected.fish.getName());

        reloadList();

        JOptionPane.showMessageDialog(
            this,
            "已丟棄 "
            + removedCount
            + " 隻「"
            + selected.fish.getName()
            + "」。"
        );
    }

    private boolean removeOneFishFromBackpack(String fishName) {
        List<Fish> backpack = InventoryManager.getMyBackpack();

        for (int i = 0; i < backpack.size(); i++) {
            Fish f = backpack.get(i);

            if (f.getName().equals(fishName)) {
                backpack.remove(i);
                return true;
            }
        }

        return false;
    }

    private int removeAllFishFromBackpack(String fishName) {
        List<Fish> backpack = InventoryManager.getMyBackpack();
        int count = 0;

        for (int i = backpack.size() - 1; i >= 0; i--) {
            Fish f = backpack.get(i);

            if (f.getName().equals(fishName)) {
                backpack.remove(i);
                count++;
            }
        }

        return count;
    }

    private class FishItem {
        Fish fish;
        int count;

        FishItem(Fish f, int c) {
            this.fish = f;
            this.count = c;
        }
    }

    private class FishCellRenderer extends JPanel implements ListCellRenderer<FishItem> {
        private JLabel iconLabel = new JLabel();
        private JLabel nameLabel = new JLabel();
        private JLabel infoLabel = new JLabel();

        public FishCellRenderer() {
            setLayout(new BorderLayout(15, 0));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            iconLabel.setPreferredSize(new Dimension(70, 70));
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

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
            FishItem val,
            int idx,
            boolean sel,
            boolean focus
        ) {
            ImageIcon icon = new ImageIcon(val.fish.getImagePath());

            if (icon.getIconWidth() > 0) {
                Image img = icon.getImage().getScaledInstance(
                    60,
                    60,
                    Image.SCALE_SMOOTH
                );
                iconLabel.setIcon(new ImageIcon(img));
                iconLabel.setText("");
            } else {
                iconLabel.setIcon(null);
                iconLabel.setText("無圖");
            }

            nameLabel.setText(val.fish.getName() + " x" + val.count);

            infoLabel.setText(
                "難易度: "
                + "⭐".repeat(val.fish.getRarityStars())
                + " | 單價: $"
                + val.fish.getPrice()
                + " | 小計: $"
                + (val.fish.getPrice() * val.count)
            );

            setBackground(sel ? list.getSelectionBackground() : list.getBackground());
            setOpaque(true);

            return this;
        }
    }
}