import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MissionBoardView extends JFrame {

    public static class Mission {
        public String id;
        public String title;
        public String description;
        public int reward;
        public int targetAmount;
        public int currentAmount;
        public boolean completed;
        public boolean rewardGiven;

        public Mission(String title, String description, int reward, int targetAmount) {
            this(inferMissionId(title), title, description, reward, targetAmount);
        }

        public Mission(String id, String title, String description, int reward, int targetAmount) {
            this.id = normalizeMissionId(id, title);
            this.title = title;
            this.description = description;
            this.reward = reward;
            this.targetAmount = targetAmount;
            this.currentAmount = 0;
            this.completed = false;
            this.rewardGiven = false;
        }
    }

    private static final List<Mission> acceptedMissions = new ArrayList<>();
    private static Mission acceptedMission = null;

    private static int deepestDepthReached = 0;
    private static int defeatedGreenEelCount = 0;
    private static int safeReturnCount = 0;
    private static int exploredDeepBaseCount = 0;

    private static String lastCompleteMessage = "";
    private static long lastCompleteMessageTime = 0;

    private List<Mission> missionList = new ArrayList<>();

    private JPanel missionListPanel;
    private JComboBox<String> statusFilterBox;
    private JLabel detailTitleLabel;
    private JTextArea detailDescriptionArea;
    private JLabel detailRewardLabel;
    private JLabel detailProgressLabel;
    private JProgressBar progressBar;
    private JButton acceptButton;
    private JButton refreshButton;
    private JButton acceptNextButton;

    private int selectedMissionIndex = 0;

    public MissionBoardView() {
        setTitle("Mission Board - 任務公告板");
        setSize(1100, 740);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getRootPane().registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        missionList.addAll(createDefaultMissionList());
        buildUI();

        setVisible(true);
    }

    public static List<Mission> createDefaultMissionList() {
        List<Mission> list = new ArrayList<>();

        list.add(new Mission("CATCH_SARDINE", "捕捉沙丁魚", "前往淺海區捕捉 3 隻沙丁魚，作為新手潛水員的基礎訓練。", 800, 3));
        list.add(new Mission("REACH_DEPTH_500", "下潛到 500m", "進入海洋地圖並成功下潛到 500m 深度，確認潛水衣運作狀態。", 1200, 500));
        list.add(new Mission("CATCH_CLOWNFISH", "帶回小丑魚", "捕捉並帶回 1 隻小丑魚，交給水族館進行觀察。", 1500, 1));
        list.add(new Mission("DEFEAT_GREEN_EEL", "擊倒綠鰻魚", "深海區出現具有攻擊性的綠鰻魚，請擊倒 1 隻並安全回收。", 2500, 1));
        list.add(new Mission("COLLECT_5_SPECIES", "收集五種生物", "完成一次探索，捕捉並帶回 5 種不同海洋生物。", 3000, 5));
        list.add(new Mission("EXPLORE_DEEP_BASE", "探索深海基地", "下潛至深海基地附近，完成區域偵查後安全返回。", 3500, 1));
        list.add(new Mission("RETURN_VALUE_3000", "高價魚獲回收", "單次下潛帶回總價值 3000 以上的魚獲。", 2200, 3000));
        list.add(new Mission("SAFE_RETURN", "安全返航訓練", "完成一次下潛任務，並成功回到陸地基地。", 1000, 1));

        return list;
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBackground(new Color(14, 24, 34));
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 3));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("MISSION BOARD 任務公告板", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 34));
        titleLabel.setForeground(new Color(255, 220, 130));

        JLabel subtitleLabel = new JLabel("任務照順序接取，完成後自動發放金幣獎勵", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(190, 225, 230));

        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        root.add(titlePanel, BorderLayout.NORTH);

        JPanel leftPanel = new JPanel(new BorderLayout(0, 12));
        leftPanel.setOpaque(false);

        JPanel filterPanel = new JPanel(new BorderLayout(10, 0));
        filterPanel.setOpaque(false);

        JLabel filterLabel = new JLabel("顯示");
        filterLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
        filterLabel.setForeground(new Color(255, 235, 170));

        statusFilterBox = new JComboBox<>(new String[] {
            "全部任務",
            "可接取",
            "進行中",
            "已完成",
            "未解鎖"
        });
        statusFilterBox.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
        statusFilterBox.addActionListener(e -> {
            refreshMissionCards();
            updateDetailPanel();
        });

        filterPanel.add(filterLabel, BorderLayout.WEST);
        filterPanel.add(statusFilterBox, BorderLayout.CENTER);

        missionListPanel = new JPanel();
        missionListPanel.setLayout(new BoxLayout(missionListPanel, BoxLayout.Y_AXIS));
        missionListPanel.setBackground(new Color(25, 38, 50));

        JScrollPane scrollPane = new JScrollPane(missionListPanel);
        scrollPane.setPreferredSize(new Dimension(540, 580));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(210, 145, 58), 3));
        scrollPane.getViewport().setBackground(new Color(25, 38, 50));

        leftPanel.add(filterPanel, BorderLayout.NORTH);
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        root.add(leftPanel, BorderLayout.WEST);

        JPanel detailPanel = new JPanel(new BorderLayout(12, 12));
        detailPanel.setBackground(new Color(22, 42, 56));
        detailPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 145, 58), 3),
            BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));

        detailTitleLabel = new JLabel();
        detailTitleLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 27));
        detailTitleLabel.setForeground(new Color(255, 225, 140));

        detailDescriptionArea = new JTextArea();
        detailDescriptionArea.setEditable(false);
        detailDescriptionArea.setLineWrap(true);
        detailDescriptionArea.setWrapStyleWord(true);
        detailDescriptionArea.setOpaque(false);
        detailDescriptionArea.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 18));
        detailDescriptionArea.setForeground(new Color(225, 240, 240));

        detailRewardLabel = new JLabel();
        detailRewardLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 20));
        detailRewardLabel.setForeground(new Color(110, 230, 255));

        detailProgressLabel = new JLabel();
        detailProgressLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
        detailProgressLabel.setForeground(new Color(200, 235, 235));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("Microsoft JhengHei", Font.BOLD, 15));
        progressBar.setForeground(new Color(70, 190, 120));
        progressBar.setBackground(new Color(10, 24, 32));

        JPanel detailTextPanel = new JPanel(new GridLayout(3, 1, 0, 10));
        detailTextPanel.setOpaque(false);
        detailTextPanel.add(detailRewardLabel);
        detailTextPanel.add(detailProgressLabel);
        detailTextPanel.add(progressBar);

        acceptButton = createButton("接取任務");
        acceptNextButton = createButton("接下一個可接任務");
        refreshButton = createButton("刷新進度");
        JButton closeButton = createButton("關閉");

        acceptButton.setToolTipText("接取目前選取的任務");
        acceptNextButton.setToolTipText("自動接取清單中下一個可接任務");
        refreshButton.setToolTipText("重新掃描背包、儲藏箱和海洋狀態");

        acceptButton.addActionListener(e -> acceptSelectedMission());
        acceptNextButton.addActionListener(e -> acceptNextAvailableMission());
        refreshButton.addActionListener(e -> {
            autoCheckMissionProgress();
            refreshMissionCards();
            updateDetailPanel();
        });
        closeButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 12, 12));
        buttonPanel.setOpaque(false);
        buttonPanel.add(acceptButton);
        buttonPanel.add(acceptNextButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);

        detailPanel.add(detailTitleLabel, BorderLayout.NORTH);
        detailPanel.add(detailDescriptionArea, BorderLayout.CENTER);
        detailPanel.add(detailTextPanel, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel(new BorderLayout(0, 14));
        rightPanel.setOpaque(false);
        rightPanel.add(detailPanel, BorderLayout.CENTER);
        rightPanel.add(buttonPanel, BorderLayout.SOUTH);

        root.add(rightPanel, BorderLayout.CENTER);

        add(root);

        autoCheckMissionProgress();
        refreshMissionCards();
        updateDetailPanel();
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Microsoft JhengHei", Font.BOLD, 19));
        button.setFocusPainted(false);
        button.setBackground(new Color(18, 80, 105));
        button.setForeground(new Color(255, 235, 170));
        button.setBorder(BorderFactory.createLineBorder(new Color(210, 145, 58), 3));
        return button;
    }

    private void refreshMissionCards() {
        ensureSelectedMissionVisible();
        missionListPanel.removeAll();

        boolean hasAny = false;

        for (int i = 0; i < missionList.size(); i++) {
            if (!shouldShowMission(i)) {
                continue;
            }

            hasAny = true;
            missionListPanel.add(createMissionCard(missionList.get(i), i));
            missionListPanel.add(Box.createVerticalStrut(12));
        }

        if (!hasAny) {
            JLabel emptyLabel = new JLabel("目前沒有符合篩選條件的任務", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 20));
            emptyLabel.setForeground(new Color(255, 235, 170));
            emptyLabel.setPreferredSize(new Dimension(500, 120));
            missionListPanel.add(emptyLabel);
        }

        missionListPanel.revalidate();
        missionListPanel.repaint();
    }

    private void ensureSelectedMissionVisible() {
        if (missionList.isEmpty()) {
            selectedMissionIndex = 0;
            return;
        }

        if (selectedMissionIndex >= 0 && selectedMissionIndex < missionList.size() && shouldShowMission(selectedMissionIndex)) {
            return;
        }

        for (int i = 0; i < missionList.size(); i++) {
            if (shouldShowMission(i)) {
                selectedMissionIndex = i;
                return;
            }
        }

        selectedMissionIndex = 0;
    }

    private boolean shouldShowMission(int index) {
        String filter = statusFilterBox == null ? "全部任務" : (String) statusFilterBox.getSelectedItem();

        if (filter == null || filter.equals("全部任務")) {
            return true;
        }

        Mission baseMission = missionList.get(index);
        Mission acceptedVersion = findAcceptedMissionByTitle(baseMission.title);
        Mission mission = acceptedVersion == null ? baseMission : acceptedVersion;

        if (filter.equals("可接取")) {
            return acceptedVersion == null && canAcceptMission(index);
        }

        if (filter.equals("進行中")) {
            return acceptedVersion != null && !mission.completed;
        }

        if (filter.equals("已完成")) {
            return acceptedVersion != null && mission.completed;
        }

        if (filter.equals("未解鎖")) {
            return acceptedVersion == null && !canAcceptMission(index);
        }

        return true;
    }

    private JPanel createMissionCard(Mission baseMission, int index) {
        Mission acceptedVersion = findAcceptedMissionByTitle(baseMission.title);
        Mission mission = acceptedVersion == null ? baseMission : acceptedVersion;

        boolean selected = index == selectedMissionIndex;
        boolean accepted = acceptedVersion != null;

        JPanel card = new JPanel(new BorderLayout(10, 6));
        card.setPreferredSize(new Dimension(500, 105));
        card.setMaximumSize(new Dimension(500, 105));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(selected ? new Color(255, 225, 120) : new Color(130, 95, 45), selected ? 4 : 2),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        card.setBackground(selected ? new Color(35, 90, 110) : new Color(235, 220, 185));

        JLabel title = new JLabel(getMissionNumber(index) + ". " + mission.title);
        title.setFont(new Font("Microsoft JhengHei", Font.BOLD, 19));
        title.setForeground(selected ? new Color(255, 235, 170) : new Color(45, 35, 25));

        JLabel desc = new JLabel("獎勵 $" + mission.reward + "｜進度 " + mission.currentAmount + " / " + mission.targetAmount);
        desc.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        desc.setForeground(selected ? new Color(220, 240, 245) : new Color(70, 60, 45));

        int percent = getPercent(mission);
        JProgressBar smallBar = new JProgressBar(0, 100);
        smallBar.setValue(percent);
        smallBar.setStringPainted(true);
        smallBar.setString(percent + "%");
        smallBar.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
        smallBar.setForeground(new Color(70, 180, 120));
        smallBar.setBackground(selected ? new Color(15, 45, 60) : new Color(210, 195, 160));

        JLabel status = new JLabel();
        status.setFont(new Font("Microsoft JhengHei", Font.BOLD, 15));

        if (accepted && mission.completed) {
            status.setText("已完成");
            status.setForeground(new Color(90, 230, 120));
        } else if (accepted) {
            status.setText("進行中");
            status.setForeground(new Color(90, 210, 255));
        } else if (!canAcceptMission(index)) {
            status.setText("未解鎖");
            status.setForeground(selected ? new Color(255, 180, 120) : new Color(130, 80, 40));
        } else {
            status.setText("可接取");
            status.setForeground(selected ? new Color(255, 225, 120) : new Color(120, 80, 35));
        }

        JPanel textPanel = new JPanel(new GridLayout(3, 1, 0, 3));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(desc);
        textPanel.add(smallBar);

        card.add(textPanel, BorderLayout.CENTER);
        card.add(status, BorderLayout.EAST);

        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedMissionIndex = index;
                autoCheckMissionProgress();
                updateDetailPanel();
                refreshMissionCards();
            }
        });

        return card;
    }

    private String getMissionNumber(int index) {
        return String.format("%02d", index + 1);
    }

    private int getPercent(Mission mission) {
        if (mission.targetAmount <= 0) {
            return 0;
        }

        int percent = (int) Math.round(mission.currentAmount * 100.0 / mission.targetAmount);

        if (percent < 0) {
            return 0;
        }

        if (percent > 100) {
            return 100;
        }

        return percent;
    }

    private void updateDetailPanel() {
        ensureSelectedMissionVisible();

        if (missionList.isEmpty() || !shouldShowMission(selectedMissionIndex)) {
            detailTitleLabel.setText("沒有符合條件的任務");
            detailDescriptionArea.setText("目前篩選條件下沒有任務可以顯示。");
            detailRewardLabel.setText("任務獎勵：--");
            detailProgressLabel.setText("目前進度：--");
            progressBar.setValue(0);
            progressBar.setString("0%");
            acceptButton.setEnabled(false);
            return;
        }

        Mission baseMission = missionList.get(selectedMissionIndex);
        Mission acceptedVersion = findAcceptedMissionByTitle(baseMission.title);
        Mission mission = acceptedVersion == null ? baseMission : acceptedVersion;

        detailTitleLabel.setText(getMissionNumber(selectedMissionIndex) + ". " + mission.title);
        detailDescriptionArea.setText(
            mission.description + "\n\n" +
            "狀態：" + getMissionStatusText(mission, selectedMissionIndex, acceptedVersion != null) + "\n\n" +
            "說明：任務進度會自動從背包、儲藏箱和海洋場景狀態更新。"
        );

        detailRewardLabel.setText("任務獎勵：$" + mission.reward);
        detailProgressLabel.setText("目前進度：" + mission.currentAmount + " / " + mission.targetAmount);
        progressBar.setValue(getPercent(mission));
        progressBar.setString(getPercent(mission) + "%");

        if (acceptedVersion != null && acceptedVersion.completed) {
            acceptButton.setText("已完成");
            acceptButton.setEnabled(false);
        } else if (acceptedVersion != null) {
            acceptButton.setText("進行中");
            acceptButton.setEnabled(false);
        } else if (!canAcceptMission(selectedMissionIndex)) {
            acceptButton.setText("請先接前面的任務");
            acceptButton.setEnabled(false);
        } else {
            acceptButton.setText("接取任務");
            acceptButton.setEnabled(true);
        }
    }

    private String getMissionStatusText(Mission mission, int index, boolean accepted) {
        if (accepted && mission.completed) {
            return "已完成，獎勵已發放";
        }

        if (accepted) {
            return "進行中";
        }

        if (!canAcceptMission(index)) {
            return "尚未解鎖，請先接取前一個任務";
        }

        return "可接取";
    }

    private boolean canAcceptMission(int index) {
        if (index == 0) {
            return true;
        }

        return isMissionAccepted(missionList.get(index - 1).title);
    }

    private void acceptNextAvailableMission() {
        for (int i = 0; i < missionList.size(); i++) {
            Mission mission = missionList.get(i);

            if (findAcceptedMissionByTitle(mission.title) == null && canAcceptMission(i)) {
                selectedMissionIndex = i;
                acceptSelectedMission();
                return;
            }
        }

        JOptionPane.showMessageDialog(
            this,
            "目前沒有可以接取的新任務。",
            "沒有新任務",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void acceptSelectedMission() {
        Mission selectedMission = missionList.get(selectedMissionIndex);

        if (isMissionAccepted(selectedMission.title)) {
            JOptionPane.showMessageDialog(this, "你已經接取過這個任務了。", "任務已存在", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (!canAcceptMission(selectedMissionIndex)) {
            JOptionPane.showMessageDialog(this, "任務需要照順序接取，請先接前一個任務。", "無法接取", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Mission accepted = new Mission(selectedMission.id, selectedMission.title, selectedMission.description, selectedMission.reward, selectedMission.targetAmount);
        acceptedMissions.add(accepted);
        acceptedMission = accepted;

        autoCheckMissionProgress();

        JOptionPane.showMessageDialog(this, "已接取任務：\n" + accepted.title + "\n\n" + accepted.description, "任務接取成功", JOptionPane.INFORMATION_MESSAGE);

        refreshMissionCards();
        updateDetailPanel();
    }

    public static List<Mission> getAcceptedMissions() {
        autoCheckMissionProgress();
        return acceptedMissions;
    }

    public static Mission getAcceptedMission() {
        autoCheckMissionProgress();

        if (acceptedMission != null) {
            return acceptedMission;
        }

        if (!acceptedMissions.isEmpty()) {
            return acceptedMissions.get(acceptedMissions.size() - 1);
        }

        return null;
    }

    public static String getAcceptedMissionTitle() {
        Mission current = getAcceptedMission();
        return current == null ? "無" : current.title;
    }

    public static boolean isMissionAccepted(String title) {
        return findAcceptedMissionByTitle(title) != null;
    }

    public static boolean isMissionIdAccepted(String id) {
        return findAcceptedMissionById(id) != null;
    }

    public static void acceptMissionFromOutside(Mission mission) {
        if (mission == null) {
            return;
        }

        mission.id = normalizeMissionId(mission.id, mission.title);

        if (isMissionAccepted(mission.title)) {
            return;
        }

        acceptedMissions.add(mission);
        acceptedMission = mission;
        autoCheckMissionProgress();
    }

    public static void updateMissionProgress(String keyword, int amount) {
        if (keyword == null) {
            return;
        }

        for (Mission mission : acceptedMissions) {
            if (mission.completed) {
                continue;
            }

            boolean matched =
                (mission.id != null && mission.id.contains(keyword))
                || (mission.title != null && mission.title.contains(keyword))
                || (mission.description != null && mission.description.contains(keyword));

            if (!matched) {
                continue;
            }

            mission.currentAmount += amount;
            limitMissionProgress(mission);

            if (mission.currentAmount >= mission.targetAmount) {
                completeMission(mission);
            }
        }
    }

    public static void updateMissionProgressById(String id, int amount) {
        Mission mission = findAcceptedMissionById(id);

        if (mission == null || mission.completed) {
            return;
        }

        mission.currentAmount += amount;
        limitMissionProgress(mission);

        if (mission.currentAmount >= mission.targetAmount) {
            completeMission(mission);
        }
    }

    public static void registerCatchFish(Fish fish) {
        autoCheckMissionProgress();
    }

    public static void registerReachDepth(int depth) {
        if (depth > deepestDepthReached) {
            deepestDepthReached = depth;
        }

        if (depth >= 1200) {
            exploredDeepBaseCount = Math.max(exploredDeepBaseCount, 1);
        }

        autoCheckMissionProgress();
    }

    public static void registerDefeatEnemy(String enemyName) {
        if (enemyName == null) {
            return;
        }

        if (enemyName.contains("綠") || enemyName.contains("青魚") || enemyName.contains("鰻") || enemyName.toLowerCase().contains("eel")) {
            defeatedGreenEelCount++;
        }

        autoCheckMissionProgress();
    }

    public static void registerSafeReturn() {
        safeReturnCount++;
        autoCheckMissionProgress();
    }

    public static void registerExploreDeepBase() {
        exploredDeepBaseCount = Math.max(exploredDeepBaseCount, 1);
        autoCheckMissionProgress();
    }

    public static String getLastCompleteMessage() {
        if (System.currentTimeMillis() - lastCompleteMessageTime > 5000) {
            return "";
        }

        return lastCompleteMessage;
    }

    public static void autoCheckMissionProgress() {
        if (acceptedMissions.isEmpty()) {
            return;
        }

        scanOceanWorldRuntimeState();

        List<Fish> allFish = getAllPlayerFishSafely();

        int sardineCount = countFishByName(allFish, "沙丁");
        int clownCount = countFishByName(allFish, "小丑");
        int greenEelLikeCount = countFishByName(allFish, "綠") + countFishByName(allFish, "青魚") + countFishByName(allFish, "鰻");
        int rareCount = countRareFish(allFish, 3);
        int speciesCount = countSpecies(allFish);
        int totalFishValue = totalFishValue(allFish);

        if (getStorageFishCountSafely() > 0) {
            safeReturnCount = Math.max(safeReturnCount, 1);
        }

        for (Mission mission : acceptedMissions) {
            if (mission.completed) {
                continue;
            }

            String id = normalizeMissionId(mission.id, mission.title);

            if (id.equals("CATCH_SARDINE")) {
                mission.currentAmount = Math.max(mission.currentAmount, sardineCount);
            } else if (id.equals("REACH_DEPTH_500")) {
                mission.currentAmount = Math.max(mission.currentAmount, deepestDepthReached);
            } else if (id.equals("CATCH_CLOWNFISH")) {
                mission.currentAmount = Math.max(mission.currentAmount, clownCount);
            } else if (id.equals("DEFEAT_GREEN_EEL")) {
                mission.currentAmount = Math.max(mission.currentAmount, Math.max(defeatedGreenEelCount, greenEelLikeCount));
            } else if (id.equals("COLLECT_5_SPECIES") || id.equals("COLLECT_FIVE_SPECIES")) {
                mission.currentAmount = Math.max(mission.currentAmount, speciesCount);
            } else if (id.equals("EXPLORE_DEEP_BASE")) {
                int progress = exploredDeepBaseCount;
                if (deepestDepthReached >= 1200) {
                    progress = 1;
                }
                mission.currentAmount = Math.max(mission.currentAmount, progress);
            } else if (id.equals("RETURN_VALUE_3000")) {
                mission.currentAmount = Math.max(mission.currentAmount, totalFishValue);
            } else if (id.equals("SAFE_RETURN")) {
                mission.currentAmount = Math.max(mission.currentAmount, safeReturnCount);
            }

            limitMissionProgress(mission);

            if (mission.currentAmount >= mission.targetAmount) {
                completeMission(mission);
            }
        }
    }

    private static void scanOceanWorldRuntimeState() {
        try {
            for (Window w : Window.getWindows()) {
                scanComponentForOceanWorld(w);
            }
        } catch (Exception e) {
        }
    }

    private static void scanComponentForOceanWorld(Component c) {
        if (c == null) {
            return;
        }

        if (c.getClass().getSimpleName().equals("OceanWorld")) {
            Double playerYValue = readDoubleField(c, "playerY");
            if (playerYValue != null) {
                int depth = Math.max(0, (int) Math.round(playerYValue - 300));
                if (depth > deepestDepthReached) {
                    deepestDepthReached = depth;
                }

                if (depth >= 1200) {
                    exploredDeepBaseCount = Math.max(exploredDeepBaseCount, 1);
                }
            }

            Object fishListObj = readField(c, "fishList");
            if (fishListObj instanceof Iterable) {
                for (Object fishObj : (Iterable<?>) fishListObj) {
                    Boolean dead = readBooleanField(fishObj, "dead");
                    String name = readStringField(fishObj, "name");

                    if (Boolean.TRUE.equals(dead) && name != null) {
                        if (name.contains("綠") || name.contains("青魚") || name.contains("鰻") || name.toLowerCase().contains("eel")) {
                            defeatedGreenEelCount = Math.max(defeatedGreenEelCount, 1);
                        }
                    }
                }
            }
        }

        if (c instanceof Container) {
            Component[] children = ((Container) c).getComponents();
            for (Component child : children) {
                scanComponentForOceanWorld(child);
            }
        }
    }

    private static Object readField(Object target, String fieldName) {
        try {
            if (target == null) {
                return null;
            }

            Field field = findField(target.getClass(), fieldName);
            if (field == null) {
                return null;
            }

            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) {
            return null;
        }
    }

    private static Double readDoubleField(Object target, String fieldName) {
        Object value = readField(target, fieldName);
        return value instanceof Number ? ((Number) value).doubleValue() : null;
    }

    private static Boolean readBooleanField(Object target, String fieldName) {
        Object value = readField(target, fieldName);
        return value instanceof Boolean ? (Boolean) value : null;
    }

    private static String readStringField(Object target, String fieldName) {
        Object value = readField(target, fieldName);
        return value == null ? null : value.toString();
    }

    private static Field findField(Class<?> cls, String fieldName) {
        Class<?> current = cls;

        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }

        return null;
    }

    private static List<Fish> getAllPlayerFishSafely() {
        List<Fish> result = new ArrayList<>();

        addFishListFromInventoryMethod(result, "getMyBackpack");
        addFishListFromInventoryMethod(result, "getStorage");
        addFishListFromInventoryMethod(result, "getCurrentDiveList");
        addFishListFromInventoryMethod(result, "getCurrentDiveFishList");
        addFishListFromInventoryMethod(result, "getStorageList");
        addFishListFromInventoryMethod(result, "getPermanentStorageList");

        return result;
    }

    private static int getStorageFishCountSafely() {
        List<Fish> storage = new ArrayList<>();
        addFishListFromInventoryMethod(storage, "getStorage");
        addFishListFromInventoryMethod(storage, "getStorageList");
        addFishListFromInventoryMethod(storage, "getPermanentStorageList");
        return storage.size();
    }

    private static void addFishListFromInventoryMethod(List<Fish> result, String methodName) {
        try {
            Method method = InventoryManager.class.getMethod(methodName);
            Object value = method.invoke(null);

            if (value instanceof Iterable) {
                for (Object item : (Iterable<?>) value) {
                    if (item instanceof Fish) {
                        result.add((Fish) item);
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    private static int countFishByName(List<Fish> fishList, String keyword) {
        int count = 0;

        for (Fish fish : fishList) {
            if (fish != null && fish.getName() != null && fish.getName().contains(keyword)) {
                count++;
            }
        }

        return count;
    }

    private static int countRareFish(List<Fish> fishList, int minStars) {
        int count = 0;

        for (Fish fish : fishList) {
            if (fish != null && fish.getRarityStars() >= minStars) {
                count++;
            }
        }

        return count;
    }

    private static int countSpecies(List<Fish> fishList) {
        Set<String> names = new HashSet<>();

        for (Fish fish : fishList) {
            if (fish != null && fish.getName() != null) {
                names.add(fish.getName());
            }
        }

        return names.size();
    }

    private static int totalFishValue(List<Fish> fishList) {
        int total = 0;

        for (Fish fish : fishList) {
            if (fish != null) {
                total += fish.getPrice();
            }
        }

        return total;
    }

    private static void limitMissionProgress(Mission mission) {
        if (mission.currentAmount < 0) {
            mission.currentAmount = 0;
        }

        if (mission.currentAmount > mission.targetAmount) {
            mission.currentAmount = mission.targetAmount;
        }
    }

    private static void completeMission(Mission mission) {
        if (mission == null || mission.completed) {
            return;
        }

        mission.completed = true;
        mission.currentAmount = mission.targetAmount;

        if (!mission.rewardGiven) {
            mission.rewardGiven = true;

            try {
                InventoryManager.addMoney(mission.reward);
            } catch (Exception e) {
                System.out.println("Mission completed, but reward could not be added.");
            }
        }

        lastCompleteMessage = "任務完成：「" + mission.title + "」獲得 $" + mission.reward;
        lastCompleteMessageTime = System.currentTimeMillis();

        SwingUtilities.invokeLater(() -> {
            try {
                JOptionPane.showMessageDialog(null, lastCompleteMessage, "任務完成", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                System.out.println(lastCompleteMessage);
            }
        });
    }

    private static Mission findAcceptedMissionByTitle(String title) {
        if (title == null) {
            return null;
        }

        for (Mission mission : acceptedMissions) {
            if (mission.title != null && mission.title.equals(title)) {
                return mission;
            }
        }

        return null;
    }

    private static Mission findAcceptedMissionById(String id) {
        if (id == null) {
            return null;
        }

        for (Mission mission : acceptedMissions) {
            if (mission.id != null && mission.id.equals(id)) {
                return mission;
            }
        }

        return null;
    }

    private static String normalizeMissionId(String id, String title) {
        if (id == null || id.trim().isEmpty() || id.equals(title)) {
            return inferMissionId(title);
        }

        if (id.equals("COLLECT_FIVE_SPECIES")) {
            return "COLLECT_5_SPECIES";
        }

        return id;
    }

    private static String inferMissionId(String title) {
        if (title == null) {
            return "UNKNOWN";
        }

        if (title.contains("沙丁")) {
            return "CATCH_SARDINE";
        }

        if (title.contains("500")) {
            return "REACH_DEPTH_500";
        }

        if (title.contains("小丑")) {
            return "CATCH_CLOWNFISH";
        }

        if (title.contains("綠") || title.contains("青魚") || title.contains("鰻")) {
            return "DEFEAT_GREEN_EEL";
        }

        if (title.contains("五種") || title.contains("5")) {
            return "COLLECT_5_SPECIES";
        }

        if (title.contains("深海基地") || title.contains("基地")) {
            return "EXPLORE_DEEP_BASE";
        }

        if (title.contains("高價") || title.contains("3000")) {
            return "RETURN_VALUE_3000";
        }

        if (title.contains("返航") || title.contains("返回") || title.contains("安全")) {
            return "SAFE_RETURN";
        }

        return title;
    }

    public static void clearAcceptedMissions() {
        acceptedMissions.clear();
        acceptedMission = null;
        deepestDepthReached = 0;
        defeatedGreenEelCount = 0;
        safeReturnCount = 0;
        exploredDeepBaseCount = 0;
        lastCompleteMessage = "";
        lastCompleteMessageTime = 0;
    }
}
