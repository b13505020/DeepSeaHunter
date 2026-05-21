import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * TavernView
 *
 * 這是乾淨重寫版，目的不是再新增大功能，而是修掉前一版括號錯位造成的編譯錯誤。
 * 直接整份覆蓋 TavernView.java，不要貼在舊檔案後面。
 */
public class TavernView extends JFrame {

    private static class Drink {
        String name;
        String type;
        int price;
        String description;
        String effectText;
        int morale;
        int focus;
        int luck;
        Color color;

        Drink(String name, String type, int price, String description, String effectText,
              int morale, int focus, int luck, Color color) {
            this.name = name;
            this.type = type;
            this.price = price;
            this.description = description;
            this.effectText = effectText;
            this.morale = morale;
            this.focus = focus;
            this.luck = luck;
            this.color = color;
        }

        int totalBuff() {
            return morale + focus + luck;
        }
    }

    private static class OwnedDrink {
        Drink drink;
        int amount;

        OwnedDrink(Drink drink, int amount) {
            this.drink = drink;
            this.amount = amount;
        }
    }

    private static final List<OwnedDrink> ownedDrinks = new ArrayList<>();
    private static int moraleBuff = 0;
    private static int focusBuff = 0;
    private static int luckBuff = 0;
    private static int totalConsumed = 0;

    private final List<Drink> drinks = new ArrayList<>();

    private JPanel drinkListPanel;
    private JLabel moneyLabel;
    private JLabel ownedLabel;
    private JLabel buffLabel;
    private JLabel recommendationLabel;
    private JTextArea detailArea;
    private JTextArea logArea;

    private JComboBox<String> categoryBox;
    private JComboBox<String> sortBox;
    private JCheckBox ownedOnlyBox;
    private JCheckBox affordableOnlyBox;
    private JTextField searchField;
    private JSpinner quantitySpinner;

    private JButton buyOneButton;
    private JButton buyFiveButton;
    private JButton buyQuantityButton;
    private JButton drinkOneButton;
    private JButton drinkQuantityButton;
    private JButton drinkAllButton;
    private JButton recommendButton;
    private JButton resetBuffButton;
    private JButton clearSearchButton;

    private int selectedIndex = 0;

    public TavernView() {
        setTitle("Tavern - 酒館補給");
        setSize(1320, 840);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        getRootPane().registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        createDrinks();
        buildUI();
        refreshAll();

        setVisible(true);
    }

