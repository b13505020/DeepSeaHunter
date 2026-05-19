import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

public class MissionBoardView extends JFrame {

    // 改成可以同時接多個任務
    private static List<Mission> acceptedMissions = new ArrayList<>();

    public MissionBoardView() {
        setTitle("任務大廳 - Mission Board");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        MissionBoardPanel panel = new MissionBoardPanel();
        setContentPane(panel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static List<Mission> getAcceptedMissions() {
        return acceptedMissions;
    }

    public static String getAcceptedMissionTitle() {
        if (acceptedMissions.isEmpty()) {
            return "無";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < acceptedMissions.size(); i++) {
            if (i > 0) {
                sb.append("、");
            }
            sb.append(acceptedMissions.get(i).title);
        }
        return sb.toString();
    }

    // 之後 OceanWorld 或其他 class 可以呼叫這個來更新任務進度
    public static void addMissionProgress(String type, int amount) {
        for (Mission mission : acceptedMissions) {
            if (mission.type.equals(type) && !mission.completed) {
                mission.currentAmount += amount;

                if (mission.currentAmount >= mission.targetAmount) {
                    mission.currentAmount = mission.targetAmount;
                    mission.completed = true;
                }

                return;
            }
        }
    }

    // 如果某個任務不需要累加進度，可以直接標記完成
    public static void completeMission(String type) {
        for (Mission mission : acceptedMissions) {
            if (mission.type.equals(type)) {
                mission.currentAmount = mission.targetAmount;
                mission.completed = true;
                return;
            }
        }
    }

    public static boolean isMissionCompleted(String type) {
        for (Mission mission : acceptedMissions) {
            if (mission.type.equals(type)) {
                return mission.completed;
            }
        }
        return false;
    }

    class MissionBoardPanel extends JPanel {

        private static final int PANEL_WIDTH = 1600;
        private static final int PANEL_HEIGHT = 900;

        private final Color steelBlue = new Color(8, 30, 43);
        private final Color copper = new Color(210, 145, 58);
        private final Color gold = new Color(255, 216, 118);
        private final Color cyan = new Color(105, 225, 255);

        private List<Mission> missions = new ArrayList<>();
        private Mission selectedMission = null;
        private List<MissionCard> missionCards = new ArrayList<>();

        private JScrollPane scrollPane;
        private JPanel missionContainer;

        private int animTick = 0;
        private Timer animationTimer;

        private Rectangle listBounds = new Rectangle(486, 214, 842, 492);
        private Rectangle closeButtonBounds = new Rectangle(840, 776, 185, 58);
        private Rectangle acceptButtonBounds = new Rectangle(1065, 776, 310, 58);

        public MissionBoardPanel() {
            setLayout(null);
            setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
            setFocusable(true);

            createMissions();
            setupMissionList();
            setupButtons();
            setupAnimation();
            setupKeyActions();

            SwingUtilities.invokeLater(() -> requestFocusInWindow());
        }

        private void createMissions() {
            missions.add(new Mission(0, "捕捉沙丁魚", "前往淺海區捕捉 3 隻沙丁魚，完成潛水員基礎訓練。", "金幣 800", "CATCH_SARDINE", 3, "新手"));
            missions.add(new Mission(1, "下潛到 500m", "進入海洋地圖並成功下潛到 500m 深度，確認潛水衣狀態。", "金幣 1200", "REACH_DEPTH_500", 500, "探索"));
            missions.add(new Mission(2, "帶回小丑魚", "捕捉並帶回 1 隻小丑魚，交給水族館進行觀察。", "金幣 1500", "CATCH_CLOWNFISH", 1, "收集"));
            missions.add(new Mission(3, "擊倒綠鰻魚", "深海區出現具攻擊性的綠鰻魚，請擊倒 1 隻並安全回收。", "金幣 2500", "DEFEAT_GREEN_EEL", 1, "戰鬥"));
            missions.add(new Mission(4, "收集五種生物", "完成一次探索，捕捉並帶回 5 種不同海洋生物。", "金幣 3000", "COLLECT_5_SPECIES", 5, "收集"));
            missions.add(new Mission(5, "探索深海基地", "下潛至深海基地附近，完成區域偵查後安全返回。", "金幣 3500", "EXPLORE_DEEP_BASE", 1, "探索"));
            missions.add(new Mission(6, "高價魚獲回收", "單次下潛帶回總價值 3000 以上的魚獲。", "金幣 2200", "RETURN_VALUE_3000", 3000, "回收"));
            missions.add(new Mission(7, "安全返航訓練", "完成一次下潛任務，並成功回到船上或陸地基地。", "金幣 1000", "SAFE_RETURN", 1, "新手"));
        }

        private void setupMissionList() {
            missionContainer = new JPanel();
            missionContainer.setOpaque(false);
            missionContainer.setLayout(new BoxLayout(missionContainer, BoxLayout.Y_AXIS));
            missionContainer.setBorder(new EmptyBorder(8, 8, 8, 8));

            for (Mission mission : missions) {
                MissionCard card = new MissionCard(mission);
                missionCards.add(card);
                missionContainer.add(card);
                missionContainer.add(Box.createVerticalStrut(14));
            }

            scrollPane = new JScrollPane(missionContainer);
            scrollPane.setBounds(listBounds);
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            scrollPane.getVerticalScrollBar().setUnitIncrement(30);

            add(scrollPane);
        }

        private void setupButtons() {
            GameButton closeBtn = new GameButton("關閉");
            closeBtn.setBounds(closeButtonBounds);
            closeBtn.addActionListener(e -> dispose());

            GameButton acceptBtn = new GameButton("接受選取任務");
            acceptBtn.setBounds(acceptButtonBounds);
            acceptBtn.addActionListener(e -> acceptSelectedMission());

            add(closeBtn);
            add(acceptBtn);
        }

        private void setupAnimation() {
            animationTimer = new Timer(30, e -> {
                animTick++;
                repaint();
            });
            animationTimer.start();
        }

        private void setupKeyActions() {
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        dispose();
                    }
                }
            });
        }

        private boolean isAccepted(Mission mission) {
            for (Mission accepted : acceptedMissions) {
                if (accepted.type.equals(mission.type)) {
                    return true;
                }
            }
            return false;
        }

        private boolean isCompleted(Mission mission) {
            for (Mission accepted : acceptedMissions) {
                if (accepted.type.equals(mission.type)) {
                    return accepted.completed;
                }
            }
            return false;
        }

        private boolean isNextMission(Mission mission) {
            return mission.order == acceptedMissions.size();
        }

        private void acceptSelectedMission() {
            if (selectedMission == null) {
                JOptionPane.showMessageDialog(this, "請先點選一個任務。", "尚未選擇任務", JOptionPane.WARNING_MESSAGE);
                requestFocusInWindow();
                return;
            }

            if (isAccepted(selectedMission)) {
                JOptionPane.showMessageDialog(this, "這個任務已經接過了。", "任務已存在", JOptionPane.INFORMATION_MESSAGE);
                requestFocusInWindow();
                return;
            }

            if (!isNextMission(selectedMission)) {
                Mission nextMission = missions.get(acceptedMissions.size());
                JOptionPane.showMessageDialog(
                    this,
                    "任務必須照順序接取。\n\n下一個可接任務是：" + nextMission.title,
                    "尚未解鎖",
                    JOptionPane.WARNING_MESSAGE
                );
                requestFocusInWindow();
                return;
            }

            acceptedMissions.add(selectedMission);

            // 不再 dispose，所以玩家可以繼續接下一個任務
            selectedMission = null;
            refreshMissionList();
            repaint();

            JOptionPane.showMessageDialog(
                this,
                "已接受任務：\n" + acceptedMissions.get(acceptedMissions.size() - 1).title,
                "任務已接受",
                JOptionPane.INFORMATION_MESSAGE
            );

            requestFocusInWindow();
        }

        private void refreshMissionList() {
            missionContainer.removeAll();
            missionCards.clear();

            for (Mission mission : missions) {
                MissionCard card = new MissionCard(mission);
                missionCards.add(card);
                missionContainer.add(card);
                missionContainer.add(Box.createVerticalStrut(14));
            }

            missionContainer.revalidate();
            missionContainer.repaint();
        }

        private void selectMission(Mission mission) {
            selectedMission = mission;
            refreshMissionList();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            drawBackground(g2);
            drawAnimatedWaterLight(g2);
            drawOuterFrame(g2);
            drawMainConsole(g2);
            drawTitle(g2);
            drawAcceptedMissionPanel(g2);
            drawSidePanels(g2);
            drawMissionHeader(g2);
            drawBottomConsole(g2);
            drawMissionOfficer(g2);
            drawDiver(g2);
            drawScrollIndicator(g2);
            drawCornerDecorations(g2);
            drawVignette(g2);

            g2.dispose();
        }

        private void drawBackground(Graphics2D g2) {
            GradientPaint bg = new GradientPaint(0, 0, new Color(1, 5, 11), 0, PANEL_HEIGHT, new Color(0, 1, 4));
            g2.setPaint(bg);
            g2.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

            g2.setColor(new Color(9, 31, 44, 150));
            for (int y = 0; y < PANEL_HEIGHT; y += 42) {
                g2.drawLine(0, y, PANEL_WIDTH, y);
            }
            for (int x = 0; x < PANEL_WIDTH; x += 56) {
                g2.drawLine(x, 0, x, PANEL_HEIGHT);
            }

            g2.setColor(new Color(255, 220, 120, 12));
            for (int i = 0; i < 7; i++) {
                int x = 190 + i * 210;
                int y = 130 + (i % 3) * 210;
                g2.drawOval(x, y, 160, 160);
            }
        }

        private void drawAnimatedWaterLight(Graphics2D g2) {
            RadialGradientPaint topLight = new RadialGradientPaint(
                new Point2D.Double(PANEL_WIDTH / 2.0, 120),
                680,
                new float[]{0f, 0.45f, 1f},
                new Color[]{new Color(0, 190, 255, 100), new Color(0, 90, 140, 30), new Color(0, 0, 0, 0)}
            );
            g2.setPaint(topLight);
            g2.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

            int scanY = 170 + (animTick * 2) % 540;
            g2.setColor(new Color(255, 220, 120, 30));
            g2.fillRect(420, scanY, 965, 2);

            for (int i = 0; i < 34; i++) {
                int x = 60 + i * 48;
                int y = 60 + ((animTick + i * 29) % 800);
                int r = 2 + (i % 5);
                g2.setColor(new Color(70, 220, 255, 14 + (i % 4) * 7));
                g2.fillOval(x, y, r, r);
            }
        }

        private void drawOuterFrame(Graphics2D g2) {
            g2.setStroke(new BasicStroke(20, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(47, 27, 12));
            g2.drawLine(55, 76, 1545, 76);
            g2.drawLine(55, 848, 1545, 848);
            g2.drawLine(55, 82, 55, 842);
            g2.drawLine(1545, 82, 1545, 842);

            g2.setStroke(new BasicStroke(6));
            g2.setColor(copper);
            g2.drawLine(56, 63, 1544, 63);
            g2.drawLine(56, 835, 1544, 835);
            g2.drawLine(43, 82, 43, 842);
            g2.drawLine(1532, 82, 1532, 842);

            g2.setStroke(new BasicStroke(2));
            g2.setColor(new Color(255, 232, 160, 120));
            g2.drawLine(56, 53, 1544, 53);
            g2.drawLine(56, 825, 1544, 825);

            for (int x = 145; x < 1500; x += 180) {
                drawBolt(g2, x, 75, 18);
                drawBolt(g2, x, 850, 18);
            }
        }

        private void drawMainConsole(Graphics2D g2) {
            drawMetalPanel(g2, 94, 104, 1412, 736, 34, new Color(9, 29, 43, 240));
            drawMetalPanel(g2, 420, 150, 965, 585, 30, new Color(6, 22, 35, 236));
            drawMetalPanel(g2, 420, 750, 965, 82, 24, new Color(6, 22, 35, 238));

            g2.setColor(new Color(0, 210, 255, 22));
            g2.fillRoundRect(438, 168, 929, 548, 24, 24);

            g2.setColor(new Color(255, 215, 120, 45));
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(435, 165, 935, 555, 26, 26);
        }

        private void drawTitle(Graphics2D g2) {
            drawMetalPanel(g2, 494, 26, 612, 108, 30, new Color(18, 52, 68, 242));

            int pulse = 42 + (int) (Math.sin(animTick * 0.07) * 14);
            g2.setColor(new Color(255, 210, 100, pulse));
            g2.fillOval(765, 18, 70, 70);
            drawBolt(g2, 800, 34, 36);

            g2.setFont(new Font("Serif", Font.BOLD, 57));
            g2.setColor(new Color(0, 0, 0, 170));
            g2.drawString("MISSION BOARD", 558, 104);
            g2.setColor(gold);
            g2.drawString("MISSION BOARD", 552, 98);

            g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
            g2.setColor(cyan);
            g2.drawString("深海工域總部委託系統", 704, 125);
            g2.setColor(new Color(255, 220, 120, 95));
            g2.drawLine(575, 111, 1025, 111);
        }

        private void drawAcceptedMissionPanel(Graphics2D g2) {
            drawMetalPanel(g2, 1125, 112, 315, 118, 22, new Color(8, 30, 43, 238));

            g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 19));
            g2.setColor(gold);
            g2.drawString("目前已接任務", 1150, 145);

            g2.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));
            g2.setColor(new Color(190, 235, 245));

            if (acceptedMissions.isEmpty()) {
                g2.drawString("尚未接取任務", 1150, 175);
                g2.drawString("請從第一個任務開始。", 1150, 198);
            } else {
                int y = 174;
                int start = Math.max(0, acceptedMissions.size() - 3);
                for (int i = start; i < acceptedMissions.size(); i++) {
                    Mission m = acceptedMissions.get(i);
                    String state = m.completed ? "完成" : (m.currentAmount + "/" + m.targetAmount);
                    g2.drawString((i + 1) + ". " + m.title + "  [" + state + "]", 1150, y);
                    y += 22;
                }
            }
        }

        private void drawSidePanels(Graphics2D g2) {
            drawMetalPanel(g2, 125, 150, 250, 270, 28, new Color(12, 39, 52, 238));
            drawMetalPanel(g2, 125, 475, 250, 280, 28, new Color(12, 39, 52, 238));

            drawGlassWindow(g2, 150, 205, 200, 170);
            drawGlassWindow(g2, 150, 535, 200, 175);

            g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 23));
            g2.setColor(new Color(0, 0, 0, 150));
            g2.drawString("任務官", 209, 190);
            g2.drawString("潛水員", 209, 517);
            g2.setColor(new Color(250, 218, 135));
            g2.drawString("任務官", 206, 188);
            g2.drawString("潛水員", 206, 515);
        }

        private void drawGlassWindow(Graphics2D g2, int x, int y, int w, int h) {
            GradientPaint glassPaint = new GradientPaint(x, y, new Color(100, 220, 255, 58), x, y + h, new Color(0, 60, 90, 36));
            g2.setPaint(glassPaint);
            g2.fillRoundRect(x, y, w, h, 18, 18);
            g2.setColor(new Color(255, 230, 140, 90));
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(x + 1, y + 1, w - 2, h - 2, 18, 18);
            g2.setColor(new Color(255, 255, 255, 42));
            g2.drawLine(x + 16, y + 18, x + w - 18, y + 8);
            g2.drawLine(x + 28, y + 42, x + w - 22, y + 25);
        }

        private void drawMissionHeader(Graphics2D g2) {
            g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 28));
            g2.setColor(gold);
            g2.drawString("可接取任務", 455, 190);
            g2.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
            g2.setColor(new Color(150, 235, 255));
            g2.drawString("任務必須照順序接取，可連續接多個任務。", 625, 190);
            g2.setColor(new Color(255, 210, 110, 60));
            g2.drawLine(455, 198, 1325, 198);
        }

        private void drawBottomConsole(Graphics2D g2) {
            int completedCount = 0;
            for (Mission m : acceptedMissions) {
                if (m.completed) {
                    completedCount++;
                }
            }

            String current = "目前任務數量：" + acceptedMissions.size() + "　完成：" + completedCount;
            int nextIndex = acceptedMissions.size();
            String nextText;
            if (nextIndex < missions.size()) {
                nextText = "下一個可接任務：" + missions.get(nextIndex).title;
            } else {
                nextText = "全部任務皆已接取";
            }

            g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
            g2.setColor(new Color(255, 225, 140));
            g2.drawString(current, 455, 805);

            g2.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));
            g2.setColor(new Color(150, 225, 245));
            g2.drawString(nextText + "　｜　ESC 可直接關閉任務大廳", 455, 828);
        }

        private void drawMissionOfficer(Graphics2D g2) {
            int boxX = 150;
            int boxY = 205;
            int floatY = (int) (Math.sin(animTick * 0.07) * 3);
            int sway = (int) (Math.sin(animTick * 0.04) * 2);
            int x = boxX + 40 + sway;
            int y = boxY + 18 + floatY;
            int w = 120;
            int h = 135;
            g2.setColor(new Color(0, 0, 0, 110));
            g2.fillOval(boxX + 55, boxY + 145, 90, 16);
            g2.setColor(new Color(18, 24, 32));
            g2.fillRoundRect(x + w / 3 - 6, y + h - 36, 22, 38, 7, 7);
            g2.fillRoundRect(x + w / 2 + 5, y + h - 36, 22, 38, 7, 7);
            g2.setPaint(new GradientPaint(x, y + 45, new Color(32, 45, 58), x, y + h - 20, new Color(12, 18, 28)));
            g2.fillRoundRect(x + 26, y + 48, w - 52, 75, 18, 18);
            g2.setColor(new Color(28, 38, 50));
            g2.fillRoundRect(x + 8, y + 60, 25, 58, 9, 9);
            g2.fillRoundRect(x + w - 33, y + 60, 25, 58, 9, 9);
            g2.setColor(new Color(230, 170, 70));
            g2.fillRect(x + 34, y + 62, w - 68, 6);
            g2.fillRect(x + 36, y + 93, w - 72, 5);
            g2.fillOval(x + w / 2 - 7, y + 84, 14, 14);
            g2.setColor(new Color(218, 173, 124));
            g2.fillOval(x + 36, y + 14, w - 72, 48);
            g2.setColor(new Color(18, 25, 34));
            g2.fillRoundRect(x + 30, y + 6, w - 60, 20, 12, 12);
            g2.fillArc(x + 35, y - 2, w - 70, 38, 0, 180);
            g2.setColor(new Color(230, 170, 70));
            g2.drawLine(x + 36, y + 24, x + w - 36, y + 24);
            g2.fillOval(x + w / 2 - 6, y + 8, 12, 12);
            g2.setColor(new Color(25, 18, 12));
            g2.fillOval(x + 48, y + 36, 5, 5);
            g2.fillOval(x + w - 53, y + 36, 5, 5);
            g2.drawArc(x + 50, y + 41, 20, 12, 200, 140);
            g2.setColor(new Color(255, 220, 120, 150));
            g2.drawOval(x + w / 2 - 16, y + 78, 32, 32);
        }

        private void drawDiver(Graphics2D g2) {
            int boxX = 150;
            int boxY = 535;
            int floatY = (int) (Math.sin(animTick * 0.075) * 4);
            int x = boxX + 36;
            int y = boxY + 12 + floatY;
            int w = 128;
            int h = 145;
            g2.setColor(new Color(0, 0, 0, 110));
            g2.fillOval(boxX + 50, boxY + 150, 100, 17);
            g2.setColor(new Color(42, 31, 24));
            g2.fillRoundRect(x + w / 3 - 4, y + h - 32, 24, 36, 8, 8);
            g2.fillRoundRect(x + w / 2 + 7, y + h - 32, 24, 36, 8, 8);
            g2.setPaint(new GradientPaint(x, y + 50, new Color(130, 82, 45), x, y + h - 20, new Color(53, 37, 28)));
            g2.fillRoundRect(x + 30, y + 55, w - 60, 75, 22, 22);
            g2.setColor(new Color(220, 150, 70));
            g2.drawRoundRect(x + 34, y + 60, w - 68, 64, 18, 18);
            g2.drawLine(x + w / 2, y + 58, x + w / 2, y + 128);
            g2.setColor(new Color(45, 48, 50));
            g2.fillOval(x + 29, y + 10, w - 58, 62);
            g2.setColor(new Color(155, 108, 62));
            g2.setStroke(new BasicStroke(3));
            g2.drawOval(x + 32, y + 13, w - 64, 56);
            g2.setColor(new Color(105, 210, 245));
            g2.fillOval(x + 48, y + 30, w - 96, 27);
            int glow = 65 + (int) (Math.sin(animTick * 0.12) * 40);
            g2.setColor(new Color(115, 225, 255, Math.max(30, Math.min(115, glow))));
            g2.fillOval(x + 39, y + 22, w - 78, 43);
            g2.setColor(new Color(65, 45, 32));
            g2.fillRoundRect(x + 8, y + 70, 28, 55, 10, 10);
            g2.fillRoundRect(x + w - 36, y + 70, 28, 55, 10, 10);
        }

        private void drawScrollIndicator(Graphics2D g2) {
            int x = 1342;
            int y = 222;
            int h = 470;
            g2.setColor(new Color(38, 96, 125, 165));
            g2.fillRoundRect(x, y, 9, h, 9, 9);
            int max = scrollPane.getVerticalScrollBar().getMaximum();
            int value = scrollPane.getVerticalScrollBar().getValue();
            int knobY = y;
            if (max > 0) {
                knobY = y + (int) ((h - 90) * (value / (double) max));
            }
            g2.setColor(new Color(178, 236, 255, 215));
            g2.fillRoundRect(x - 4, knobY, 17, 90, 12, 12);
            g2.setColor(new Color(255, 220, 110, 110));
            g2.drawLine(x + 4, y - 20, x + 4, y - 6);
            g2.drawLine(x + 4, y + h + 6, x + 4, y + h + 20);
        }

        private void drawCornerDecorations(Graphics2D g2) {
            g2.setStroke(new BasicStroke(4));
            g2.setColor(new Color(255, 215, 120, 80));
            int[][] corners = {{108, 118}, {1458, 118}, {108, 796}, {1458, 796}};
            for (int[] c : corners) {
                g2.drawLine(c[0], c[1], c[0] + 34, c[1]);
                g2.drawLine(c[0], c[1], c[0], c[1] + 34);
            }
        }

        private void drawVignette(Graphics2D g2) {
            g2.setColor(new Color(0, 0, 0, 95));
            g2.fillRect(0, 0, PANEL_WIDTH, 18);
            g2.fillRect(0, PANEL_HEIGHT - 18, PANEL_WIDTH, 18);
            g2.fillRect(0, 0, 18, PANEL_HEIGHT);
            g2.fillRect(PANEL_WIDTH - 18, 0, 18, PANEL_HEIGHT);
        }

        private void drawMetalPanel(Graphics2D g2, int x, int y, int w, int h, int arc, Color fill) {
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRoundRect(x + 8, y + 8, w, h, arc, arc);
            g2.setPaint(new GradientPaint(x, y, fill.brighter(), x, y + h, fill.darker()));
            g2.fillRoundRect(x, y, w, h, arc, arc);
            g2.setColor(copper);
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(x + 2, y + 2, w - 4, h - 4, arc, arc);
            g2.setColor(new Color(255, 255, 255, 35));
            g2.drawRoundRect(x + 10, y + 10, w - 20, h - 20, Math.max(8, arc - 6), Math.max(8, arc - 6));
        }

        private void drawBolt(Graphics2D g2, int x, int y, int r) {
            g2.setColor(new Color(35, 20, 8, 180));
            g2.fillOval(x - r, y - r, r * 2, r * 2);
            g2.setColor(new Color(220, 160, 60));
            g2.setStroke(new BasicStroke(3));
            g2.drawOval(x - r, y - r, r * 2, r * 2);
            g2.drawLine(x - r / 2, y, x + r / 2, y);
            g2.drawLine(x, y - r / 2, x, y + r / 2);
        }

        class MissionCard extends JPanel {
            private Mission mission;
            private boolean hover = false;

            public MissionCard(Mission mission) {
                this.mission = mission;
                setOpaque(false);
                setPreferredSize(new Dimension(815, 112));
                setMaximumSize(new Dimension(815, 112));
                setLayout(new BorderLayout());
                setBorder(new EmptyBorder(10, 12, 10, 12));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                JLabel icon = new JLabel(getIconForMission(mission), SwingConstants.CENTER);
                icon.setPreferredSize(new Dimension(70, 85));
                icon.setFont(new Font("Microsoft JhengHei", Font.BOLD, 30));
                icon.setForeground(new Color(245, 195, 90));

                JPanel textPanel = new JPanel();
                textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
                textPanel.setOpaque(false);
                textPanel.setBorder(new EmptyBorder(4, 8, 4, 8));

                JLabel title = new JLabel(mission.title + "　[" + mission.category + "]");
                title.setFont(new Font("Microsoft JhengHei", Font.BOLD, 23));
                title.setForeground(new Color(255, 220, 125));

                JLabel desc = new JLabel("<html><body style='width:555px'>" + mission.description + "</body></html>");
                desc.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));
                desc.setForeground(new Color(225, 235, 235));

                JLabel reward = new JLabel("獎勵：" + mission.reward);
                reward.setFont(new Font("Microsoft JhengHei", Font.BOLD, 15));
                reward.setForeground(new Color(105, 230, 255));

                textPanel.add(title);
                textPanel.add(Box.createVerticalStrut(5));
                textPanel.add(desc);
                textPanel.add(Box.createVerticalStrut(5));
                textPanel.add(reward);

                JLabel status = new JLabel(getMissionStatusText(mission), SwingConstants.CENTER);
                status.setPreferredSize(new Dimension(90, 85));
                status.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
                status.setForeground(new Color(230, 190, 95));

                add(icon, BorderLayout.WEST);
                add(textPanel, BorderLayout.CENTER);
                add(status, BorderLayout.EAST);

                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        selectMission(mission);
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hover = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hover = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean selected = mission == selectedMission;
                boolean accepted = isAccepted(mission);
                boolean next = isNextMission(mission);

                Color top;
                Color bottom;
                Color border;

                if (isCompleted(mission)) {
                    top = new Color(80, 105, 45, 235);
                    bottom = new Color(35, 65, 28, 235);
                    border = new Color(255, 225, 120);
                } else if (accepted) {
                    top = new Color(35, 92, 65, 232);
                    bottom = new Color(12, 50, 35, 232);
                    border = new Color(125, 235, 150);
                } else if (selected) {
                    top = new Color(34, 112, 140, 242);
                    bottom = new Color(12, 50, 78, 242);
                    border = new Color(255, 214, 95);
                } else if (next) {
                    top = new Color(14, 52, 72, 230);
                    bottom = new Color(6, 28, 44, 230);
                    border = new Color(210, 160, 70);
                } else if (hover) {
                    top = new Color(18, 68, 90, 234);
                    bottom = new Color(8, 38, 55, 234);
                    border = new Color(150, 120, 70);
                } else {
                    top = new Color(8, 30, 42, 170);
                    bottom = new Color(4, 14, 22, 170);
                    border = new Color(80, 70, 52);
                }

                g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bottom));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.setColor(border);
                g2.setStroke(new BasicStroke(selected ? 4 : 2));
                g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 18, 18);

                if (selected || next) {
                    g2.setColor(new Color(255, 220, 90, selected ? 48 : 26));
                    g2.fillRoundRect(8, 8, getWidth() - 16, getHeight() - 16, 14, 14);
                }

                g2.dispose();
                super.paintComponent(g);
            }
        }

        class GameButton extends JButton {
            public GameButton(String text) {
                super(text);
                setFont(new Font("Microsoft JhengHei", Font.BOLD, 22));
                setForeground(new Color(255, 235, 170));
                setFocusPainted(false);
                setContentAreaFilled(false);
                setOpaque(false);
                setBorder(BorderFactory.createEmptyBorder());
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                ButtonModel model = getModel();
                Color top = model.isPressed() ? new Color(13, 55, 70) : new Color(24, 94, 116);
                Color bottom = model.isPressed() ? new Color(7, 28, 40) : new Color(10, 50, 70);
                g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bottom));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(235, 185, 75));
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 16, 16);
                if (model.isRollover()) {
                    g2.setColor(new Color(255, 230, 120, 50));
                    g2.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 12, 12);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        }

        private String getMissionStatusText(Mission mission) {
            if (isCompleted(mission)) {
                return "完成";
            }
            if (isAccepted(mission)) {
                return "已接";
            }
            if (isNextMission(mission)) {
                return "可接";
            }
            return "鎖定";
        }

        private String getIconForMission(Mission mission) {
            if (mission.category.equals("戰鬥")) return "⚔";
            if (mission.category.equals("探索")) return "⌖";
            if (mission.category.equals("收集")) return "◆";
            if (mission.category.equals("回收")) return "▣";
            return "⚓";
        }
    }

    public static class Mission {
        int order;
        String title;
        String description;
        String reward;
        String type;
        int targetAmount;
        int currentAmount;
        boolean completed;
        String category;

        public Mission(int order, String title, String description, String reward, String type, int targetAmount, String category) {
            this.order = order;
            this.title = title;
            this.description = description;
            this.reward = reward;
            this.type = type;
            this.targetAmount = targetAmount;
            this.currentAmount = 0;
            this.completed = false;
            this.category = category;
        }
    }
}
