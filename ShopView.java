import javax.swing.*;
import java.awt.*;

public class ShopView extends JFrame {
    public ShopView() {
        setTitle("神秘商人的移動商店");
        setSize(400, 300);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        int balance = InventoryManager.getTotalPrice(); // 假設你的金錢等於背包總值，或另設變數
        infoPanel.add(new JLabel("💰 目前資金: $" + balance, SwingConstants.CENTER));
        infoPanel.add(new JLabel("你要升級武器嗎？", SwingConstants.CENTER));

        JButton buyBtn = new JButton("購買：重型弩砲 ($1000)");
        buyBtn.addActionListener(e -> {
            if (balance >= 1000) {
                JOptionPane.showMessageDialog(this, "購買成功！現在可以使用 2 鍵切換武器。");
                // 這裡可以加入扣錢邏輯或解鎖邏輯
            } else {
                JOptionPane.showMessageDialog(this, "錢不夠喔！快去下潛抓魚。");
            }
        });

        add(infoPanel, BorderLayout.CENTER);
        add(buyBtn, BorderLayout.SOUTH);
        setVisible(true);
    }
}