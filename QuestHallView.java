import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class QuestHallView extends JFrame {

    public QuestHallView() {
        setTitle("Headquarters - Mission Hall");
        setSize(1600, 900);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        add(new QuestHallPanel());

        setVisible(true);
    }

    class QuestHallPanel extends JPanel implements KeyListener, Runnable {

        private BufferedImage background;
        private BufferedImage diverSheet;
        private BufferedImage npcSheet;

        private double playerX = 180;
        private double playerY = 620;

        private final int PLAYER_WIDTH = 110;
        private final int PLAYER_HEIGHT = 135;
        private final double PLAYER_SPEED = 6.0;

        private boolean left;
        private boolean right;
        private boolean up;
        private boolean down;
        private boolean playerFacingLeft = false;

        // NPC 位置
        private double npcX = 930;
        private double npcY = 555;

        private final int NPC_WIDTH = 100;
        private final int NPC_HEIGHT = 135;
        private final double NPC_SPEED = 1.2;

        // NPC 不再到處亂走，只在任務櫃台附近自然巡邏
        private double npcLeftLimit = 850;
        private double npcRightLimit = 1080;
        private boolean npcMovingRight = true;

        // 動畫控制
        private int npcAnimTick = 0;
        private int npcFrame = 0;
        private int npcFrameCounter = 0;

        public QuestHallPanel() {
            setLayout(null);
            setFocusable(true);
            addKeyListener(this);

            loadImages();
            setupButtons();

            new Thread(this).start();

            SwingUtilities.invokeLater(() -> requestFocusInWindow());
        }

        private void loadImages() {
            try {
                background = ImageIO.read(new File("assets/quest_hall.png"));
            } catch (Exception e) {
                System.out.println("Cannot load assets/quest_hall.png");
            }

            try {
                diverSheet = ImageIO.read(new File("assets/diver_clean.png"));
            } catch (Exception e) {
                System.out.println("Cannot load assets/diver_clean.png");
            }

            try {
                npcSheet = ImageIO.read(new File("assets/quest_npc.png"));
            } catch (Exception e) {
                System.out.println("Cannot load assets/quest_npc.png");
            }
        }

        private void setupButtons() {
            JButton closeBtn = new JButton("Back");
            closeBtn.setBounds(20, 20, 100, 35);
            closeBtn.setFocusable(false);
            closeBtn.addActionListener(e -> dispose());
            add(closeBtn);
        }

        @Override
        public void run() {
            while (true) {
                updatePlayerMovement();
                updateNpcMovement();
                repaint();

                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void updatePlayerMovement() {
            double dx = 0;
            double dy = 0;

            if (left) {
                dx -= 1;
                playerFacingLeft = true;
            }

            if (right) {
                dx += 1;
                playerFacingLeft = false;
            }

            if (up) {
                dy -= 1;
            }

            if (down) {
                dy += 1;
            }

            if (dx != 0 || dy != 0) {
                double length = Math.sqrt(dx * dx + dy * dy);
                dx /= length;
                dy /= length;

                playerX += dx * PLAYER_SPEED;
                playerY += dy * PLAYER_SPEED;
            }

            playerX = Math.max(0, Math.min(playerX, 1600 - PLAYER_WIDTH));
            playerY = Math.max(0, Math.min(playerY, 900 - PLAYER_HEIGHT));
        }

        private void updateNpcMovement() {
            npcAnimTick++;

            // 只做左右慢慢巡邏，比原本繞大圈自然很多
            if (npcMovingRight) {
                npcX += NPC_SPEED;
                if (npcX >= npcRightLimit) {
                    npcMovingRight = false;
                }
            } else {
                npcX -= NPC_SPEED;
                if (npcX <= npcLeftLimit) {
                    npcMovingRight = true;
                }
            }

            // 走路幀不要跑太快，避免看起來抽搐
            npcFrameCounter++;
            if (npcFrameCounter % 12 == 0) {
                npcFrame = (npcFrame + 1) % 4;
            }
        }

        private boolean isNearNpc() {
            double playerCenterX = playerX + PLAYER_WIDTH / 2.0;
            double playerCenterY = playerY + PLAYER_HEIGHT / 2.0;

            double npcCenterX = npcX + NPC_WIDTH / 2.0;
            double npcCenterY = npcY + NPC_HEIGHT / 2.0;

            double dx = playerCenterX - npcCenterX;
            double dy = playerCenterY - npcCenterY;
            double distance = Math.sqrt(dx * dx + dy * dy);

            // 判定範圍放大，避免玩家明明靠近卻按不到
            return distance < 190;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            drawBackground(g);
            drawNpc(g);
            drawPlayer(g);
            drawPrompt(g);
        }

        private void drawBackground(Graphics g) {
            if (background != null) {
                g.drawImage(background, 0, 0, 1600, 900, this);
            } else {
                g.setColor(new Color(35, 35, 45));
                g.fillRect(0, 0, 1600, 900);

                g.setColor(Color.WHITE);
                g.setFont(new Font("Serif", Font.BOLD, 50));
                g.drawString("MISSION HALL", 580, 120);
            }
        }

        private void drawNpc(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            int drawX = (int) npcX;
            int drawY = (int) npcY;

            // 輕微上下浮動，像呼吸，不會像亂跳
            int floatY = (int) (Math.sin(npcAnimTick * 0.08) * 3);

            if (npcSheet != null) {
                int sheetW = npcSheet.getWidth();
                int sheetH = npcSheet.getHeight();

                // 假設 NPC 圖是 4 欄 x 4 列 sprite sheet
                // 但只用第 0 列，避免上下左右列不一致造成走路怪
                int frameWidth = sheetW / 4;
                int frameHeight = sheetH / 4;

                int srcX1 = npcFrame * frameWidth;
                int srcY1 = 0;
                int srcX2 = srcX1 + frameWidth;
                int srcY2 = srcY1 + frameHeight;

                if (npcMovingRight) {
                    g2.drawImage(
                        npcSheet,
                        drawX,
                        drawY + floatY,
                        drawX + NPC_WIDTH,
                        drawY + floatY + NPC_HEIGHT,
                        srcX1,
                        srcY1,
                        srcX2,
                        srcY2,
                        this
                    );
                } else {
                    // 往左走時直接水平翻轉，不用切到另一列，避免方向錯亂
                    g2.drawImage(
                        npcSheet,
                        drawX + NPC_WIDTH,
                        drawY + floatY,
                        drawX,
                        drawY + floatY + NPC_HEIGHT,
                        srcX1,
                        srcY1,
                        srcX2,
                        srcY2,
                        this
                    );
                }
            } else {
                g2.setColor(new Color(180, 120, 60));
                g2.fillRoundRect(drawX, drawY + floatY, NPC_WIDTH, NPC_HEIGHT, 20, 20);

                g2.setColor(Color.WHITE);
                g2.drawString("Quest NPC", drawX, drawY - 10 + floatY);
            }

            // NPC 腳下陰影，讓他比較站在地上
            g2.setColor(new Color(0, 0, 0, 90));
            g2.fillOval(drawX + 15, drawY + NPC_HEIGHT - 5, NPC_WIDTH - 30, 18);

            g2.dispose();
        }

        private void drawPlayer(Graphics g) {
            int sx = (int) playerX;
            int sy = (int) playerY;

            if (diverSheet != null) {
                if (playerFacingLeft) {
                    g.drawImage(
                        diverSheet,
                        sx + PLAYER_WIDTH,
                        sy,
                        sx,
                        sy + PLAYER_HEIGHT,
                        0,
                        0,
                        128,
                        140,
                        this
                    );
                } else {
                    g.drawImage(
                        diverSheet,
                        sx,
                        sy,
                        sx + PLAYER_WIDTH,
                        sy + PLAYER_HEIGHT,
                        0,
                        0,
                        128,
                        140,
                        this
                    );
                }
            } else {
                g.setColor(Color.ORANGE);
                g.fillRect(sx, sy, PLAYER_WIDTH, PLAYER_HEIGHT);
            }
        }

        private void drawPrompt(Graphics g) {
            g.setFont(new Font("Monospaced", Font.BOLD, 18));
            g.setColor(Color.CYAN);
            g.drawString("Move: WASD / Arrow Keys", 20, 80);
            g.drawString("ESC: Back", 20, 105);

            if (isNearNpc()) {
                g.setColor(new Color(0, 0, 0, 170));
                g.fillRoundRect((int) playerX - 60, (int) playerY - 55, 430, 38, 12, 12);

                g.setColor(Color.WHITE);
                g.setFont(new Font("Monospaced", Font.BOLD, 22));
                g.drawString("Press ENTER to open missions", (int) playerX - 45, (int) playerY - 28);
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int code = e.getKeyCode();

            if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) {
                left = true;
            }

            if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) {
                right = true;
            }

            if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
                up = true;
            }

            if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) {
                down = true;
            }

            if (code == KeyEvent.VK_ENTER) {
                if (isNearNpc()) {
                    new MissionBoardView();
                }
            }

            if (code == KeyEvent.VK_ESCAPE) {
                dispose();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            int code = e.getKeyCode();

            if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) {
                left = false;
            }

            if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) {
                right = false;
            }

            if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
                up = false;
            }

            if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) {
                down = false;
            }
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }
    }
}
