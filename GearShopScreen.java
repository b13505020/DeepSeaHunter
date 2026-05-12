import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class GearShopScreen extends JPanel {
    private BufferedImage shopBg;

    private JLabel moneyLabel;
    private JLabel infoLabel;
    private JLabel hintLabel;

    private JButton suitBtn;
    private JButton oxygenBtn;
    private JButton backpackBtn;
    private JButton sellFishBtn;
    private JButton exitBtn;

    public GearShopScreen(ActionListener backToLandAction) {
        setLayout(null);
        setPreferredSize(new Dimension(1600, 900));
        setFocusable(true);

        loadImages();

        setupInfoLabels();
        setupHotspotButtons(backToLandAction);

        updateShopText("點選商店中的設備區域進行升級。");
    }

    private void loadImages() {
        try {
            shopBg = ImageIO.read(new File("assets/shop_background.png"));
        } catch (IOException e) {
            System.out.println("❌ 找不到 assets/shop_background.png");
            e.printStackTrace();
        }
    }

    private void setupInfoLabels() {
        moneyLabel = new JLabel();
        moneyLabel.setBounds(40, 25, 500, 40);
        moneyLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 24));
        moneyLabel.setForeground(new Color(255, 230, 160));
        add(moneyLabel);

        infoLabel = new JLabel();
        infoLabel.setBounds(40, 65, 1000, 35);
        infoLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
        infoLabel.setForeground(new Color(180, 240, 255));
        add(infoLabel);

        hintLabel = new JLabel();
        hintLabel.setBounds(40, 805, 1200, 40);
        hintLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 22));
        hintLabel.setForeground(Color.WHITE);
        add(hintLabel);
    }

    private void setupHotspotButtons(ActionListener backToLandAction) {
        // 左側：潛水衣升級區
        suitBtn = createHotspotButton(
            "升級潛水衣：增加最大下潛深度",
            80,
            145,
            430,
            430
        );

        suitBtn.addActionListener(e -> {
            boolean success = InventoryManager.upgradeSuit();

            if (success) {
                updateShopText("潛水衣升級成功！現在可以潛得更深。");
            } else {
                updateShopText("潛水衣升級失敗：錢不夠或已達最高等級。");
            }
        });

        add(suitBtn);

        // 右中：氧氣瓶升級區
        oxygenBtn = createHotspotButton(
            "升級氧氣瓶：增加下水時間",
            1010,
            145,
            290,
            430
        );

        oxygenBtn.addActionListener(e -> {
            boolean success = InventoryManager.upgradeOxygen();

            if (success) {
                updateShopText("氧氣瓶升級成功！下次下水可以待更久。");
            } else {
                updateShopText("氧氣瓶升級失敗：錢不夠或已達最高等級。");
            }
        });

        add(oxygenBtn);

        // 最右：背包升級區
        backpackBtn = createHotspotButton(
            "升級背包：增加可攜帶魚的數量",
            1300,
            150,
            260,
            500
        );

        backpackBtn.addActionListener(e -> {
            boolean success = InventoryManager.upgradeBackpack();

            if (success) {
                updateShopText("背包升級成功！下次可以帶更多魚。");
            } else {
                updateShopText("背包升級失敗：錢不夠或已達最高等級。");
            }
        });

        add(backpackBtn);

        // 中間桌上的電腦 / 結算機：賣魚
        sellFishBtn = createHotspotButton(
            "打開賣魚介面",
            820,
            430,
            170,
            160
        );

        sellFishBtn.addActionListener(e -> {
            new SellFishView(() -> {
                updateShopText("交易完成，金錢與儲藏箱已更新。");
                repaint();
            });
        });

        add(sellFishBtn);

        // 離開商店按鈕：放在右下角，避免破壞圖片
        exitBtn = new JButton("返回陸地");
        exitBtn.setBounds(1360, 790, 180, 55);
        exitBtn.setFont(new Font("Microsoft JhengHei", Font.BOLD, 20));
        exitBtn.setForeground(Color.WHITE);
        exitBtn.setBackground(new Color(45, 35, 25));
        exitBtn.setBorder(BorderFactory.createLineBorder(new Color(220, 170, 80), 3));
        exitBtn.setFocusable(false);
        exitBtn.addActionListener(backToLandAction);
        add(exitBtn);
    }

    private JButton createHotspotButton(String tooltip, int x, int y, int w, int h) {
        JButton btn = new JButton();

        btn.setBounds(x, y, w, h);
        btn.setToolTipText(tooltip);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hintLabel.setText(tooltip);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hintLabel.setText("點選商店中的設備區域進行升級。");
            }
        });

        return btn;
    }

    private void updateShopText(String message) {
        moneyLabel.setText("目前金錢：$" + InventoryManager.getMoney());

        infoLabel.setText(
            "潛水衣 Lv." + InventoryManager.getSuitLevel()
            + " / 最大深度 " + InventoryManager.getMaxDepth() + " m"
            + "　｜　氧氣瓶 Lv." + InventoryManager.getOxygenLevel()
            + " / " + String.format("%.0f", InventoryManager.getMaxOxygenTime()) + " 秒"
            + "　｜　背包 Lv." + InventoryManager.getBackpackLevel()
            + " / 容量 " + InventoryManager.getBackpackCapacity()
        );

        hintLabel.setText(message);

        updateButtonTooltips();
        repaint();
    }

    private void updateButtonTooltips() {
        if (InventoryManager.getSuitUpgradeCost() == -1) {
            suitBtn.setToolTipText("潛水衣已達最高等級");
        } else {
            suitBtn.setToolTipText(
                "升級潛水衣 Lv."
                + InventoryManager.getSuitLevel()
                + " → Lv."
                + (InventoryManager.getSuitLevel() + 1)
                + "，需要 $"
                + InventoryManager.getSuitUpgradeCost()
            );
        }

        if (InventoryManager.getOxygenUpgradeCost() == -1) {
            oxygenBtn.setToolTipText("氧氣瓶已達最高等級");
        } else {
            oxygenBtn.setToolTipText(
                "升級氧氣瓶 Lv."
                + InventoryManager.getOxygenLevel()
                + " → Lv."
                + (InventoryManager.getOxygenLevel() + 1)
                + "，需要 $"
                + InventoryManager.getOxygenUpgradeCost()
            );
        }

        if (InventoryManager.getBackpackUpgradeCost() == -1) {
            backpackBtn.setToolTipText("背包已達最高等級");
        } else {
            backpackBtn.setToolTipText(
                "升級背包 Lv."
                + InventoryManager.getBackpackLevel()
                + " → Lv."
                + (InventoryManager.getBackpackLevel() + 1)
                + "，需要 $"
                + InventoryManager.getBackpackUpgradeCost()
            );
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        updateShopText("點選商店中的設備區域進行升級。");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (shopBg != null) {
            g.drawImage(shopBg, 0, 0, 1600, 900, this);
        } else {
            g.setColor(new Color(20, 25, 35));
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        drawBottomMessageBox(g);
    }

    private void drawBottomMessageBox(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(25, 785, 1250, 75, 25, 25);

        g2d.setColor(new Color(220, 170, 80));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(25, 785, 1250, 75, 25, 25);
    }
}