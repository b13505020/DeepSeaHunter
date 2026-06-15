import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;

public class WeaponShopScreen extends JPanel {

    public static final int SCREEN_WIDTH = 1600;
    public static final int SCREEN_HEIGHT = 900;

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
        "鐵匠：想換更猛的傢伙？錢夠就自己挑。",
        "鐵匠：水下可不是開玩笑的地方，武器要選對。",
        "鐵匠：狙擊槍打得遠，榴彈發射器火力猛。",
        "鐵匠：沒錢就先去賣魚，別只會看。"
    };

    private int currentBossLine = 0;

    private JLabel moneyLabel;
    private JPanel weaponListPanel;
    private JScrollPane scrollPane;
    private JButton exitBtn;

    private Timer bossTimer;
    private Timer dialogueTimer;

    public WeaponShopScreen(ActionListener backAction) {
        this.backAction = backAction;

        setLayout(null);
        setFocusable(true);
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));

        loadImages();
        setupUI();
        setupBossMovement();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                refreshWeaponList();
                layoutComponentsForFullscreen();
                requestFocusInWindow();
            }

            @Override
            public void componentResized(ComponentEvent e) {
                layoutComponentsForFullscreen();
            }
        });

        SwingUtilities.invokeLater(() -> layoutComponentsForFullscreen());
    }

    private double getScaleX() {
        if (getWidth() <= 0) {
            return 1.0;
        }

        return getWidth() / (double) SCREEN_WIDTH;
    }

    private double getScaleY() {
        if (getHeight() <= 0) {
            return 1.0;
        }

        return getHeight() / (double) SCREEN_HEIGHT;
    }

    private double getFontScale() {
        return Math.min(getScaleX(), getScaleY());
    }

    private int sx(int value) {
        return (int) Math.round(value * getScaleX());
    }

    private int sy(int value) {
        return (int) Math.round(value * getScaleY());
    }

    private int sw(int value) {
        return (int) Math.round(value * getScaleX());
    }

    private int sh(int value) {
        return (int) Math.round(value * getScaleY());
    }

    private Font scaledFont(String name, int style, int size) {
        int scaledSize = Math.max(12, (int) Math.round(size * getFontScale()));
        return new Font(name, style, scaledSize);
    }

    private void setGameBounds(Component component, int x, int y, int w, int h) {
        component.setBounds(sx(x), sy(y), sw(w), sh(h));
    }

    private void layoutComponentsForFullscreen() {
        if (exitBtn != null) {
            // 改到右下角，避免擋住左下角鐵匠對話框
            setGameBounds(exitBtn, 1360, 805, 170, 50);
            exitBtn.setFont(scaledFont("Microsoft JhengHei", Font.BOLD, 18));
            exitBtn.setText("返回陸地");
        }

        if (moneyLabel != null) {
            setGameBounds(moneyLabel, 1030, 120, 430, 40);
            moneyLabel.setFont(scaledFont("Monospaced", Font.BOLD, 24));
        }

        if (scrollPane != null) {
            setGameBounds(scrollPane, 1000, 180, 500, 580);
        }

        if (weaponListPanel != null) {
            int cardW = Math.max(300, sw(460));
            int cardH = Math.max(90, sh(115));

            for (Component c : weaponListPanel.getComponents()) {
                if (c instanceof JPanel) {
                    c.setMaximumSize(new Dimension(cardW, cardH));
                    c.setPreferredSize(new Dimension(cardW, cardH));
                }
            }
        }

        revalidate();
        repaint();
    }

    private void setupBossMovement() {
        bossTimer = new Timer(80, e -> {
            bossX += bossDirection;

            if (bossX <= bossLeftLimit || bossX >= bossRightLimit) {
                bossDirection *= -1;
            }

            repaint();
        });

        bossTimer.start();

        dialogueTimer = new Timer(3500, e -> {
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
        exitBtn = new JButton("返回陸地");
        exitBtn.setFocusable(false);
        exitBtn.setForeground(Color.WHITE);
        exitBtn.setBackground(new Color(45, 35, 25));
        exitBtn.setBorder(BorderFactory.createLineBorder(new Color(220, 170, 80), 3));
        exitBtn.addActionListener(e -> {
            if (backAction != null) {
                backAction.actionPerformed(null);
            }
        });
        add(exitBtn);

        moneyLabel = new JLabel();
        moneyLabel.setForeground(Color.YELLOW);
        moneyLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
        add(moneyLabel);

        weaponListPanel = new JPanel();
        weaponListPanel.setLayout(new BoxLayout(weaponListPanel, BoxLayout.Y_AXIS));
        weaponListPanel.setBackground(new Color(20, 35, 45));

        scrollPane = new JScrollPane(weaponListPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane);

        refreshWeaponList();
        layoutComponentsForFullscreen();
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

        if (r > 235 && g > 235 && b > 235) {
            return true;
        }

        if (Math.abs(r - g) < 10 && Math.abs(g - b) < 10 && r >= 170 && r <= 245) {
            return true;
        }

        return false;
    }

    private void refreshWeaponList() {
        if (moneyLabel != null) {
            moneyLabel.setText("Money: $" + InventoryManager.getMoney());
        }

        weaponListPanel.removeAll();

        for (Weapon weapon : WeaponManager.getAllWeapons()) {
            weaponListPanel.add(createWeaponCard(weapon));
            weaponListPanel.add(Box.createVerticalStrut(10));
        }

        weaponListPanel.revalidate();
        weaponListPanel.repaint();

        layoutComponentsForFullscreen();
    }

    private JPanel createWeaponCard(Weapon weapon) {
        JPanel card = new JPanel(new BorderLayout(10, 10));

        int cardW = Math.max(300, sw(460));
        int cardH = Math.max(90, sh(115));

        card.setMaximumSize(new Dimension(cardW, cardH));
        card.setPreferredSize(new Dimension(cardW, cardH));
        card.setBackground(new Color(45, 65, 80));
        card.setBorder(BorderFactory.createLineBorder(new Color(0, 220, 255), 2));

        String imagePath = WeaponManager.getImagePath(weapon);
        ImageIcon icon = new ImageIcon(imagePath);
        JLabel imgLabel;

        if (icon.getIconWidth() > 0) {
            Image img = icon.getImage().getScaledInstance(
                Math.max(80, sw(100)),
                Math.max(48, sh(60)),
                Image.SCALE_SMOOTH
            );
            imgLabel = new JLabel(new ImageIcon(img));
        } else {
            imgLabel = new JLabel("No Img", SwingConstants.CENTER);
            imgLabel.setForeground(Color.WHITE);
            System.out.println("❌ 找不到武器圖片：" + imagePath);
        }

        imgLabel.setPreferredSize(new Dimension(Math.max(95, sw(115)), Math.max(70, sh(90))));
        card.add(imgLabel, BorderLayout.WEST);

        int price = WeaponManager.getPrice(weapon);
        int infoFontSize = Math.max(11, (int) Math.round(14 * getFontScale()));

        JLabel infoLabel = new JLabel(
            "<html>"
            + "<b style='color:white; font-size:" + infoFontSize + "px;'>" + weapon.getName() + "</b><br>"
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

        buyBtn.setPreferredSize(new Dimension(Math.max(70, sw(90)), Math.max(35, sh(40))));
        buyBtn.setFocusable(false);
        card.add(buyBtn, BorderLayout.EAST);

        return card;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        layoutComponentsForFullscreen();

        Graphics2D g2d = (Graphics2D) g.create();

        double scaleX = getScaleX();
        double scaleY = getScaleY();

        g2d.scale(scaleX, scaleY);

        if (bg != null) {
            g2d.drawImage(bg, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, this);
        } else {
            g2d.setColor(new Color(20, 30, 45));
            g2d.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        }

        if (bossImage != null) {
            g2d.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
            );

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

        g2d.dispose();
    }
}