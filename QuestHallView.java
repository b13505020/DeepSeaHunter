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

        private double npcX = 1050;
        private double npcY = 520;

        private final int NPC_WIDTH = 90;
        private final int NPC_HEIGHT = 120;
        private final double NPC_SPEED = 2.0;

        private int npcFrame = 0;
        private int npcFrameCounter = 0;
        private int npcDirection = 0;

        private int currentPatrolIndex = 0;

        private Point[] patrolPoints = {
            new Point(360, 560),
            new Point(650, 650),
            new Point(940, 520),
            new Point(1250, 620),
            new Point(850, 430)
        };

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

            closeBtn.addActionListener(e -> {
                dispose();
            });

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
            Point target = patrolPoints[currentPatrolIndex];

            double dx = target.x - npcX;
            double dy = target.y - npcY;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance < 5) {
                currentPatrolIndex = (currentPatrolIndex + 1) % patrolPoints.length;
                return;
            }

            dx /= distance;
            dy /= distance;

            npcX += dx * NPC_SPEED;
            npcY += dy * NPC_SPEED;

            if (Math.abs(dx) > Math.abs(dy)) {
                if (dx > 0) {
                    npcDirection = 2;
                } else {
                    npcDirection = 1;
                }
            } else {
                if (dy > 0) {
                    npcDirection = 0;
                } else {
                    npcDirection = 3;
                }
            }

            npcFrameCounter++;

            if (npcFrameCounter % 10 == 0) {
                npcFrame = (npcFrame + 1) % 4;
            }
        }

        private boolean isNearNpc() {
            Rectangle playerRect = new Rectangle(
                (int) playerX,
                (int) playerY,
                PLAYER_WIDTH,
                PLAYER_HEIGHT
            );

            Rectangle npcRect = new Rectangle(
                (int) npcX,
                (int) npcY,
                NPC_WIDTH,
                NPC_HEIGHT
            );

            return playerRect.intersects(npcRect);
        }

        private void showMissionDialog() {
            String[] missions = {
                "新手任務：捕捉 3 隻沙丁魚",
                "探索任務：下潛到 500m",
                "收集任務：帶回 1 隻小丑魚",
                "危險任務：擊倒 1 隻綠鰻魚"
            };

            String selected = (String) JOptionPane.showInputDialog(
                this,
                "請選擇一個任務：",
                "任務大廳",
                JOptionPane.PLAIN_MESSAGE,
                null,
                missions,
                missions[0]
            );

            if (selected != null) {
                JOptionPane.showMessageDialog(
                    this,
                    "你已接取任務：\n" + selected,
                    "任務已接受",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }

            requestFocusInWindow();
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
            if (npcSheet != null) {
                int frameWidth = npcSheet.getWidth() / 4;
                int frameHeight = npcSheet.getHeight() / 4;

                int srcX1 = npcFrame * frameWidth;
                int srcY1 = npcDirection * frameHeight;
                int srcX2 = srcX1 + frameWidth;
                int srcY2 = srcY1 + frameHeight;

                g.drawImage(
                    npcSheet,
                    (int) npcX,
                    (int) npcY,
                    (int) npcX + NPC_WIDTH,
                    (int) npcY + NPC_HEIGHT,
                    srcX1,
                    srcY1,
                    srcX2,
                    srcY2,
                    this
                );
            } else {
                g.setColor(new Color(180, 120, 60));
                g.fillRoundRect((int) npcX, (int) npcY, NPC_WIDTH, NPC_HEIGHT, 20, 20);

                g.setColor(Color.WHITE);
                g.drawString("Quest NPC", (int) npcX, (int) npcY - 10);
            }
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
