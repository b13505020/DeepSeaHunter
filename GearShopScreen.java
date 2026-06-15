import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;

public class GearShopScreen extends JPanel {
    public static final int SCREEN_WIDTH = 1600;
    public static final int SCREEN_HEIGHT = 900;

    private BufferedImage shopBg;
    private BufferedImage bossImage;

    private JLabel moneyLabel;
    private JLabel infoLabel;

    private JTextArea messageArea;

    private JButton suitBtn;
    private JButton oxygenBtn;
    private JButton backpackBtn;
    private JButton sellFishBtn;
    private JButton exitBtn;

    private JButton bossBtn;

    private int bossX = 590;
    private int bossY = 360;

    private final int BOSS_WIDTH = 170;
    private final int BOSS_HEIGHT = 270;

    private final int bossMinX = 555;
    private final int bossMaxX = 700;

    private int bossDirection = 1;
    private final int bossSpeed = 1;

    private final int BOSS_CUT_Y = 570;

    private Timer bossMoveTimer;
    private Timer bossDialogTimer;

    private boolean bossDialogActive = false;

    private Random random = new Random();

    private String[] bossDialogs = {
        "今天想升級什麼？潛得越深，魚通常越值錢。",
        "背包常常滿掉嗎？那你真的該先升級背包了。",
        "氧氣就是命，海裡可不是逞強的地方。",
        "有些魚不難抓，但值不值得帶回來，就看你的背包空間了。",
        "錢不夠沒關係，多下海幾趟，總會翻身的。"
    };

