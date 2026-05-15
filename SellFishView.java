import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SellFishView extends JFrame {
    private DefaultListModel<FishGroup> listModel = new DefaultListModel<>();
    private JList<FishGroup> fishList = new JList<>(listModel);
    private JLabel infoLabel = new JLabel();

    private Runnable afterSellAction;

    public SellFishView(Runnable afterSellAction) {
        this.afterSellAction = afterSellAction;

        setTitle("選擇要賣出的魚");
        setSize(760, 560);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setupInfoLabel();
        setupFishList();
        setupButtons();

        reloadFishList();
        setVisible(true);
    }

    private void setupInfoLabel() {
        infoLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        add(infoLabel, BorderLayout.NORTH);
    }

    private void setupFishList() {
        fishList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fishList.setCellRenderer(new FishSellRenderer());

        JScrollPane scrollPane = new JScrollPane(fishList);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void setupButtons() {
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton sellSelectedBtn = new JButton("賣出選取魚種");
        sellSelectedBtn.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
        sellSelectedBtn.addActionListener(e -> sellSelectedFishGroup());

        JButton sellAllBtn = new JButton("賣出全部魚");
        sellAllBtn.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
        sellAllBtn.addActionListener(e -> sellAllFish());

        JButton closeBtn = new JButton("關閉");
        closeBtn.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        closeBtn.addActionListener(e -> dispose());

        buttonPanel.add(sellSelectedBtn);
        buttonPanel.add(sellAllBtn);
        buttonPanel.add(closeBtn);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void sellSelectedFishGroup() {
        FishGroup selectedGroup = fishList.getSelectedValue();

        if (selectedGroup == null) {
            JOptionPane.showMessageDialog(this, "請先選擇要賣出的魚！");
            return;
        }

        if (selectedGroup.count <= 0) {
            JOptionPane.showMessageDialog(this, "這種魚目前沒有庫存。");
            return;
        }

        JSpinner amountSpinner = new JSpinner(
            new SpinnerNumberModel(1, 1, selectedGroup.count, 1)
        );

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("魚種：" + selectedGroup.fish.getName()));
        panel.add(new JLabel("目前數量：" + selectedGroup.count + " 隻"));
        panel.add(new JLabel("單價：$" + selectedGroup.fish.getPrice()));
        panel.add(new JLabel("請選擇要賣出的數量："));
        panel.add(amountSpinner);

        int result = JOptionPane.showConfirmDialog(
            this,
            panel,
            "選擇賣出數量",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        int amount = (int) amountSpinner.getValue();

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "確定要賣出 "
            + amount
            + " 隻「"
            + selectedGroup.fish.getName()
            + "」嗎？\n可以獲得 $"
            + (amount * selectedGroup.fish.getPrice()),
            "確認賣出",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        List<Fish> fishesToSell = findFishToSell(
            selectedGroup.fish.getName(),
            amount
        );

        int earned = InventoryManager.sellSelectedFish(fishesToSell);

        JOptionPane.showMessageDialog(
            this,
            "成功賣出 "
            + amount
            + " 隻「"
            + selectedGroup.fish.getName()
            + "」，獲得 $"
            + earned
        );

        reloadFishList();

        if (afterSellAction != null) {
            afterSellAction.run();
        }
    }

    private List<Fish> findFishToSell(String fishName, int amount) {
        List<Fish> result = new ArrayList<>();

        for (Fish f : InventoryManager.getStorage()) {
            if (f.getName().equals(fishName)) {
                result.add(f);

                if (result.size() >= amount) {
                    break;
                }
            }
        }

        return result;
    }

    private void sellAllFish() {
        if (InventoryManager.getStorage().isEmpty()) {
            JOptionPane.showMessageDialog(this, "目前沒有魚可以賣。");
            return;
        }

        int result = JOptionPane.showConfirmDialog(
            this,
            "確定要賣出儲藏箱裡的全部魚嗎？",
            "確認賣出全部",
            JOptionPane.YES_NO_OPTION
        );

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        int earned = InventoryManager.sellAllStorageFish();

        JOptionPane.showMessageDialog(
            this,
            "成功賣出全部魚，獲得 $" + earned
        );

        reloadFishList();

        if (afterSellAction != null) {
            afterSellAction.run();
        }
    }

    private void reloadFishList() {
        listModel.clear();

        Map<String, FishGroup> groupedFish = new LinkedHashMap<>();

        for (Fish f : InventoryManager.getStorage()) {
            String name = f.getName();

            if (groupedFish.containsKey(name)) {
                groupedFish.get(name).count++;
            } else {
                groupedFish.put(name, new FishGroup(f, 1));
            }
        }

        for (FishGroup group : groupedFish.values()) {
            listModel.addElement(group);
        }

        infoLabel.setText(
            "目前金錢：$" + InventoryManager.getMoney()
            + "　｜　儲藏箱價值：$" + InventoryManager.getStorageValue()
            + "　｜　魚總數：" + InventoryManager.getStorage().size()
            + "　｜　魚種數：" + groupedFish.size()
        );
    }

    private class FishGroup {
        Fish fish;
        int count;

        FishGroup(Fish fish, int count) {
            this.fish = fish;
            this.count = count;
        }

        int getTotalPrice() {
            return fish.getPrice() * count;
        }
    }

    private class FishSellRenderer extends JPanel implements ListCellRenderer<FishGroup> {
        private JLabel iconLabel = new JLabel();
        private JLabel nameLabel = new JLabel();
        private JLabel infoLabel = new JLabel();

        public FishSellRenderer() {
            setLayout(new BorderLayout(15, 0));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JPanel textPanel = new JPanel(new GridLayout(2, 1));
            textPanel.setOpaque(false);

            nameLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
            infoLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));

            textPanel.add(nameLabel);
            textPanel.add(infoLabel);

            add(iconLabel, BorderLayout.WEST);
            add(textPanel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(
            JList<? extends FishGroup> list,
            FishGroup group,
            int index,
            boolean isSelected,
            boolean cellHasFocus
        ) {
            Fish fish = group.fish;

            ImageIcon icon = new ImageIcon(fish.getImagePath());

            if (icon.getIconWidth() > 0) {
                Image img = icon.getImage().getScaledInstance(
                    60,
                    60,
                    Image.SCALE_SMOOTH
                );

                iconLabel.setIcon(new ImageIcon(img));
            } else {
                iconLabel.setIcon(null);
            }

            String stars = "⭐".repeat(fish.getRarityStars());

            nameLabel.setText(
                fish.getName()
                + " x"
                + group.count
                + "　｜　單價：$"
                + fish.getPrice()
                + "　｜　小計：$"
                + group.getTotalPrice()
            );

            infoLabel.setText(
                "難易度："
                + stars
                + "　｜　單隻重量："
                + String.format("%.1f", fish.getWeight())
                + " kg"
            );

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                nameLabel.setForeground(list.getSelectionForeground());
                infoLabel.setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                nameLabel.setForeground(Color.BLACK);
                infoLabel.setForeground(Color.DARK_GRAY);
            }

            return this;
        }
    }
}