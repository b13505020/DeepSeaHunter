import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

public class MissionBoardView extends JFrame {

    private static Mission acceptedMission = null;

    public MissionBoardView() {
        setTitle("任務大廳 - Mission Board");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setUndecorated(false);

        MissionBoardPanel panel = new MissionBoardPanel();
        setContentPane(panel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static Mission getAcceptedMission() {
        return acceptedMission;
    }

    public static String getAcceptedMissionTitle() {
        if (acceptedMission == null) {
            return "無";
        }
        return acceptedMission.title;
    }

    class MissionBoardPanel extends JPanel {

        private static final int PANEL_WIDTH = 1600;
        private static final int PANEL_HEIGHT = 900;

        private List<Mission> missions = new ArrayList<>();
        private Mission selectedMission = null;
        private List<JPanel> missionCards = new ArrayList<>();

        private int animTick = 0;
        private Timer animationTimer;

        private Rectangle missionListBounds = new Rectangle(455, 185, 850, 520);
        private Rectangle closeButtonBounds = new Rectangle(820, 765, 180, 60);
        private Rectangle acceptButtonBounds = new Rectangle(1050, 765, 285, 60);

        public MissionBoardPanel() {
            setLayout(null);
            setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
            setFocusable(true);

            createMissions();
            setupMissionScrollList();
            setupButtons();
            setupAnimation();

            SwingUtilities.invokeLater(() -> requestFocusInWindow());
        }

        private void createMissions() {
            missions.add(new Mission(
                "捕捉沙丁魚",
                "前往淺海區捕捉 3 隻沙丁魚，作為新手潛水員的基礎訓練。",
                "金幣 800",
                "CATCH_SARDINE",
                3
            ));

            missions.add(new Mission(
                "下潛到 500m",
                "進入海洋地圖並成功下潛到 500m 深度，確認潛水衣運作狀態。",
                "金幣 1200",
                "REACH_DEPTH_500",
                500
            ));

            missions.add(new Mission(
                "帶回小丑魚",
                "捕捉並帶回 1 隻小丑魚，交給水族館進行觀察。",
                "金幣 1500",
                "CATCH_CLOWNFISH",
                1
            ));

            missions.add(new Mission(
                "擊倒綠鰻魚",
                "深海區出現具攻擊性的綠鰻魚，請擊倒 1 隻並安全回收。",
                "金幣 2500",
                "DEFEAT_GREEN_EEL",
                1
            ));

            missions.add(new Mission(
                "收集五種生物",
                "完成一次探索，捕捉並帶回 5 種不同海洋生物。",
                "金幣 3000",
                "COLLECT_5_SPECIES",
                5
            ));

            missions.add(new Mission(
                "探索深海基地",
                "下潛至深海基地附近，完成區域偵查後安全返回。",
                "金幣 3500",
                "EXPLORE_DEEP_BASE",
                1
            ));

            missions.add(new Mission(
                "高價魚獲回收",
                "單次下潛帶回總價值 3000 以上的魚獲。",
                "金幣 2200",
                "RETURN_VALUE_3000",
                3000
            ));

            missions.add(new Mission(
                "安全返航訓練",
                "完成一次下潛任務，並成功回到船上或陸地基地。",
                "金幣 1000",
                "SAFE_RETURN",
                1
            ));
        }

        private void setupMissionScrollList() {
            JPanel missionContainer = new JPanel();
            missionContainer.setLayout(new BoxLayout(missionContainer, BoxLayout.Y_AXIS));
            missionContainer.setOpaque(false);
            missionContainer.setBorder(new EmptyBorder(8, 8, 8, 8));

            for (Mission mission : missions) {
                JPanel card = createMissionCard(mission);
                missionCards.add(card);
                missionContainer.add(card);
                missionContainer.add(Box.createVerticalStrut(12));
            }

            JScrollPane scrollPane = new JScrollPane(missionContainer);
            scrollPane.setBounds(missionListBounds);
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            scrollPane.getVerticalScrollBar().setUnitIncrement(24);

            add(scrollPane);
        }

        private JPanel createMissionCard(Mission mission) {
            JPanel card = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    boolean selected = mission == selectedMission;
                    Color bgTop = selected ? new Color(25, 95, 125, 235) : new Color(8, 35, 52, 225);
                    Color bgBottom = selected ? new Color(15, 55, 80, 235) : new Color(4, 18, 30, 225);
                    Color border = selected ? new Color(255, 205, 80) : new Color(120, 95, 48);

                    GradientPaint gp = new GradientPaint(0, 0, bgTop, 0, getHeight(), bgBottom);
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);

                    g2.setColor(border);
                    g2.setStroke(new BasicStroke(selected ? 4 : 2));
                    g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 18, 18);

                    if (selected) {
                        g2.setColor(new Color(255, 220, 90, 55));
                        g2.fillRoundRect(8, 8, getWidth() - 16, getHeight() - 16, 14, 14);
                    }

                    g2.dispose();
                }
            };