    private void createDrinks() {
        drinks.clear();

        drinks.add(new Drink("海霧麥芽飲", "招牌飲品", 120, "帶有麥芽香和淡淡海鹽味，是潛水員最常點的入門款。", "士氣小幅提升，適合出任務前喝。", 1, 0, 0, new Color(205, 150, 65)));
        drinks.add(new Drink("珊瑚莓果露", "果飲", 160, "以珊瑚莓調成，入口偏甜，顏色明亮。", "士氣與幸運感提升。", 1, 0, 1, new Color(225, 95, 120)));
        drinks.add(new Drink("深海可可", "熱飲", 180, "濃厚熱可可，適合返航後恢復精神。", "士氣提升，疲勞感下降。", 2, 0, 0, new Color(115, 72, 45)));
        drinks.add(new Drink("藍潮薄荷水", "清爽飲品", 150, "帶有藍色光澤的薄荷飲，喝起來很清涼。", "專注小幅提升。", 0, 1, 0, new Color(65, 165, 210)));
        drinks.add(new Drink("水母氣泡飲", "氣泡飲", 190, "杯中會冒出像水母般的細緻泡泡。", "士氣提升，幸運感小幅提升。", 1, 0, 1, new Color(155, 210, 235)));
        drinks.add(new Drink("珍珠米露", "甜飲", 170, "海島米做成的甜飲，口感溫和。", "士氣提升，適合日常補給。", 1, 0, 0, new Color(235, 215, 170)));
        drinks.add(new Drink("鹽風檸檬茶", "茶飲", 210, "帶有海風香氣的檸檬茶，後味乾淨。", "專注提升，適合高壓任務前飲用。", 0, 2, 0, new Color(230, 205, 80)));
        drinks.add(new Drink("漩渦薑汁飲", "刺激飲品", 240, "入口微辣，像被小漩渦推了一下。", "士氣和專注提升。", 1, 1, 0, new Color(210, 125, 60)));
        drinks.add(new Drink("夜光葡萄汁", "高級果飲", 260, "杯中有微微夜光，是酒館招牌之一。", "幸運感提升，適合重要任務前飲用。", 0, 0, 2, new Color(125, 95, 220)));
        drinks.add(new Drink("船長特調", "招牌飲品", 320, "任務接取人推薦的特調飲品，味道厚實。", "士氣大幅提升。", 3, 0, 0, new Color(175, 90, 55)));
        drinks.add(new Drink("海鹽梅子飲", "酸甜飲品", 190, "酸甜口味搭配一點海鹽，容易入口。", "士氣小幅提升，專注小幅提升。", 1, 1, 0, new Color(210, 115, 155)));
        drinks.add(new Drink("冰晶蘇打", "冰飲", 280, "冰冷透明，喝下去像吞下一塊冰晶。", "專注大幅提升。", 0, 3, 0, new Color(130, 225, 245)));
        drinks.add(new Drink("火山辣可可", "特殊熱飲", 300, "入口微辣，像海底火山一樣熱。", "士氣與專注提升。", 2, 1, 0, new Color(220, 80, 45)));
        drinks.add(new Drink("沉船陳年果茶", "高級茶飲", 360, "以沉船寶箱中的古老茶方調製，味道濃厚。", "士氣和幸運感提升。", 2, 0, 1, new Color(155, 85, 50)));
        drinks.add(new Drink("人魚祝福露", "稀有飲品", 520, "傳說杯中會浮現像歌聲一樣的光。", "幸運感大幅提升。", 0, 0, 4, new Color(120, 230, 210)));
        drinks.add(new Drink("深潛員套餐", "套餐", 680, "一份熱湯、一杯特調和小點心，出任務前最穩。", "士氣、專注、幸運感都提升。", 2, 2, 2, new Color(240, 180, 85)));
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBackground(new Color(10, 18, 28));
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 3));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("TAVERN 酒館補給", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 36));
        titleLabel.setForeground(new Color(255, 220, 135));

        JLabel subtitleLabel = new JLabel("購買補給飲品，出任務前調整士氣、專注與幸運", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(190, 225, 230));

        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        root.add(titlePanel, BorderLayout.NORTH);

        root.add(createLeftPanel(), BorderLayout.WEST);
        root.add(createRightPanel(), BorderLayout.CENTER);

        add(root);
    }

    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout(0, 12));
        leftPanel.setOpaque(false);

        JPanel filterPanel = new JPanel(new GridLayout(3, 2, 10, 8));
        filterPanel.setOpaque(false);

        categoryBox = new JComboBox<>(new String[] {"全部", "招牌飲品", "果飲", "熱飲", "茶飲", "氣泡飲", "高級", "套餐"});
        categoryBox.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
        categoryBox.addActionListener(e -> {
            selectedIndex = 0;
            refreshAll();
        });

        sortBox = new JComboBox<>(new String[] {"預設順序", "價格由低到高", "價格由高到低", "效果由高到低", "持有數量由高到低"});
        sortBox.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
        sortBox.addActionListener(e -> {
            selectedIndex = 0;
            refreshAll();
        });

        searchField = new JTextField();
        searchField.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        searchField.setToolTipText("可輸入飲品名稱、分類或描述關鍵字");
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                selectedIndex = 0;
                refreshAll();
            }
        });

        filterPanel.add(createSmallLabel("分類"));
        filterPanel.add(categoryBox);
        filterPanel.add(createSmallLabel("排序"));
        filterPanel.add(sortBox);
        filterPanel.add(createSmallLabel("搜尋"));
        filterPanel.add(searchField);

        ownedOnlyBox = createCheckBox("只看已持有");
        ownedOnlyBox.addActionListener(e -> {
            selectedIndex = 0;
            refreshAll();
        });

        affordableOnlyBox = createCheckBox("只看買得起");
        affordableOnlyBox.addActionListener(e -> {
            selectedIndex = 0;
            refreshAll();
        });

        clearSearchButton = createButton("清除搜尋");
        clearSearchButton.setToolTipText("清除分類、排序、搜尋與篩選");
        clearSearchButton.addActionListener(e -> {
            searchField.setText("");
            categoryBox.setSelectedItem("全部");
            sortBox.setSelectedItem("預設順序");
            ownedOnlyBox.setSelected(false);
            affordableOnlyBox.setSelected(false);
            selectedIndex = 0;
            refreshAll();
        });

        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        quantitySpinner.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
        quantitySpinner.setToolTipText("用於購買數量與飲用數量");
        quantitySpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                refreshAll();
            }
        });

        JPanel extraFilterPanel = new JPanel(new GridLayout(2, 3, 12, 8));
        extraFilterPanel.setOpaque(false);
        extraFilterPanel.add(ownedOnlyBox);
        extraFilterPanel.add(affordableOnlyBox);
        extraFilterPanel.add(clearSearchButton);
        extraFilterPanel.add(createSmallLabel("數量"));
        extraFilterPanel.add(quantitySpinner);
        extraFilterPanel.add(new JLabel(""));

        JPanel topLeftPanel = new JPanel(new BorderLayout(0, 10));
        topLeftPanel.setOpaque(false);
        topLeftPanel.add(filterPanel, BorderLayout.CENTER);
        topLeftPanel.add(extraFilterPanel, BorderLayout.SOUTH);

        drinkListPanel = new JPanel();
        drinkListPanel.setLayout(new BoxLayout(drinkListPanel, BoxLayout.Y_AXIS));
        drinkListPanel.setBackground(new Color(25, 35, 45));

        JScrollPane scrollPane = new JScrollPane(drinkListPanel);
        scrollPane.setPreferredSize(new Dimension(590, 590));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(210, 145, 58), 3));
        scrollPane.getViewport().setBackground(new Color(25, 35, 45));

        leftPanel.add(topLeftPanel, BorderLayout.NORTH);
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        return leftPanel;
    }

    private JPanel createRightPanel() {
        JPanel detailPanel = new JPanel(new BorderLayout(12, 12));
        detailPanel.setBackground(new Color(20, 42, 54));
        detailPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 145, 58), 3),
            BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));

        moneyLabel = createInfoLabel(22, new Color(130, 235, 255));
        ownedLabel = createInfoLabel(18, new Color(255, 235, 170));
        buffLabel = createInfoLabel(17, new Color(170, 255, 190));
        recommendationLabel = createInfoLabel(16, new Color(255, 210, 120));

        JPanel topInfoPanel = new JPanel(new GridLayout(4, 1, 0, 8));
        topInfoPanel.setOpaque(false);
        topInfoPanel.add(moneyLabel);
        topInfoPanel.add(ownedLabel);
        topInfoPanel.add(buffLabel);
        topInfoPanel.add(recommendationLabel);

        detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        detailArea.setOpaque(false);
        detailArea.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 18));
        detailArea.setForeground(new Color(232, 245, 245));

        logArea = new JTextArea("酒館紀錄會顯示在這裡。");
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        logArea.setForeground(new Color(230, 240, 230));
        logArea.setBackground(new Color(10, 26, 34));
        logArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(90, 130, 140), 2),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, detailArea, logArea);
        splitPane.setResizeWeight(0.70);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setDividerSize(6);

        buyOneButton = createButton("購買 1 杯");
        buyFiveButton = createButton("購買 5 杯");
        buyQuantityButton = createButton("購買數量");
        drinkOneButton = createButton("飲用 1 杯");
        drinkQuantityButton = createButton("飲用數量");
        drinkAllButton = createButton("飲用全部");
        recommendButton = createButton("推薦補給");
        resetBuffButton = createButton("重置狀態");
        JButton closeButton = createButton("關閉酒館");

        buyOneButton.setToolTipText("購買目前選取的飲品 1 杯");
        buyFiveButton.setToolTipText("購買目前選取的飲品 5 杯");
        buyQuantityButton.setToolTipText("依左側數量欄購買");
        drinkOneButton.setToolTipText("飲用目前選取的飲品 1 杯");
        drinkQuantityButton.setToolTipText("依左側數量欄飲用");
        drinkAllButton.setToolTipText("一次飲用目前持有的全部同款飲品");
        recommendButton.setToolTipText("依目前最低狀態推薦補給");
        resetBuffButton.setToolTipText("只重置士氣、專注、幸運，不會刪除已購買飲品");

        buyOneButton.addActionListener(e -> buySelectedDrink(1));
        buyFiveButton.addActionListener(e -> buySelectedDrink(5));
        buyQuantityButton.addActionListener(e -> buySelectedDrink(getSelectedQuantity()));
        drinkOneButton.addActionListener(e -> drinkSelectedDrink(1));
        drinkQuantityButton.addActionListener(e -> drinkSelectedDrink(getSelectedQuantity()));
        drinkAllButton.addActionListener(e -> drinkSelectedDrink(getOwnedAmount(getSelectedDrink().name)));
        recommendButton.addActionListener(e -> selectRecommendedDrink());
        resetBuffButton.addActionListener(e -> resetBuffs());
        closeButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new GridLayout(3, 3, 12, 12));
        buttonPanel.setOpaque(false);
        buttonPanel.add(buyOneButton);
        buttonPanel.add(buyFiveButton);
        buttonPanel.add(buyQuantityButton);
        buttonPanel.add(drinkOneButton);
        buttonPanel.add(drinkQuantityButton);
        buttonPanel.add(drinkAllButton);
        buttonPanel.add(recommendButton);
        buttonPanel.add(resetBuffButton);
        buttonPanel.add(closeButton);

        detailPanel.add(topInfoPanel, BorderLayout.NORTH);
        detailPanel.add(splitPane, BorderLayout.CENTER);
        detailPanel.add(buttonPanel, BorderLayout.SOUTH);

        return detailPanel;
    }

    private JLabel createSmallLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Microsoft JhengHei", Font.BOLD, 17));
        label.setForeground(new Color(255, 235, 170));
        return label;
    }

    private JLabel createInfoLabel(int size, Color color) {
        JLabel label = new JLabel();
        label.setFont(new Font("Microsoft JhengHei", Font.BOLD, size));
        label.setForeground(color);
        return label;
    }

    private JCheckBox createCheckBox(String text) {
        JCheckBox box = new JCheckBox(text);
        box.setOpaque(false);
        box.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
        box.setForeground(new Color(255, 235, 170));
        return box;
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Microsoft JhengHei", Font.BOLD, 17));
        button.setFocusPainted(false);
        button.setBackground(new Color(18, 80, 105));
        button.setForeground(new Color(255, 235, 170));
        button.setBorder(BorderFactory.createLineBorder(new Color(210, 145, 58), 3));
        return button;
    }

    private void refreshAll() {
        List<Drink> shown = getFilteredDrinks();

        if (shown.isEmpty()) {
            selectedIndex = 0;
        } else if (selectedIndex >= shown.size()) {
            selectedIndex = shown.size() - 1;
        }

        refreshDrinkCards();
        updateDetail();
    }

    private List<Drink> getFilteredDrinks() {
        String category = categoryBox == null ? "全部" : (String) categoryBox.getSelectedItem();
        String keyword = searchField == null ? "" : searchField.getText().trim();
        String sort = sortBox == null ? "預設順序" : (String) sortBox.getSelectedItem();
        boolean ownedOnly = ownedOnlyBox != null && ownedOnlyBox.isSelected();
        boolean affordableOnly = affordableOnlyBox != null && affordableOnlyBox.isSelected();
        int currentMoney = getMoneySafely();

        List<Drink> shown = new ArrayList<>();

        for (Drink drink : drinks) {
            boolean categoryMatch;

            if (category == null || category.equals("全部")) {
                categoryMatch = true;
            } else if (category.equals("高級")) {
                categoryMatch = drink.type.contains("高級") || drink.type.contains("稀有");
            } else {
                categoryMatch = drink.type.contains(category);
            }

            boolean keywordMatch = keyword.isEmpty()
                || drink.name.contains(keyword)
                || drink.type.contains(keyword)
                || drink.description.contains(keyword);

            boolean ownedMatch = !ownedOnly || getOwnedAmount(drink.name) > 0;
            boolean affordableMatch = !affordableOnly || currentMoney >= drink.price;

            if (categoryMatch && keywordMatch && ownedMatch && affordableMatch) {
                shown.add(drink);
            }
        }

        if (sort != null) {
            if (sort.equals("價格由低到高")) {
                Collections.sort(shown, Comparator.comparingInt(d -> d.price));
            } else if (sort.equals("價格由高到低")) {
                Collections.sort(shown, (a, b) -> b.price - a.price);
            } else if (sort.equals("效果由高到低")) {
                Collections.sort(shown, (a, b) -> b.totalBuff() - a.totalBuff());
            } else if (sort.equals("持有數量由高到低")) {
                Collections.sort(shown, (a, b) -> getOwnedAmount(b.name) - getOwnedAmount(a.name));
            }
        }

        return shown;
    }

    private Drink getSelectedDrink() {
        List<Drink> shown = getFilteredDrinks();

        if (shown.isEmpty()) {
            return drinks.get(0);
        }

        return shown.get(selectedIndex);
    }

    private void refreshDrinkCards() {
        drinkListPanel.removeAll();

        List<Drink> shown = getFilteredDrinks();

        if (shown.isEmpty()) {
            JLabel emptyLabel = new JLabel("找不到符合條件的飲品", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 20));
            emptyLabel.setForeground(new Color(255, 235, 170));
            emptyLabel.setPreferredSize(new Dimension(540, 100));
            drinkListPanel.add(emptyLabel);
        }

        for (int i = 0; i < shown.size(); i++) {
            JPanel card = createDrinkCard(shown.get(i), i);
            drinkListPanel.add(card);
            drinkListPanel.add(Box.createVerticalStrut(10));
        }

        drinkListPanel.revalidate();
        drinkListPanel.repaint();
    }

    private JPanel createDrinkCard(Drink drink, int index) {
        boolean selected = index == selectedIndex;

        JPanel card = new JPanel(new BorderLayout(12, 6));
        card.setPreferredSize(new Dimension(545, 98));
        card.setMaximumSize(new Dimension(545, 98));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(selected ? new Color(255, 225, 120) : new Color(130, 95, 45), selected ? 4 : 2),
            BorderFactory.createEmptyBorder(9, 14, 9, 14)
        ));
        card.setBackground(selected ? new Color(38, 88, 104) : new Color(235, 220, 185));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        DrinkIcon icon = new DrinkIcon(drink.color, selected, drink.type, drink.name);
        icon.setPreferredSize(new Dimension(62, 74));

        JLabel nameLabel = new JLabel(drink.name);
        nameLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 19));
        nameLabel.setForeground(selected ? new Color(255, 240, 175) : new Color(45, 35, 25));

        JLabel infoLabel = new JLabel(drink.type + "｜價格 $" + drink.price + "｜持有 " + getOwnedAmount(drink.name));
        infoLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        infoLabel.setForeground(selected ? new Color(225, 245, 245) : new Color(75, 60, 45));

        JLabel effectLabel = new JLabel("效果：士氣 +" + drink.morale + "｜專注 +" + drink.focus + "｜幸運 +" + drink.luck + "｜總效果 " + drink.totalBuff());
        effectLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));
        effectLabel.setForeground(selected ? new Color(175, 255, 190) : new Color(55, 95, 60));

        JPanel textPanel = new JPanel(new GridLayout(3, 1));
        textPanel.setOpaque(false);
        textPanel.add(nameLabel);
        textPanel.add(infoLabel);
        textPanel.add(effectLabel);

        card.add(icon, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedIndex = index;
                refreshAll();
            }
        });

        return card;
    }

    private static class DrinkIcon extends JComponent {
        private Color color;
        private boolean selected;
        private String type;
        private String name;

        DrinkIcon(Color color, boolean selected, String type, String name) {
            this.color = color;
            this.selected = selected;
            this.type = type == null ? "" : type;
            this.name = name == null ? "" : name;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int cx = w / 2;

            g2.setColor(new Color(0, 0, 0, 70));
            g2.fillOval(cx - 18, h - 13, 36, 8);

            if (type.contains("熱") || type.contains("茶")) {
                drawCup(g2, cx, h);
            } else if (type.contains("氣泡") || type.contains("冰")) {
                drawTallGlass(g2, cx, h, true);
            } else if (type.contains("高級") || type.contains("稀有") || name.contains("人魚") || name.contains("船長")) {
                drawGoblet(g2, cx, h);
            } else {
                drawTallGlass(g2, cx, h, false);
            }

            if (selected) {
                g2.setColor(new Color(255, 236, 160, 90));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(3, 3, w - 7, h - 7, 12, 12);
            }

            g2.dispose();
        }

        private void drawTallGlass(Graphics2D g2, int cx, int h, boolean bubbles) {
            int glassTopY = 10;
            int glassBottomY = h - 20;
            int glassWTop = 26;
            int glassWBottom = 20;

            Polygon glass = new Polygon();
            glass.addPoint(cx - glassWTop / 2, glassTopY);
            glass.addPoint(cx + glassWTop / 2, glassTopY);
            glass.addPoint(cx + glassWBottom / 2, glassBottomY);
            glass.addPoint(cx - glassWBottom / 2, glassBottomY);

            GradientPaint drinkPaint = new GradientPaint(cx, glassTopY, lighten(color, 55), cx, glassBottomY, darken(color, 25));
            g2.setPaint(drinkPaint);

            Polygon liquid = new Polygon();
            liquid.addPoint(cx - 11, glassTopY + 16);
            liquid.addPoint(cx + 11, glassTopY + 16);
            liquid.addPoint(cx + 8, glassBottomY - 3);
            liquid.addPoint(cx - 8, glassBottomY - 3);
            g2.fillPolygon(liquid);

            g2.setColor(new Color(255, 255, 255, 120));
            g2.fillRoundRect(cx - 8, glassTopY + 20, 4, 26, 4, 4);

            g2.setColor(new Color(235, 245, 250, 105));
            g2.fillPolygon(glass);

            g2.setColor(new Color(70, 60, 55, 190));
            g2.setStroke(new BasicStroke(2f));
            g2.drawPolygon(glass);

            g2.setColor(new Color(255, 247, 228, 220));
            for (int i = -2; i <= 2; i++) {
                g2.fillOval(cx - 10 + i * 5, glassTopY + 8 + Math.abs(i % 2), 8, 8);
            }

            g2.setColor(new Color(220, 70, 70, 210));
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(cx + 5, glassTopY - 6, cx + 10, glassTopY + 12);

            if (bubbles) {
                g2.setColor(new Color(255, 255, 255, 150));
                g2.drawOval(cx - 4, glassTopY + 28, 4, 4);
                g2.drawOval(cx + 2, glassTopY + 34, 3, 3);
                g2.drawOval(cx - 2, glassTopY + 40, 5, 5);
            }
        }

        private void drawCup(Graphics2D g2, int cx, int h) {
            int y = 20;
            int cupW = 28;
            int cupH = 22;

            GradientPaint drinkPaint = new GradientPaint(cx, y + 8, lighten(color, 35), cx, y + cupH + 6, darken(color, 35));
            g2.setPaint(drinkPaint);
            g2.fillRoundRect(cx - cupW / 2, y + 10, cupW, cupH, 8, 8);

            g2.setColor(new Color(255, 245, 230, 220));
            g2.fillRoundRect(cx - cupW / 2, y + 4, cupW, 10, 8, 8);

            g2.setColor(new Color(90, 75, 60, 190));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(cx - cupW / 2, y + 10, cupW, cupH, 8, 8);
            g2.drawOval(cx + cupW / 2 - 2, y + 14, 9, 12);

            g2.setColor(new Color(220, 225, 228, 220));
            g2.fillRoundRect(cx - 18, y + cupH + 26, 36, 5, 6, 6);

            g2.setColor(new Color(255, 255, 255, 120));
            g2.drawArc(cx - 12, y - 2, 8, 12, 0, 180);
            g2.drawArc(cx - 2, y - 6, 8, 14, 0, 180);
            g2.drawArc(cx + 8, y - 2, 8, 12, 0, 180);
        }

        private void drawGoblet(Graphics2D g2, int cx, int h) {
            int bowlY = 10;
            int bowlW = 28;
            int bowlH = 24;

            GradientPaint drinkPaint = new GradientPaint(cx, bowlY + 6, lighten(color, 45), cx, bowlY + bowlH + 4, darken(color, 30));
            g2.setPaint(drinkPaint);
            g2.fillOval(cx - bowlW / 2, bowlY + 8, bowlW, bowlH);

            g2.setColor(new Color(255, 245, 230, 80));
            g2.fillOval(cx - bowlW / 2, bowlY + 8, bowlW, bowlH);

            g2.setColor(new Color(90, 70, 55, 190));
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(cx - bowlW / 2, bowlY + 8, bowlW, bowlH);
            g2.drawRoundRect(cx - 15, bowlY + 5, 30, 6, 4, 4);
            g2.drawLine(cx, bowlY + bowlH + 8, cx, bowlY + bowlH + 24);
            g2.drawRoundRect(cx - 10, bowlY + bowlH + 24, 20, 4, 4, 4);

            g2.setColor(new Color(255, 255, 255, 110));
            g2.fillOval(cx - 8, bowlY + 14, 6, 10);

            g2.setColor(new Color(255, 240, 150, 170));
            g2.fillOval(cx - 14, bowlY + 2, 4, 4);
            g2.fillOval(cx + 12, bowlY + 8, 3, 3);
            g2.fillOval(cx + 9, bowlY + 24, 4, 4);
        }

        private Color lighten(Color base, int amount) {
            return new Color(Math.min(255, base.getRed() + amount), Math.min(255, base.getGreen() + amount), Math.min(255, base.getBlue() + amount));
        }

        private Color darken(Color base, int amount) {
            return new Color(Math.max(0, base.getRed() - amount), Math.max(0, base.getGreen() - amount), Math.max(0, base.getBlue() - amount));
        }
    }

    private void updateDetail() {
        List<Drink> shown = getFilteredDrinks();
        int money = getMoneySafely();

        moneyLabel.setText("目前金幣：$" + money);
        buffLabel.setText("目前狀態：士氣 +" + moraleBuff + "｜專注 +" + focusBuff + "｜幸運 +" + luckBuff + "｜已飲用 " + totalConsumed + " 次");
        recommendationLabel.setText("推薦：" + getRecommendationText());

        if (shown.isEmpty()) {
            ownedLabel.setText("目前持有：無");
            detailArea.setText("找不到符合條件的飲品。\n\n可以按「清除搜尋」回到完整清單。");
            setDrinkButtonsEnabled(false);
            return;
        }

        Drink drink = getSelectedDrink();
        int owned = getOwnedAmount(drink.name);
        int qty = getSelectedQuantity();

        ownedLabel.setText("目前持有：" + drink.name + " × " + owned);

        detailArea.setText(
            "【" + drink.name + "】\n" +
            "分類：" + drink.type + "\n" +
            "價格：$" + drink.price + "\n\n" +
            "介紹：\n" + drink.description + "\n\n" +
            "飲用效果：\n" + drink.effectText + "\n\n" +
            "數值效果：\n" +
            "士氣 +" + drink.morale + "，專注 +" + drink.focus + "，幸運 +" + drink.luck + "，總效果 " + drink.totalBuff() + "\n\n" +
            "操作說明：\n" +
            "購買會扣金幣並加入持有數量；飲用會消耗持有數量並累加狀態。\n" +
            getAffordHint(drink, money)
        );

        buyOneButton.setEnabled(money >= drink.price);
        buyFiveButton.setEnabled(money >= drink.price * 5);
        buyQuantityButton.setEnabled(money >= drink.price * qty);
        drinkOneButton.setEnabled(owned > 0);
        drinkQuantityButton.setEnabled(owned >= qty);
        drinkAllButton.setEnabled(owned > 0);
    }

    private void setDrinkButtonsEnabled(boolean enabled) {
        buyOneButton.setEnabled(enabled);
        buyFiveButton.setEnabled(enabled);
        buyQuantityButton.setEnabled(enabled);
        drinkOneButton.setEnabled(enabled);
        drinkQuantityButton.setEnabled(enabled);
        drinkAllButton.setEnabled(enabled);
    }

    private String getAffordHint(Drink drink, int money) {
        if (money >= drink.price) {
            return "目前金幣足夠購買。";
        }

        return "目前還差 $" + (drink.price - money) + " 才能購買 1 杯。";
    }

    private String getRecommendationText() {
        Drink best = getRecommendedDrink();

        if (best == null) {
            return "目前沒有推薦";
        }

        return best.name + "（補目前較低的狀態）";
    }

    private Drink getRecommendedDrink() {
        Drink best = null;
        int bestScore = Integer.MIN_VALUE;

        for (Drink drink : drinks) {
            int score = 0;

            if (moraleBuff <= focusBuff && moraleBuff <= luckBuff) {
                score += drink.morale * 5;
            }

            if (focusBuff <= moraleBuff && focusBuff <= luckBuff) {
                score += drink.focus * 5;
            }

            if (luckBuff <= moraleBuff && luckBuff <= focusBuff) {
                score += drink.luck * 5;
            }

            score += drink.totalBuff() * 2;
            score -= drink.price / 200;

            if (score > bestScore) {
                bestScore = score;
                best = drink;
            }
        }

        return best;
    }

    private void selectRecommendedDrink() {
        Drink recommended = getRecommendedDrink();

        if (recommended == null) {
            return;
        }

        categoryBox.setSelectedItem("全部");
        sortBox.setSelectedItem("預設順序");
        ownedOnlyBox.setSelected(false);
        affordableOnlyBox.setSelected(false);
        searchField.setText("");

        List<Drink> shown = getFilteredDrinks();

        for (int i = 0; i < shown.size(); i++) {
            if (shown.get(i).name.equals(recommended.name)) {
                selectedIndex = i;
                break;
            }
        }

        appendLog("系統推薦：" + recommended.name);
        refreshAll();
    }

    private int getSelectedQuantity() {
        if (quantitySpinner == null) {
            return 1;
        }

        Object value = quantitySpinner.getValue();

        if (value instanceof Number) {
            int amount = ((Number) value).intValue();

            if (amount < 1) {
                return 1;
            }

            return amount;
        }

        return 1;
    }

    private void resetBuffs() {
        int answer = JOptionPane.showConfirmDialog(
            this,
            "確定要清除目前士氣、專注、幸運狀態嗎？\n這不會影響已購買的飲品。",
            "重置狀態",
            JOptionPane.YES_NO_OPTION
        );

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        moraleBuff = 0;
        focusBuff = 0;
        luckBuff = 0;
        totalConsumed = 0;
        appendLog("已重置酒館狀態。");
        refreshAll();
    }

    private void buySelectedDrink(int amount) {
        Drink drink = getSelectedDrink();
        int totalPrice = drink.price * amount;

        if (getMoneySafely() < totalPrice) {
            JOptionPane.showMessageDialog(this, "金幣不足，無法購買「" + drink.name + "」× " + amount + "。\n需要 $" + totalPrice + "。", "金幣不足", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean paid = spendMoneySafely(totalPrice);

        if (!paid) {
            JOptionPane.showMessageDialog(this, "扣款失敗，請檢查 InventoryManager 是否有 addMoney(int) 或 spendMoney(int)。", "購買失敗", JOptionPane.WARNING_MESSAGE);
            return;
        }

        addOwnedDrink(drink, amount);
        appendLog("購買：" + drink.name + " × " + amount + "，花費 $" + totalPrice);

        JOptionPane.showMessageDialog(this, "購買成功：\n" + drink.name + " × " + amount + "\n花費 $" + totalPrice, "購買成功", JOptionPane.INFORMATION_MESSAGE);
        refreshAll();
    }

    private void drinkSelectedDrink(int amount) {
        Drink drink = getSelectedDrink();
        int owned = getOwnedAmount(drink.name);

        if (owned <= 0) {
            JOptionPane.showMessageDialog(this, "你沒有「" + drink.name + "」，請先購買。", "無法飲用", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (amount <= 0) {
            return;
        }

        if (amount > owned) {
            amount = owned;
        }

        removeOwnedDrink(drink.name, amount);

        moraleBuff += drink.morale * amount;
        focusBuff += drink.focus * amount;
        luckBuff += drink.luck * amount;
        totalConsumed += amount;

        appendLog("飲用：" + drink.name + " × " + amount + "｜士氣 +" + (drink.morale * amount) + " 專注 +" + (drink.focus * amount) + " 幸運 +" + (drink.luck * amount));

        JOptionPane.showMessageDialog(
            this,
            "你飲用了「" + drink.name + "」× " + amount + "。\n\n效果：\n" + drink.effectText +
            "\n\n目前狀態：\n士氣 +" + moraleBuff + "｜專注 +" + focusBuff + "｜幸運 +" + luckBuff,
            "飲用完成",
            JOptionPane.INFORMATION_MESSAGE
        );

        refreshAll();
    }

    private void appendLog(String text) {
        if (logArea == null) {
            return;
        }

        String current = logArea.getText();

        if (current.equals("酒館紀錄會顯示在這裡。")) {
            logArea.setText(text);
        } else {
            logArea.setText(text + "\n" + current);
        }
    }

    private static int getOwnedAmount(String name) {
        for (OwnedDrink owned : ownedDrinks) {
            if (owned.drink.name.equals(name)) {
                return owned.amount;
            }
        }

        return 0;
    }

    private static void addOwnedDrink(Drink drink, int amount) {
        for (OwnedDrink owned : ownedDrinks) {
            if (owned.drink.name.equals(drink.name)) {
                owned.amount += amount;
                return;
            }
        }

        ownedDrinks.add(new OwnedDrink(drink, amount));
    }

    private static void removeOwnedDrink(String name, int amount) {
        for (int i = 0; i < ownedDrinks.size(); i++) {
            OwnedDrink owned = ownedDrinks.get(i);

            if (owned.drink.name.equals(name)) {
                owned.amount -= amount;

                if (owned.amount <= 0) {
                    ownedDrinks.remove(i);
                }

                return;
            }
        }
    }

    private int getMoneySafely() {
        try {
            Method method = InventoryManager.class.getMethod("getMoney");
            Object value = method.invoke(null);

            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
        } catch (Exception e) {
        }

        try {
            Method method = InventoryManager.class.getMethod("getTotalPrice");
            Object value = method.invoke(null);

            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
        } catch (Exception e) {
        }

        return 0;
    }

    private boolean spendMoneySafely(int amount) {
        try {
            Method method = InventoryManager.class.getMethod("spendMoney", int.class);
            Object value = method.invoke(null, amount);

            if (value instanceof Boolean) {
                return (Boolean) value;
            }

            return true;
        } catch (Exception e) {
        }

        try {
            Method method = InventoryManager.class.getMethod("addMoney", int.class);
            method.invoke(null, -amount);
            return true;
        } catch (Exception e) {
        }

        return false;
    }

    public static int getMoraleBuff() {
        return moraleBuff;
    }

    public static int getFocusBuff() {
        return focusBuff;
    }

    public static int getLuckBuff() {
        return luckBuff;
    }

    public static int getTotalConsumed() {
        return totalConsumed;
    }
}
