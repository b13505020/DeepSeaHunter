import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.imageio.ImageIO;

public class WeaponShopScreen extends JPanel {

    private Image bg;
    private Image bossImage;

    private ActionListener backAction;

    private JLabel moneyLabel;
    private JPanel weaponListPanel;

    public WeaponShopScreen(ActionListener backAction) {
        this.backAction = backAction;

        setLayout(null);
        setFocusable(true);
        setPreferredSize(new Dimension(1600, 900));

        loadImages();
        setupUI();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                refreshWeaponList();
                requestFocusInWindow();
            }
        });
    }

    private void loadImages() {
        try {
            bg = ImageIO.read(new File("assets/shop_background.png"));
        } catch (Exception e) {
            System.out.println("❌ 找不到 assets/shop_background.png");
        }

        try {
            bossImage = ImageIO.read(new File("assets/shop_boss.png"));
        } catch (Exception e) {
            System.out.println("❌ 找不到 assets/shop_boss.png，武器商店不顯示老闆");
        }
    }

    private void setupUI() {
        JButton exitBtn = new JButton("EXIT");
        exitBtn.setBounds(50, 800, 150, 50);
        exitBtn.setFocusable(false);
        exitBtn.addActionListener(e -> backAction.actionPerformed(null));
        add(exitBtn);

        moneyLabel = new JLabel();
        moneyLabel.setBounds(1030, 120, 430, 40);
        moneyLabel.setForeground(Color.YELLOW);
        moneyLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
        add(moneyLabel);

        weaponListPanel = new JPanel();
        weaponListPanel.setLayout(new BoxLayout(weaponListPanel, BoxLayout.Y_AXIS));
        weaponListPanel.setBackground(new Color(20, 35, 45));

        JScrollPane scrollPane = new JScrollPane(weaponListPanel);
        scrollPane.setBounds(1000, 180, 500, 580);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane);

        refreshWeaponList();
    }

    private void refreshWeaponList() {
        moneyLabel.setText("Money: $" + InventoryManager.getMoney());

        weaponListPanel.removeAll();

        for (Weapon weapon : WeaponManager.getAllWeapons()) {
            weaponListPanel.add(createWeaponCard(weapon));
            weaponListPanel.add(Box.createVerticalStrut(10));
        }

        weaponListPanel.revalidate();
        weaponListPanel.repaint();
    }

    private JPanel createWeaponCard(Weapon weapon) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setMaximumSize(new Dimension(460, 115));
        card.setPreferredSize(new Dimension(460, 115));
        card.setBackground(new Color(45, 65, 80));
        card.setBorder(BorderFactory.createLineBorder(new Color(0, 220, 255), 2));

        String imagePath = WeaponManager.getImagePath(weapon);
        ImageIcon icon = new ImageIcon(imagePath);
        JLabel imgLabel;

        if (icon.getIconWidth() > 0) {
            Image img = icon.getImage().getScaledInstance(100, 60, Image.SCALE_SMOOTH);
            imgLabel = new JLabel(new ImageIcon(img));
        } else {
            imgLabel = new JLabel("No Img", SwingConstants.CENTER);
            imgLabel.setForeground(Color.WHITE);
            System.out.println("❌ 找不到武器圖片：" + imagePath);
        }

        imgLabel.setPreferredSize(new Dimension(115, 90));
        card.add(imgLabel, BorderLayout.WEST);

        int price = WeaponManager.getPrice(weapon);

        JLabel infoLabel = new JLabel(
            "<html>"
            + "<b style='color:white; font-size:14px;'>" + weapon.getName() + "</b><br>"
            + "<span style='color:#DDDDDD;'>Damage: " + weapon.getDamage() + "</span><br>"
            + "<span style='color:#DDDDDD;'>Range: " + weapon.getRange() + "</span><br>"
            + "<span style='color:#FFD966;'>Price: $" + price + "</span>"
            + "</html>"
        );
        card.add(infoLabel, BorderLayout.CENTER);

        JButton buyBtn = new JButton();

        if (WeaponManager.isOwned(weapon)) {
            buyBtn.setText("OWNED");
            buyBtn.setEnabled(false);
        } else {
            buyBtn.setText("BUY");
            buyBtn.addActionListener(e -> {
                boolean success = WeaponManager.buyWeapon(weapon);

                if (success) {
                    JOptionPane.showMessageDialog(this, "購買成功：" + weapon.getName());
                } else {
                    JOptionPane.showMessageDialog(this, "錢不夠，先去賣魚或下水賺錢！");
                }

                refreshWeaponList();
                requestFocusInWindow();
            });
        }

        buyBtn.setPreferredSize(new Dimension(90, 40));
        buyBtn.setFocusable(false);
        card.add(buyBtn, BorderLayout.EAST);

        return card;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        if (bg != null) {
            g2d.drawImage(bg, 0, 0, 1600, 900, this);
        } else {
            g2d.setColor(new Color(20, 30, 45));
            g2d.fillRect(0, 0, 1600, 900);
        }

        if (bossImage != null) {
            g2d.drawImage(bossImage, 120, 220, 480, 580, this);
        }

        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRoundRect(980, 80, 540, 720, 25, 25);

        g2d.setColor(Color.CYAN);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 28));
        g2d.drawString("BLACKSMITH - WEAPON SHOP", 1020, 105);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 22));
        g2d.drawString("鐵匠：想換更猛的傢伙？錢夠就自己挑。", 100, 820);
    }
}