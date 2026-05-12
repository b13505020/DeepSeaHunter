import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SellFishView extends JFrame {
    private DefaultListModel<Fish> listModel = new DefaultListModel<>();
    private JList<Fish> fishList = new JList<>(listModel);
    private JLabel infoLabel = new JLabel();

    private Runnable afterSellAction;

    public SellFishView(Runnable afterSellAction) {
        this.afterSellAction = afterSellAction;

        setTitle("選擇要賣出的魚");
        setSize(700, 550);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        infoLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        add(infoLabel, BorderLayout.NORTH);

        fishList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        fishList.setCellRenderer(new FishSellRenderer());

        JScrollPane scrollPane = new JScrollPane(fishList);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton sellSelectedBtn = new JButton("賣出選取魚");
        sellSelectedBtn.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));

        sellSelectedBtn.addActionListener(e -> {
            List<Fish> selectedFish = fishList.getSelectedValuesList();

            if (selectedFish.isEmpty()) {
                JOptionPane.showMessageDialog(this, "請先選擇要賣出的魚！");
                return;
            }

            int result = JOptionPane.showConfirmDialog(
                this,
                "確定要賣出選取的 " + selectedFish.size() + " 隻魚嗎？",
                "確認賣出",
                JOptionPane.YES_NO_OPTION
            );

            if (result != JOptionPane.YES_OPTION) {
                return;
            }

            int earned = InventoryManager.sellSelectedFish(selectedFish);

            JOptionPane.showMessageDialog(
                this,
                "成功賣出選取魚，獲得 $" + earned
            );

            reloadFishList();

            if (afterSellAction != null) {
                afterSellAction.run();
            }
        });

        JButton sellAllBtn = new JButton("賣出全部魚");
        sellAllBtn.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));

        sellAllBtn.addActionListener(e -> {
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
        });

        JButton closeBtn = new JButton("關閉");
        closeBtn.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        closeBtn.addActionListener(e -> dispose());

        buttonPanel.add(sellSelectedBtn);
        buttonPanel.add(sellAllBtn);
        buttonPanel.add(closeBtn);

        add(buttonPanel, BorderLayout.SOUTH);

        reloadFishList();
        setVisible(true);
    }

    private void reloadFishList() {
        listModel.clear();

        for (Fish f : InventoryManager.getStorage()) {
            listModel.addElement(f);
        }

        infoLabel.setText(
            "目前金錢：$" + InventoryManager.getMoney()
            + "　｜　儲藏箱價值：$" + InventoryManager.getStorageValue()
            + "　｜　魚數量：" + InventoryManager.getStorage().size()
        );
    }

    private class FishSellRenderer extends JPanel implements ListCellRenderer<Fish> {
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
            infoLabel.setForeground(Color.DARK_GRAY);

            textPanel.add(nameLabel);
            textPanel.add(infoLabel);

            add(iconLabel, BorderLayout.WEST);
            add(textPanel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(
            JList<? extends Fish> list,
            Fish fish,
            int index,
            boolean isSelected,
            boolean cellHasFocus
        ) {
            ImageIcon icon = new ImageIcon(fish.getImagePath());

            if (icon.getIconWidth() > 0) {
                Image img = icon.getImage().getScaledInstance(55, 55, Image.SCALE_SMOOTH);
                iconLabel.setIcon(new ImageIcon(img));
            } else {
                iconLabel.setIcon(null);
            }

            String stars = "⭐".repeat(fish.getRarityStars());

            nameLabel.setText(fish.getName() + "　價值：$" + fish.getPrice());
            infoLabel.setText(
                "難易度：" + stars
                + "　重量：" + String.format("%.1f", fish.getWeight()) + " kg"
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