    public GearShopScreen(ActionListener backToLandAction) {
        setLayout(null);
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setFocusable(true);

        loadImages();

        setupInfoLabels();
        setupMessageArea();
        setupHotspotButtons(backToLandAction);
        setupBossButton();
        setupBossMovement();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layoutComponentsForFullscreen();
            }
        });

        setBottomMessage("點選商店中的設備區域進行升級。");

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
        if (moneyLabel != null) {
            setGameBounds(moneyLabel, 40, 25, 500, 40);
            moneyLabel.setFont(scaledFont("Microsoft JhengHei", Font.BOLD, 24));
        }

        if (infoLabel != null) {
            setGameBounds(infoLabel, 40, 65, 1250, 35);
            infoLabel.setFont(scaledFont("Microsoft JhengHei", Font.BOLD, 18));
        }

        if (messageArea != null) {
            setGameBounds(messageArea, 45, 805, 1180, 45);
            messageArea.setFont(scaledFont("Microsoft JhengHei", Font.BOLD, 22));
        }

        if (suitBtn != null) {
            setGameBounds(suitBtn, 80, 145, 430, 430);
        }

        if (oxygenBtn != null) {
            setGameBounds(oxygenBtn, 1010, 145, 290, 430);
        }

        if (backpackBtn != null) {
            setGameBounds(backpackBtn, 1300, 150, 260, 500);
        }

        if (sellFishBtn != null) {
            setGameBounds(sellFishBtn, 820, 430, 170, 160);
        }

        if (exitBtn != null) {
            setGameBounds(exitBtn, 1360, 790, 180, 55);
            exitBtn.setFont(scaledFont("Microsoft JhengHei", Font.BOLD, 20));
        }

        if (bossBtn != null) {
            setGameBounds(bossBtn, bossX, bossY, BOSS_WIDTH, getBossVisibleHeight());
        }

        revalidate();
        repaint();
    }

    private void loadImages() {
        try {
            shopBg = ImageIO.read(new File("assets/shop_background.png"));
        } catch (IOException e) {
            System.out.println("❌ 找不到 assets/shop_background.png");
            e.printStackTrace();
        }

        try {
            bossImage = ImageIO.read(new File("assets/shop_boss.png"));
            bossImage = removeLightBackground(bossImage);
        } catch (IOException e) {
            System.out.println("❌ 找不到 assets/shop_boss.png，將使用替代方塊顯示老闆");
        }
    }

    private BufferedImage removeLightBackground(BufferedImage src) {
        BufferedImage result = new BufferedImage(
            src.getWidth(),
            src.getHeight(),
            BufferedImage.TYPE_INT_ARGB
        );

        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int argb = src.getRGB(x, y);

                int a = (argb >> 24) & 0xff;
                int r = (argb >> 16) & 0xff;
                int g = (argb >> 8) & 0xff;
                int b = argb & 0xff;

                if (a < 10) {
                    result.setRGB(x, y, 0x00000000);
                    continue;
                }

                if (isLightBackgroundPixel(r, g, b)) {
                    result.setRGB(x, y, 0x00000000);
                } else {
                    result.setRGB(x, y, argb);
                }
            }
        }

        return result;
    }

    private boolean isLightBackgroundPixel(int r, int g, int b) {
        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));

        boolean isBright = r > 195 && g > 195 && b > 195;
        boolean isNeutralGray = max - min < 35;

        return isBright && isNeutralGray;
    }

    private void setupInfoLabels() {
        moneyLabel = new JLabel();
        moneyLabel.setForeground(new Color(255, 230, 160));
        add(moneyLabel);

        infoLabel = new JLabel();
        infoLabel.setForeground(new Color(180, 240, 255));
        add(infoLabel);
    }

    private void setupMessageArea() {
        messageArea = new JTextArea();
        messageArea.setForeground(Color.WHITE);
        messageArea.setOpaque(false);
        messageArea.setEditable(false);
        messageArea.setFocusable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        add(messageArea);
    }

    private void setBottomMessage(String text) {
        messageArea.setText(text);
    }

    private void setupHotspotButtons(ActionListener backToLandAction) {
        suitBtn = createHotspotButton("升級潛水衣：增加最大下潛深度");

        suitBtn.addActionListener(e -> {
            boolean success = InventoryManager.upgradeSuit();

            if (success) {
                setBottomMessage("潛水衣升級成功！現在可以潛得更深。");
            } else {
                setBottomMessage("潛水衣升級失敗：錢不夠或已達最高等級。");
            }

            updateShopText();
        });

        add(suitBtn);

        oxygenBtn = createHotspotButton("升級氧氣瓶：增加下水時間");

        oxygenBtn.addActionListener(e -> {
            boolean success = InventoryManager.upgradeOxygen();

            if (success) {
                setBottomMessage("氧氣瓶升級成功！下次下水可以待更久。");
            } else {
                setBottomMessage("氧氣瓶升級失敗：錢不夠或已達最高等級。");
            }

            updateShopText();
        });

        add(oxygenBtn);

        backpackBtn = createHotspotButton("升級背包：增加可攜帶魚的數量");

        backpackBtn.addActionListener(e -> {
            boolean success = InventoryManager.upgradeBackpack();

            if (success) {
                setBottomMessage("背包升級成功！下次可以帶更多魚。");
            } else {
                setBottomMessage("背包升級失敗：錢不夠或已達最高等級。");
            }

            updateShopText();
        });

        add(backpackBtn);

        sellFishBtn = createHotspotButton("打開賣魚介面");

        sellFishBtn.addActionListener(e -> {
            new SellFishView(() -> {
                setBottomMessage("交易完成，金錢與儲藏箱已更新。");
                updateShopText();
                repaint();
            });
        });

        add(sellFishBtn);

        exitBtn = new JButton("返回陸地");
        exitBtn.setForeground(Color.WHITE);
        exitBtn.setBackground(new Color(45, 35, 25));
        exitBtn.setBorder(BorderFactory.createLineBorder(new Color(220, 170, 80), 3));
        exitBtn.setFocusable(false);
        exitBtn.addActionListener(backToLandAction);
        add(exitBtn);
    }

    private JButton createHotspotButton(String tooltip) {
        JButton btn = new JButton();

        btn.setToolTipText(tooltip);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                bossDialogActive = false;
                setBottomMessage(btn.getToolTipText());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!bossDialogActive) {
                    setBottomMessage("點選商店中的設備區域進行升級。");
                }
            }
        });

        return btn;
    }

    private void setupBossButton() {
        bossBtn = new JButton();

        bossBtn.setContentAreaFilled(false);
        bossBtn.setBorderPainted(false);
        bossBtn.setFocusPainted(false);
        bossBtn.setOpaque(false);
        bossBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bossBtn.setToolTipText("和商店老闆說話");

        bossBtn.addActionListener(e -> showRandomBossDialog());

        bossBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!bossDialogActive) {
                    setBottomMessage("點擊老闆可以聊天。");
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!bossDialogActive) {
                    setBottomMessage("點選商店中的設備區域進行升級。");
                }
            }
        });

        add(bossBtn);
    }

    private void setupBossMovement() {
        bossMoveTimer = new Timer(70, e -> {
            bossX += bossDirection * bossSpeed;

            if (bossX <= bossMinX) {
                bossX = bossMinX;
                bossDirection = 1;
            } else if (bossX >= bossMaxX) {
                bossX = bossMaxX;
                bossDirection = -1;
            }

            if (bossBtn != null) {
                setGameBounds(bossBtn, bossX, bossY, BOSS_WIDTH, getBossVisibleHeight());
            }

            repaint();
        });

        bossMoveTimer.start();
    }

    private int getBossVisibleHeight() {
        return Math.max(80, BOSS_CUT_Y - bossY);
    }

    private void showRandomBossDialog() {
        String dialog = bossDialogs[random.nextInt(bossDialogs.length)];

        bossDialogActive = true;
        setBottomMessage("老闆：「" + dialog + "」");

        if (bossDialogTimer != null && bossDialogTimer.isRunning()) {
            bossDialogTimer.stop();
        }

        bossDialogTimer = new Timer(5000, e -> {
            bossDialogActive = false;
            setBottomMessage("點選商店中的設備區域進行升級。");
            repaint();
        });

        bossDialogTimer.setRepeats(false);
        bossDialogTimer.start();

        repaint();
        requestFocusInWindow();
    }

    private void drawBoss(Graphics2D g2d) {
        Graphics2D bossG = (Graphics2D) g2d.create();

        bossG.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );

        if (bossImage != null) {
            Shape oldClip = bossG.getClip();

            bossG.setClip(
                bossX,
                bossY,
                BOSS_WIDTH,
                getBossVisibleHeight()
            );

            bossG.drawImage(
                bossImage,
                bossX,
                bossY,
                BOSS_WIDTH,
                BOSS_HEIGHT,
                this
            );

            bossG.setClip(oldClip);
        } else {
            bossG.setColor(new Color(80, 55, 35));
            bossG.fillRoundRect(
                bossX,
                bossY,
                BOSS_WIDTH,
                getBossVisibleHeight(),
                25,
                25
            );

            bossG.setColor(new Color(230, 190, 120));
            bossG.setFont(new Font("Microsoft JhengHei", Font.BOLD, 24));
            bossG.drawString("商店老闆", bossX + 25, bossY + 90);
        }

        bossG.dispose();
    }

    private void updateShopText() {
        moneyLabel.setText("目前金錢：$" + InventoryManager.getMoney());

        infoLabel.setText(
            "潛水衣 Lv." + InventoryManager.getSuitLevel()
            + " / 最大深度 " + InventoryManager.getMaxDepth() + " m"
            + "　｜　氧氣瓶 Lv." + InventoryManager.getOxygenLevel()
            + " / " + String.format("%.0f", InventoryManager.getMaxOxygenTime()) + " 秒"
            + "　｜　背包 Lv." + InventoryManager.getBackpackLevel()
            + " / 容量 " + InventoryManager.getBackpackCapacity() + " 隻魚"
        );

        updateButtonTooltips();
        layoutComponentsForFullscreen();
        repaint();
    }

    private void updateButtonTooltips() {
        updateSuitTooltip();
        updateOxygenTooltip();
        updateBackpackTooltip();
    }

    private void updateSuitTooltip() {
        if (InventoryManager.getSuitUpgradeCost() == -1) {
            suitBtn.setToolTipText(
                "潛水衣已達最高等級｜目前最大深度 "
                + InventoryManager.getMaxDepth()
                + " m"
            );
            return;
        }

        int currentLevel = InventoryManager.getSuitLevel();
        int currentDepth = InventoryManager.getMaxDepth();
        int nextDepth = getNextSuitDepth();

        suitBtn.setToolTipText(
            "升級潛水衣 Lv."
            + currentLevel
            + " → Lv."
            + (currentLevel + 1)
            + "｜費用 $"
            + InventoryManager.getSuitUpgradeCost()
            + "｜最大深度 "
            + currentDepth
            + " m → "
            + nextDepth
            + " m"
        );
    }

    private void updateOxygenTooltip() {
        if (InventoryManager.getOxygenUpgradeCost() == -1) {
            oxygenBtn.setToolTipText(
                "氧氣瓶已達最高等級｜目前氧氣時間 "
                + String.format("%.0f", InventoryManager.getMaxOxygenTime())
                + " 秒"
            );
            return;
        }

        int currentLevel = InventoryManager.getOxygenLevel();
        double currentTime = InventoryManager.getMaxOxygenTime();
        double nextTime = getNextOxygenTime();

        oxygenBtn.setToolTipText(
            "升級氧氣瓶 Lv."
            + currentLevel
            + " → Lv."
            + (currentLevel + 1)
            + "｜費用 $"
            + InventoryManager.getOxygenUpgradeCost()
            + "｜氧氣時間 "
            + String.format("%.0f", currentTime)
            + " 秒 → "
            + String.format("%.0f", nextTime)
            + " 秒"
        );
    }

    private void updateBackpackTooltip() {
        if (InventoryManager.getBackpackUpgradeCost() == -1) {
            backpackBtn.setToolTipText(
                "背包已達最高等級｜目前容量 "
                + InventoryManager.getBackpackCapacity()
                + " 隻魚"
            );
            return;
        }

        int currentLevel = InventoryManager.getBackpackLevel();
        int currentCapacity = InventoryManager.getBackpackCapacity();
        int nextCapacity = getNextBackpackCapacity();

        backpackBtn.setToolTipText(
            "升級背包 Lv."
            + currentLevel
            + " → Lv."
            + (currentLevel + 1)
            + "｜費用 $"
            + InventoryManager.getBackpackUpgradeCost()
            + "｜容量 "
            + currentCapacity
            + " 隻魚 → "
            + nextCapacity
            + " 隻魚"
        );
    }

    private int getNextSuitDepth() {
        int nextLevel = InventoryManager.getSuitLevel() + 1;

        if (nextLevel == 2) {
            return 1500;
        }

        if (nextLevel == 3) {
            return 2400;
        }

        return InventoryManager.getMaxDepth();
    }

    private double getNextOxygenTime() {
        int nextLevel = InventoryManager.getOxygenLevel() + 1;

        if (nextLevel == 2) {
            return 90.0;
        }

        if (nextLevel == 3) {
            return 120.0;
        }

        return InventoryManager.getMaxOxygenTime();
    }

    private int getNextBackpackCapacity() {
        int nextLevel = InventoryManager.getBackpackLevel() + 1;

        if (nextLevel == 2) {
            return 8;
        }

        if (nextLevel == 3) {
            return 12;
        }

        return InventoryManager.getBackpackCapacity();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        updateShopText();
        setBottomMessage("點選商店中的設備區域進行升級。");
        SwingUtilities.invokeLater(() -> layoutComponentsForFullscreen());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();

        double scaleX = getScaleX();
        double scaleY = getScaleY();

        g2d.scale(scaleX, scaleY);

        if (shopBg != null) {
            g2d.drawImage(shopBg, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, this);
        } else {
            g2d.setColor(new Color(20, 25, 35));
            g2d.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        }

        drawBoss(g2d);
        drawBottomMessageBox(g2d);

        g2d.dispose();
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