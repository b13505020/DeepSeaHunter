import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class MissionBoardView extends JFrame {

    // 目前已接取的任務，之後 OceanWorld 或其他 class 可以讀這個
    private static Mission acceptedMission = null;

    public MissionBoardView() {
        setTitle("任務大廳 - Mission Board");
        setSize(1600, 900);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        add(new MissionBoardPanel());

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

        private BufferedImage background;
        private BufferedImage npcSheet;
        private BufferedImage diverSheet;

        private List<Mission> missions = new ArrayList<>();
        private Mission selectedMission = null;
        private JPanel missionContainer;
        private List<JPanel> missionCards = new ArrayList<>();

        private int animTick = 0;
        private Timer animationTimer;

        public MissionBoardPanel() {
            setLayout(null);
            setFocusable(true);

            loadImages();
            createMissions();
            setupMissionScrollList();
            setupBottomButtons();
            setupAnimation();

            SwingUtilities.invokeLater(() -> requestFocusInWindow());
        }

        private void loadImages() {
            try {
                background = ImageIO.read(new File("assets/mission_board.png"));
            } catch (Exception e) {
                System.out.println("Cannot load assets/mission_board.png");
            }

            try {
                npcSheet = ImageIO.read(new File("assets/quest_npc.png"));
            } catch (Exception e) {
                System.out.println("Cannot load assets/quest_npc.png");
            }

            try {
                diverSheet = ImageIO.read(new File("assets/diver_clean.png"));
            } catch (Exception e) {
                System.out.println("Cannot load assets/diver_clean.png");
            }
        }

        private void createMissions() {
            missions.add(new Mission(
                "捕捉沙丁魚",
                "前往淺海區捕捉 3 隻沙丁魚，作為新手潛水員的基礎訓練。",
                "金幣 800，工域點數 40",
                "CATCH_SARDINE",
                3
            ));

            missions.add(new Mission(
                "下潛到 500m",
                "進入海洋地圖並成功下潛到 500m 深度，確認潛水衣運作狀態。",
                "金幣 1200，工域點數 60",
                "REACH_DEPTH_500",
                500
            ));

            missions.add(new Mission(
                "帶回小丑魚",
                "捕捉並帶回 1 隻小丑魚，交給水族館進行觀察。",
                "金幣 1500，工域點數 80",
                "CATCH_CLOWNFISH",
                1
            ));

            missions.add(new Mission(
                "擊倒綠鰻魚",
                "深海區出現具攻擊性的綠鰻魚，請擊倒 1 隻並安全回收。",
                "金幣 2500，工域點數 150",
                "DEFEAT_GREEN_EEL",
                1
            ));

            missions.add(new Mission(
                "收集五種生物",
                "完成一次探索，捕捉並帶回 5 種不同海洋生物。",
                "金幣 3000，工域點數 180",
                "COLLECT_5_SPECIES",
                5
            ));

            missions.add(new Mission(
                "探索深海基地",
                "下潛至深海基地附近，完成區域偵查後安全返回。",
                "金幣 3500，工域點數 220",
                "EXPLORE_DEEP_BASE",
                1
            ));

            missions.add(new Mission(
                "高價魚獲回收",
                "單次下潛帶回總價值 3000 以上的魚獲。",
                "金幣 2200，工域點數 120",
                "RETURN_VALUE_3000",
                3000
            ));

            missions.add(new Mission(
                "安全返航訓練",
                "完成一次下潛任務，並成功回到船上或陸地基地。",
                "金幣 1000，工域點數 50",
                "SAFE_RETURN",
                1
            ));
        }

        private void setupMissionScrollList() {
            missionContainer = new JPanel();
            missionContainer.setLayout(new BoxLayout(missionContainer, BoxLayout.Y_AXIS));
            missionContainer.setOpaque(false);
            missionContainer.setBorder(new EmptyBorder(8, 8, 8, 8));

            for (Mission mission : missions) {
                JPanel card = createMissionCard(mission);
                missionCards.add(card);
                missionContainer.add(card);
                missionContainer.add(Box.createVerticalStrut(10));
            }

            JScrollPane scrollPane = new JScrollPane(missionContainer);

            // 這裡是右邊任務列表的位置，可以依照你的背景圖微調
            scrollPane.setBounds(535, 190, 770, 500);
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());

            scrollPane.getVerticalScrollBar().setUnitIncrement(20);
            scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

            add(scrollPane);
        }

        private JPanel createMissionCard(Mission mission) {
            JPanel card = new JPanel();
            card.setLayout(new BorderLayout());
            card.setMaximumSize(new Dimension(730, 105));
            card.setPreferredSize(new Dimension(730, 105));
            card.setBackground(new Color(5, 22, 34, 215));
            card.setBorder(BorderFactory.createLineBorder(new Color(120, 95, 45), 2));
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel icon = new JLabel("⚓", SwingConstants.CENTER);
            icon.setPreferredSize(new Dimension(70, 85));
            icon.setFont(new Font("Serif", Font.BOLD, 38));
            icon.setForeground(new Color(230, 185, 80));

            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false);
            textPanel.setBorder(new EmptyBorder(10, 8, 8, 8));

            JLabel titleLabel = new JLabel(mission.title);
            titleLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 24));
            titleLabel.setForeground(new Color(255, 215, 125));

            JLabel descLabel = new JLabel("<html><body style='width:450px'>" + mission.description + "</body></html>");
            descLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
            descLabel.setForeground(new Color(220, 230, 230));

            JLabel rewardLabel = new JLabel("獎勵：" + mission.reward);
            rewardLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
            rewardLabel.setForeground(new Color(100, 220, 255));

            textPanel.add(titleLabel);
            textPanel.add(Box.createVerticalStrut(6));
            textPanel.add(descLabel);
            textPanel.add(Box.createVerticalStrut(6));
            textPanel.add(rewardLabel);

            JLabel statusLabel = new JLabel("點擊選取", SwingConstants.CENTER);
            statusLabel.setPreferredSize(new Dimension(105, 85));
            statusLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
            statusLabel.setForeground(new Color(180, 160, 100));

            card.add(icon, BorderLayout.WEST);
            card.add(textPanel, BorderLayout.CENTER);
            card.add(statusLabel, BorderLayout.EAST);

            card.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectedMission = mission;
                    updateMissionCardStyles();
                }
            });

            return card;
        }

        private void updateMissionCardStyles() {
            for (int i = 0; i < missionCards.size(); i++) {
                JPanel card = missionCards.get(i);
                Mission mission = missions.get(i);

                if (mission == selectedMission) {
                    card.setBackground(new Color(30, 75, 95, 235));
                    card.setBorder(BorderFactory.createLineBorder(new Color(255, 200, 75), 4));
                } else {
                    card.setBackground(new Color(5, 22, 34, 215));
                    card.setBorder(BorderFactory.createLineBorder(new Color(120, 95, 45), 2));
                }
            }

            repaint();
        }

        private void setupBottomButtons() {
            JButton closeBtn = new JButton("關閉");
            closeBtn.setBounds(815, 775, 175, 58);
            closeBtn.setFont(new Font("Microsoft JhengHei", Font.BOLD, 22));
            closeBtn.setFocusPainted(false);
            closeBtn.setBackground(new Color(30, 50, 65));
            closeBtn.setForeground(new Color(255, 230, 160));
            closeBtn.setBorder(BorderFactory.createLineBorder(new Color(180, 135, 55), 3));

            closeBtn.addActionListener(e -> dispose());

            JButton acceptBtn = new JButton("接受選取任務");
            acceptBtn.setBounds(1045, 775, 285, 58);
            acceptBtn.setFont(new Font("Microsoft JhengHei", Font.BOLD, 22));
            acceptBtn.setFocusPainted(false);
            acceptBtn.setBackground(new Color(20, 90, 115));
            acceptBtn.setForeground(new Color(255, 235, 170));
            acceptBtn.setBorder(BorderFactory.createLineBorder(new Color(230, 175, 65), 3));

            acceptBtn.addActionListener(e -> acceptSelectedMission());

            add(closeBtn);
            add(acceptBtn);
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

            if (background != null) {
                g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
            } else {
                drawFallbackBackground(g);
            }

            drawAnimatedNpc(g);
            drawAnimatedDiver(g);
            drawCurrentMissionText(g);
        }

        private void drawFallbackBackground(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            GradientPaint gp = new GradientPaint(
                0,
                0,
                new Color(18, 25, 35),
                0,
                getHeight(),
                new Color(5, 8, 15)
            );
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(new Color(220, 180, 90));
            g.setFont(new Font("Serif", Font.BOLD, 58));
            g.drawString("MISSION BOARD", 560, 90);
        }

        private void drawAnimatedNpc(Graphics g) {
            if (npcSheet == null) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();

            int baseX = 155;
            int baseY = 155;
            int drawW = 150;
            int drawH = 185;

            int floatY = (int) (Math.sin(animTick * 0.10) * 4);
            int swayX = (int) (Math.sin(animTick * 0.05) * 3);

            int frameWidth = npcSheet.getWidth() / 4;
            int frameHeight = npcSheet.getHeight() / 4;
            int frame = (animTick / 10) % 4;

            // 使用第 0 列：正面待機走動感
            int srcX1 = frame * frameWidth;
            int srcY1 = 0;
            int srcX2 = srcX1 + frameWidth;
            int srcY2 = srcY1 + frameHeight;

            g2.drawImage(
                npcSheet,
                baseX + swayX,
                baseY + floatY,
                baseX + swayX + drawW,
                baseY + floatY + drawH,
                srcX1,
                srcY1,
                srcX2,
                srcY2,
                this
            );

            g2.dispose();
        }

        private void drawAnimatedDiver(Graphics g) {
            if (diverSheet == null) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();

            int baseX = 155;
            int baseY = 530;
            int drawW = 165;
            int drawH = 215;

            int floatY = (int) (Math.sin(animTick * 0.08) * 5);
            int frame = (animTick / 12) % 8;

            int frameWidth = diverSheet.getWidth() / 8;
            int frameHeight = diverSheet.getHeight() / 4;

            int srcX1 = frame * frameWidth;
            int srcY1 = 0;
            int srcX2 = srcX1 + frameWidth;
            int srcY2 = srcY1 + frameHeight;

            g2.drawImage(
                diverSheet,
                baseX,
                baseY + floatY,
                baseX + drawW,
                baseY + floatY + drawH,
                srcX1,
                srcY1,
                srcX2,
                srcY2,
                this
            );

            int glowAlpha = 80 + (int) (Math.sin(animTick * 0.12) * 45);
            glowAlpha = Math.max(35, Math.min(140, glowAlpha));

            g2.setColor(new Color(110, 220, 255, glowAlpha));
            g2.fillOval(baseX + 82, baseY + 40 + floatY, 45, 45);

            g2.dispose();
        }

        private void drawCurrentMissionText(Graphics g) {
            g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
            g.setColor(new Color(255, 220, 130));

            String current = "目前任務：";

            if (acceptedMission == null) {
                current += "無";
            } else {
                current += acceptedMission.title;
            }

            g.drawString(current, 535, 850);
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

