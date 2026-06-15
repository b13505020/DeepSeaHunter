import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Map;

public class WeaponShopScreen extends JPanel {

    private Image bg;
    private BufferedImage bossImage;

    private ActionListener backAction;
    private int bossX = 120;
    private int bossY = 220;
    private int bossW = 480;
    private int bossH = 580;

    private int bossDirection = 1;
    private int bossLeftLimit = 90;
    private int bossRightLimit = 210;

    private String[] bossLines = {
    "鐵匠：想換更猛的傢伙？付錢或帶材料來都行。",
    "鐵匠：水下可不是開玩笑的地方，武器要選對。",
    "鐵匠：狙擊槍打得遠，榴彈發射器火力猛。",
    "鐵匠：沒錢就先去賣魚，別只會看。",
    "鐵匠：沙灘上的齒輪、珊瑚和石材都能拿來製作。",
    "鐵匠：沒錢就去海灘找材料，我照樣能幫你打造。"
};

    private int currentBossLine = 0;

    private JLabel moneyLabel;
    private JPanel weaponListPanel;

    public WeaponShopScreen(ActionListener backAction) {
        this.backAction = backAction;

        setLayout(null);
        setFocusable(true);
        setPreferredSize(new Dimension(1600, 900));

        loadImages();
        setupUI();
        setupBossMovement();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                refreshWeaponList();
                requestFocusInWindow();
            }
        });
    }
    private void setupBossMovement() {
        Timer bossTimer = new Timer(80, e -> {
            bossX += bossDirection;
    
            if (bossX <= bossLeftLimit || bossX >= bossRightLimit) {
                bossDirection *= -1;
            }
    
            repaint();
        });
    
        bossTimer.start();
    
        Timer dialogueTimer = new Timer(3500, e -> {
            currentBossLine++;
    
            if (currentBossLine >= bossLines.length) {
                currentBossLine = 0;
            }
    
            repaint();
        });
    
        dialogueTimer.start();
    }

    private void loadImages() {
        try {
            bg = ImageIO.read(new File("assets/shop_background.png"));
        } catch (Exception e) {
            System.out.println("❌ 找不到 assets/shop_background.png");
        }

        try {
            BufferedImage originalBoss = ImageIO.read(new File("assets/shop_boss.png"));
            bossImage = makeEdgeBackgroundTransparent(originalBoss);
        } catch (Exception e) {
            System.out.println("❌ 找不到 assets/shop_boss.png，武器商店不顯示老闆");
        }
    }

    private void setupUI() {
        JButton exitBtn = new JButton("EXIT");
        exitBtn.setBounds(1360, 790, 150, 60);
        exitBtn.setFocusable(false);
        exitBtn.addActionListener(e -> backAction.actionPerformed(null));
        add(exitBtn);

        moneyLabel = new JLabel();
        moneyLabel.setBounds(1015, 790, 480, 40);
        moneyLabel.setForeground(Color.YELLOW);
        moneyLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 21));
        add(moneyLabel);

        weaponListPanel = new JPanel();
        weaponListPanel.setLayout(new BoxLayout(weaponListPanel, BoxLayout.Y_AXIS));
        weaponListPanel.setBackground(new Color(20, 35, 45));

        JScrollPane scrollPane = new JScrollPane(weaponListPanel);
        scrollPane.setBounds(1000, 170, 500, 560);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane);

        refreshWeaponList();
    }

    private BufferedImage makeEdgeBackgroundTransparent(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
    
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.drawImage(src, 0, 0, null);
        g2.dispose();
    
        boolean[][] visited = new boolean[w][h];
        ArrayDeque<int[]> queue = new ArrayDeque<>();
    
        for (int x = 0; x < w; x++) {
            queue.add(new int[] { x, 0 });
            queue.add(new int[] { x, h - 1 });
        }
    
        for (int y = 0; y < h; y++) {
            queue.add(new int[] { 0, y });
            queue.add(new int[] { w - 1, y });
        }
    
        int[] dx = { 1, -1, 0, 0 };
        int[] dy = { 0, 0, 1, -1 };
    
        while (!queue.isEmpty()) {
            int[] p = queue.removeFirst();
            int x = p[0];
            int y = p[1];
    
            if (x < 0 || x >= w || y < 0 || y >= h) {
                continue;
            }
    
            if (visited[x][y]) {
                continue;
            }
    
            visited[x][y] = true;
    
            int argb = result.getRGB(x, y);
    
            if (!isBackgroundLike(argb)) {
                continue;
            }
    
            result.setRGB(x, y, argb & 0x00FFFFFF);
    
            for (int i = 0; i < 4; i++) {
                queue.add(new int[] { x + dx[i], y + dy[i] });
            }
        }
    
        return result;
    }
    
    private boolean isBackgroundLike(int argb) {
        int a = (argb >> 24) & 0xff;
        int r = (argb >> 16) & 0xff;
        int g = (argb >> 8) & 0xff;
        int b = argb & 0xff;
    
        if (a < 10) {
            return true;
        }
    
        // 白底
        if (r > 235 && g > 235 && b > 235) {
            return true;
        }
    
        // 淺灰底 / 棋盤格背景
        if (Math.abs(r - g) < 10 && Math.abs(g - b) < 10 && r >= 170 && r <= 245) {
            return true;
        }
    
        return false;
    }
    private void refreshWeaponList() {
        moneyLabel.setText(
            "金錢：$" + InventoryManager.getMoney()
            + "　｜　儲藏素材：" + InventoryManager.getStorageMaterialTotalCount()
        );

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
        card.setMaximumSize(new Dimension(460, 150));
        card.setPreferredSize(new Dimension(460, 150));
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
        String recipeText = buildRecipeHtml(weapon);

        JLabel infoLabel = new JLabel(
            "<html>"
            + "<b style='color:white; font-size:14px;'>" + weapon.getName() + "</b><br>"
            + "<span style='color:#DDDDDD;'>Damage: " + weapon.getDamage() + "</span><br>"
            + "<span style='color:#DDDDDD;'>Range: " + weapon.getRange() + "</span><br>"
            + "<span style='color:#FFD966;'>金錢購買：$" + price + "</span><br>"
            + recipeText
            + "</html>"
        );
        card.add(infoLabel, BorderLayout.CENTER);

        JButton buyBtn = new JButton();
        JButton craftBtn = new JButton("CRAFT");
        JPanel actionPanel = new JPanel(new GridLayout(2, 1, 0, 8));
        actionPanel.setOpaque(false);
        actionPanel.setPreferredSize(new Dimension(92, 96));

        if (WeaponManager.isOwned(weapon)) {
            buyBtn.setText("OWNED");
            buyBtn.setEnabled(false);
            craftBtn.setText("OWNED");
            craftBtn.setEnabled(false);
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
        
            craftBtn.setEnabled(WeaponManager.canCraft(weapon));
            craftBtn.setToolTipText(
                craftBtn.isEnabled()
                ? "使用儲藏箱內的沙灘素材製作"
                : "儲藏箱內的製作素材不足"
            );
            craftBtn.addActionListener(e -> {
                boolean success = WeaponManager.craftWeapon(weapon);

                if (success) {
                    JOptionPane.showMessageDialog(this, "製作成功：" + weapon.getName());
                } else {
                    JOptionPane.showMessageDialog(
                        this,
                        "製作素材不足：\n" + buildMissingMaterialText(weapon)
                    );
                }

                refreshWeaponList();
                requestFocusInWindow();
            });
        }

        buyBtn.setFocusable(false);
        craftBtn.setFocusable(false);
        actionPanel.add(buyBtn);
        actionPanel.add(craftBtn);
        card.add(actionPanel, BorderLayout.EAST);

        return card;
    }

    private String buildRecipeHtml(Weapon weapon) {
        Map<String, Integer> recipe = WeaponManager.getRecipe(weapon);

        if (recipe.isEmpty()) {
            return "<span style='color:#AAAAAA;'>初始武器，不需製作</span>";
        }

        StringBuilder text = new StringBuilder(
            "<span style='color:#8FE8FF;'>素材製作：</span>"
        );
        boolean first = true;

        for (Map.Entry<String, Integer> entry : recipe.entrySet()) {
            if (!first) {
                text.append("、");
            }

            int owned = InventoryManager.getStorageMaterialCount(entry.getKey());
            String color = owned >= entry.getValue() ? "#9CFF9C" : "#FF9C9C";
            text.append("<span style='color:")
                .append(color)
                .append(";'>")
                .append(entry.getKey())
                .append(" ")
                .append(owned)
                .append("/")
                .append(entry.getValue())
                .append("</span>");
            first = false;
        }

        return text.toString();
    }

    private String buildMissingMaterialText(Weapon weapon) {
        StringBuilder text = new StringBuilder();

        for (Map.Entry<String, Integer> entry : WeaponManager.getRecipe(weapon).entrySet()) {
            int owned = InventoryManager.getStorageMaterialCount(entry.getKey());

            if (owned < entry.getValue()) {
                if (text.length() > 0) {
                    text.append("\n");
                }

                text.append(entry.getKey())
                    .append("：")
                    .append(owned)
                    .append("/")
                    .append(entry.getValue());
            }
        }

        return text.length() == 0 ? "請重新開啟武器商店後再試。" : text.toString();
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
            g2d.drawImage(bossImage, bossX, bossY, bossW, bossH, this);
        }

        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRoundRect(980, 80, 540, 720, 25, 25);

        g2d.setColor(Color.CYAN);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 28));
        g2d.drawString("BLACKSMITH - WEAPON SHOP", 1020, 105);

        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRoundRect(50, 750, 1500, 120, 30, 30);

        g2d.setColor(new Color(200, 150, 50));
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRoundRect(50, 750, 1500, 120, 30, 30);
 
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 24));
        g2d.drawString(bossLines[currentBossLine], 100, 820);  
    }
}