            card.setOpaque(false);
            card.setMaximumSize(new Dimension(820, 112));
            card.setPreferredSize(new Dimension(820, 112));
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            card.setBorder(new EmptyBorder(10, 12, 10, 12));

            JLabel icon = new JLabel("⚓", SwingConstants.CENTER);
            icon.setPreferredSize(new Dimension(72, 86));
            icon.setFont(new Font("Serif", Font.BOLD, 34));
            icon.setForeground(new Color(230, 188, 85));

            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false);
            textPanel.setBorder(new EmptyBorder(4, 8, 4, 8));

            JLabel titleLabel = new JLabel(mission.title);
            titleLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 23));
            titleLabel.setForeground(new Color(255, 218, 125));

            JLabel descLabel = new JLabel("<html><body style='width:560px'>" + mission.description + "</body></html>");
            descLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));
            descLabel.setForeground(new Color(225, 235, 235));

            JLabel rewardLabel = new JLabel("獎勵：" + mission.reward);
            rewardLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 15));
            rewardLabel.setForeground(new Color(115, 230, 255));

            textPanel.add(titleLabel);
            textPanel.add(Box.createVerticalStrut(5));
            textPanel.add(descLabel);
            textPanel.add(Box.createVerticalStrut(5));
            textPanel.add(rewardLabel);

            JLabel statusLabel = new JLabel("選取", SwingConstants.CENTER);
            statusLabel.setPreferredSize(new Dimension(90, 86));
            statusLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
            statusLabel.setForeground(new Color(225, 185, 90));

            card.add(icon, BorderLayout.WEST);
            card.add(textPanel, BorderLayout.CENTER);
            card.add(statusLabel, BorderLayout.EAST);

            card.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectedMission = mission;
                    updateMissionCardStyles();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (mission != selectedMission) {
                        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    }
                }
            });

            return card;
        }

        private void updateMissionCardStyles() {
            for (JPanel card : missionCards) {
                card.repaint();
            }
            repaint();
        }

        private void setupButtons() {
            JButton closeBtn = createGameButton("關閉");
            closeBtn.setBounds(closeButtonBounds);
            closeBtn.addActionListener(e -> dispose());

            JButton acceptBtn = createGameButton("接受選取任務");
            acceptBtn.setBounds(acceptButtonBounds);
            acceptBtn.addActionListener(e -> acceptSelectedMission());

            add(closeBtn);
            add(acceptBtn);
        }

        private JButton createGameButton(String text) {
            JButton btn = new JButton(text) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    ButtonModel model = getModel();
                    Color top = model.isPressed() ? new Color(20, 60, 75) : new Color(25, 95, 115);
                    Color bottom = model.isPressed() ? new Color(10, 35, 50) : new Color(12, 55, 72);

                    g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bottom));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

                    g2.setColor(new Color(235, 185, 75));
                    g2.setStroke(new BasicStroke(3));
                    g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 16, 16);

                    g2.dispose();
                    super.paintComponent(g);
                }
            };

            btn.setFont(new Font("Microsoft JhengHei", Font.BOLD, 22));
            btn.setForeground(new Color(255, 235, 170));
            btn.setFocusPainted(false);
            btn.setContentAreaFilled(false);
            btn.setOpaque(false);
            btn.setBorder(BorderFactory.createEmptyBorder());
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return btn;
        }

        private void acceptSelectedMission() {
            if (selectedMission == null) {
                JOptionPane.showMessageDialog(
                    this,
                    "請先點選一個任務。",
                    "尚未選擇任務",
                    JOptionPane.WARNING_MESSAGE
                );
                requestFocusInWindow();
                return;
            }

            acceptedMission = selectedMission;

            JOptionPane.showMessageDialog(
                this,
                "已接受任務：\n" + acceptedMission.title + "\n\n" + acceptedMission.description,
                "任務已接受",
                JOptionPane.INFORMATION_MESSAGE
            );

            dispose();
        }

        private void setupAnimation() {
            animationTimer = new Timer(30, e -> {
                animTick++;
                repaint();
            });
            animationTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            drawBackground(g2);
            drawTopTitle(g2);
            drawLeftCharacterPanels(g2);
            drawMissionFrame(g2);
            drawButtonArea(g2);
            drawAnimatedNpc(g2);
            drawAnimatedDiver(g2);
            drawCurrentMissionText(g2);
            drawScrollbarHint(g2);

            g2.dispose();
        }

        private void drawBackground(Graphics2D g2) {
            GradientPaint bg = new GradientPaint(
                0, 0, new Color(10, 18, 28),
                0, PANEL_HEIGHT, new Color(4, 8, 14)
            );
            g2.setPaint(bg);
            g2.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

            g2.setColor(new Color(20, 45, 60, 180));
            for (int x = -100; x < PANEL_WIDTH + 100; x += 140) {
                g2.drawLine(x, 0, x + 260, PANEL_HEIGHT);
            }

            g2.setColor(new Color(255, 200, 80, 45));
            for (int i = 0; i < 10; i++) {
                int cx = 120 + i * 160;
                int cy = 120 + (i % 3) * 180;
                g2.fillOval(cx, cy, 5, 5);
            }
        }

        private void drawTopTitle(Graphics2D g2) {
            drawMetalPanel(g2, 390, 35, 820, 95, 28, new Color(18, 48, 65, 235));

            g2.setFont(new Font("Serif", Font.BOLD, 54));
            g2.setColor(new Color(40, 25, 10, 120));
            g2.drawString("MISSION BOARD", 520, 102);

            g2.setColor(new Color(245, 205, 110));
            g2.drawString("MISSION BOARD", 516, 98);

            g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
            g2.setColor(new Color(140, 230, 255));
            g2.drawString("Deep Sea Industry Headquarters", 650, 122);
        }

        private void drawLeftCharacterPanels(Graphics2D g2) {
            drawMetalPanel(g2, 80, 145, 285, 275, 28, new Color(18, 40, 52, 230));
            drawMetalPanel(g2, 80, 480, 285, 285, 28, new Color(18, 40, 52, 230));

            g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 24));
            g2.setColor(new Color(245, 205, 110));
            g2.drawString("任務官", 180, 180);
            g2.drawString("潛水員", 178, 515);

            g2.setColor(new Color(80, 180, 210, 80));
            g2.drawRoundRect(112, 195, 220, 190, 18, 18);
            g2.drawRoundRect(112, 530, 220, 195, 18, 18);
        }

        private void drawMissionFrame(Graphics2D g2) {
            drawMetalPanel(g2, 420, 145, 930, 590, 30, new Color(12, 32, 45, 230));

            g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 28));
            g2.setColor(new Color(245, 205, 110));
            g2.drawString("可接取任務", 455, 182);

            g2.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
            g2.setColor(new Color(150, 230, 255));
            g2.drawString("滑鼠滾輪可上下查看任務，點擊任務卡片後按下接受。", 600, 182);
        }

        private void drawButtonArea(Graphics2D g2) {
            drawMetalPanel(g2, 420, 748, 930, 100, 26, new Color(10, 28, 40, 230));
        }

        private void drawAnimatedNpc(Graphics2D g2) {
            int baseX = 150;
            int baseY = 210;
            int drawW = 145;
            int drawH = 180;

            int floatY = (int) (Math.sin(animTick * 0.08) * 4);
            int swayX = (int) (Math.sin(animTick * 0.04) * 2);

            g2.setColor(new Color(0, 0, 0, 90));
            g2.fillOval(baseX + 22, baseY + drawH - 8, drawW - 44, 18);

            drawProgramNpc(g2, baseX + swayX, baseY + floatY, drawW, drawH);
        }

        private void drawProgramNpc(Graphics2D g2, int x, int y, int w, int h) {
            g2.setStroke(new BasicStroke(3));

            int headW = w / 2;
            int headH = h / 4;
            int headX = x + w / 4;
            int headY = y + 10;

            g2.setColor(new Color(205, 165, 120));
            g2.fillOval(headX, headY, headW, headH);

            g2.setColor(new Color(60, 45, 35));
            g2.fillArc(headX - 2, headY - 8, headW + 4, headH, 0, 180);

            g2.setColor(new Color(30, 35, 42));
            g2.fillRoundRect(x + w / 4, y + h / 3, w / 2, h / 2, 18, 18);

            g2.setColor(new Color(210, 150, 55));
            g2.fillRect(x + w / 4 + 8, y + h / 3 + 12, w / 2 - 16, 8);
            g2.fillOval(x + w / 2 - 9, y + h / 2 - 5, 18, 18);

            g2.setColor(new Color(55, 70, 80));
            g2.fillRoundRect(x + 20, y + h / 3 + 15, 28, h / 3, 10, 10);
            g2.fillRoundRect(x + w - 48, y + h / 3 + 15, 28, h / 3, 10, 10);

            g2.setColor(new Color(25, 28, 35));
            g2.fillRoundRect(x + w / 3, y + h - 38, 28, 44, 8, 8);
            g2.fillRoundRect(x + w / 2, y + h - 38, 28, 44, 8, 8);

            g2.setColor(new Color(25, 20, 15));
            g2.fillOval(headX + headW / 3, headY + headH / 2, 5, 5);
            g2.fillOval(headX + headW * 2 / 3, headY + headH / 2, 5, 5);

            g2.setColor(new Color(245, 210, 110, 120));
            g2.drawOval(x + w / 2 - 18, y + h / 2 - 18, 36, 36);
        }

        private void drawAnimatedDiver(Graphics2D g2) {
            int baseX = 145;
            int baseY = 555;
            int drawW = 150;
            int drawH = 195;

            int floatY = (int) (Math.sin(animTick * 0.075) * 5);

            g2.setColor(new Color(0, 0, 0, 90));
            g2.fillOval(baseX + 20, baseY + drawH - 8, drawW - 40, 20);

            drawProgramDiver(g2, baseX, baseY + floatY, drawW, drawH);
        }

        private void drawProgramDiver(Graphics2D g2, int x, int y, int w, int h) {
            g2.setStroke(new BasicStroke(3));

            g2.setColor(new Color(20, 45, 65));
            g2.fillRoundRect(x + w / 4, y + h / 3, w / 2, h / 2, 22, 22);

            g2.setColor(new Color(40, 110, 145));
            g2.fillRoundRect(x + w / 4 + 8, y + h / 3 + 10, w / 2 - 16, h / 2 - 20, 18, 18);

            g2.setColor(new Color(30, 45, 60));
            g2.fillOval(x + w / 4, y + 20, w / 2, h / 3);

            g2.setColor(new Color(120, 220, 255));
            g2.fillOval(x + w / 3, y + 42, w / 3, h / 7);

            int glowAlpha = 65 + (int) (Math.sin(animTick * 0.12) * 40);
            glowAlpha = Math.max(30, Math.min(120, glowAlpha));
            g2.setColor(new Color(120, 220, 255, glowAlpha));
            g2.fillOval(x + w / 3 - 10, y + 34, w / 3 + 20, h / 7 + 20);

            g2.setColor(new Color(20, 35, 48));
            g2.fillRoundRect(x + 20, y + h / 3 + 18, 30, h / 3, 10, 10);
            g2.fillRoundRect(x + w - 50, y + h / 3 + 18, 30, h / 3, 10, 10);

            g2.setColor(new Color(15, 28, 40));
            g2.fillRoundRect(x + w / 3, y + h - 45, 28, 48, 8, 8);
            g2.fillRoundRect(x + w / 2, y + h - 45, 28, 48, 8, 8);

            g2.setColor(new Color(80, 150, 190));
            g2.drawLine(x + w / 2, y + h / 3, x + w / 2, y + h - 10);
        }

        private void drawCurrentMissionText(Graphics2D g2) {
            String current = "目前任務：";
            if (acceptedMission == null) {
                current += "無";
            } else {
                current += acceptedMission.title;
            }

            g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
            g2.setColor(new Color(255, 220, 130));
            g2.drawString(current, 455, 818);
        }

        private void drawScrollbarHint(Graphics2D g2) {
            int x = 1320;
            int y = 210;
            int h = 470;

            g2.setColor(new Color(60, 120, 145, 120));
            g2.fillRoundRect(x, y, 8, h, 8, 8);

            int knobY = y + (int) (Math.sin(animTick * 0.03) * 12) + 40;
            g2.setColor(new Color(180, 230, 255, 180));
            g2.fillRoundRect(x - 3, knobY, 14, 80, 10, 10);
        }

        private void drawMetalPanel(Graphics2D g2, int x, int y, int w, int h, int arc, Color fill) {
            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillRoundRect(x + 8, y + 8, w, h, arc, arc);

            GradientPaint gp = new GradientPaint(x, y, fill.brighter(), x, y + h, fill.darker());
            g2.setPaint(gp);
            g2.fillRoundRect(x, y, w, h, arc, arc);

            g2.setColor(new Color(225, 175, 75));
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(x + 2, y + 2, w - 4, h - 4, arc, arc);

            g2.setColor(new Color(255, 255, 255, 35));
            g2.drawRoundRect(x + 10, y + 10, w - 20, h - 20, arc - 6, arc - 6);
        }
    }

    public static class Mission {
        String title;
        String description;
        String reward;
        String type;
        int targetAmount;
        int currentAmount;
        boolean completed;

        public Mission(String title, String description, String reward, String type, int targetAmount) {
            this.title = title;
            this.description = description;
            this.reward = reward;
            this.type = type;
            this.targetAmount = targetAmount;
            this.currentAmount = 0;
            this.completed = false;
        }
    }
}
