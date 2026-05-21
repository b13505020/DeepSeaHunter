import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.imageio.ImageIO;

public class QuestHallView extends JFrame {

    public QuestHallView() {
        setTitle("Mission Hall - 任務大廳");
        setSize(1600, 900);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        add(new QuestHallPanel());
        setVisible(true);
    }

    class QuestHallPanel extends JPanel implements KeyListener, MouseListener, MouseMotionListener {

        private Image backgroundImg;
        private Image captainImg;
        private Image[] npcImages;

        private Timer timer;
        private int tick = 0;

        // 這三個 Rectangle 只是「隱形點擊區」，不會畫出框框。
        private Rectangle tavernRect = new Rectangle(45, 250, 310, 360);
        private Rectangle captainRect = new Rectangle(675, 342, 250, 292);
        private Rectangle missionBoardRect = new Rectangle(1165, 230, 390, 390);
        private Rectangle closeRect = new Rectangle(1390, 785, 150, 54);

        private boolean nearCaptain = false;
        private boolean boardHovered = false;
        private boolean tavernHovered = false;
        private boolean captainTalking = false;

        private String message = "點左側酒館區域進入酒館，點右側任務板區域開啟任務。";

        private List<HallNpc> npcs = new ArrayList<>();

        public QuestHallPanel() {
            setFocusable(true);
            addKeyListener(this);
            addMouseListener(this);
            addMouseMotionListener(this);

            loadImages();
            createNpcs();

            timer = new Timer(16, e -> {
                tick++;
                updateNpcs();
                MissionBoardView.autoCheckMissionProgress();
                repaint();
            });
            timer.start();

            SwingUtilities.invokeLater(() -> requestFocusInWindow());
        }

        private void loadImages() {
            backgroundImg = loadImage("assets/mission_hall_bg.png");
            captainImg = loadImage("assets/mission_captain.png");

            npcImages = new Image[24];
            for (int i = 0; i < npcImages.length; i++) {
                npcImages[i] = loadImage("assets/mission_npc_" + i + ".png");
            }
        }

        private Image loadImage(String path) {
            try {
                File file = new File(path);
                if (!file.exists()) {
                    return null;
                }
                return ImageIO.read(file);
            } catch (Exception e) {
                return null;
            }
        }

        private void createNpcs() {
            npcs.clear();

            // 修正版：只使用完整身體的 NPC 素材，不再使用切到半身的走路展示圖。
            // 中間任務官不動，只修正路人 NPC。
            npcs.add(new HallNpc(0, 345, 646, 52, 78, 0, 0.24, 300, 455, "酒館那邊很熱鬧。"));
            npcs.add(new HallNpc(1, 455, 715, 54, 80, 35, -0.22, 390, 555, "先接任務再下海。"));
            npcs.add(new HallNpc(10, 555, 615, 50, 76, 70, 0.2, 510, 650, "完成任務會有獎金。"));
            npcs.add(new HallNpc(11, 990, 640, 52, 78, 105, -0.24, 920, 1065, "任務公告板在右邊。"));
            npcs.add(new HallNpc(12, 1080, 718, 54, 80, 140, 0.22, 1015, 1160, "今天的委託很多。"));
            npcs.add(new HallNpc(13, 1210, 700, 52, 78, 175, -0.2, 1140, 1285, "深海區要小心氧氣。"));
            npcs.add(new HallNpc(14, 625, 724, 50, 76, 210, 0.18, 575, 700, "任務官在中間。"));
            npcs.add(new HallNpc(15, 915, 724, 50, 76, 245, -0.18, 850, 980, "公告板可以接任務。"));
        }

        private void updateNpcs() {
            for (HallNpc npc : npcs) {
                npc.x += npc.vx;

                if (npc.x < npc.minX) {
                    npc.x = npc.minX;
                    npc.vx = Math.abs(npc.vx);
                    npc.facingRight = true;
                }

                if (npc.x > npc.maxX) {
                    npc.x = npc.maxX;
                    npc.vx = -Math.abs(npc.vx);
                    npc.facingRight = false;
                }

                npc.walkFrame = (tick / 12 + npc.phase) % 4;

                npc.talkTimer--;
                if (npc.talkTimer <= 0) {
                    npc.showBubble = !npc.showBubble;
                    npc.talkTimer = 170 + (npc.phase % 150);
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            drawBackground(g2);
            // 依照 y 座標排序，讓靠下的 NPC 畫在前面，空間感比較自然。
            List<HallNpc> sorted = new ArrayList<>(npcs);
            sorted.sort(Comparator.comparingDouble(npc -> npc.y));
            drawMovingNpcs(g2, sorted);

            drawCaptain(g2);
            drawHighlights(g2);
            drawCaptainSpeech(g2);
            drawBottomPrompt(g2);
            drawCloseButton(g2);
            drawMessage(g2);

            g2.dispose();
        }

        private void drawBackground(Graphics2D g2) {
            if (backgroundImg != null) {
                g2.drawImage(backgroundImg, 0, 0, getWidth(), getHeight(), this);
                return;
            }

            g2.setColor(new Color(8, 18, 28));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        private void drawHighlights(Graphics2D g2) {
            // 不畫任何灰色面板、酒館框、任務框或標籤。
            // 左右兩側只保留隱形點擊區，畫面完全交給底圖呈現。
        }

        private void drawCaptain(Graphics2D g2) {
            int x = captainRect.x;
            int y = captainRect.y + (int) (Math.sin(tick * 0.04) * 2);
            int w = captainRect.width;
            int h = captainRect.height;

            g2.setColor(new Color(0, 0, 0, 85));
            g2.fillOval(x + 34, y + h - 18, w - 68, 26);

            if (captainImg != null) {
                g2.drawImage(captainImg, x, y, w, h, this);
            }
        }

        private void drawCaptainSpeech(Graphics2D g2) {
            if (!captainTalking && !nearCaptain) {
                return;
            }

            drawPanel(g2, 530, 138, 540, 110);

            g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
            g2.setColor(new Color(255, 220, 130));
            g2.drawString("任務接取人", 562, 174);

            g2.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 18));
            g2.setColor(new Color(230, 245, 245));

            if (captainTalking) {
                g2.drawString(getCaptainLine(), 562, 210);
            } else {
                g2.drawString("按 ENTER 與我對話，點右側任務公告板接任務。", 562, 210);
            }
        }

        private String getCaptainLine() {
            String[] lines = {
                "點右側任務公告板，就能打開任務清單。",
                "接了任務之後，只要條件達成就會自動完成。",
                "完成任務會自動發放金幣。",
                "左側酒館入口之後可以做成情報與支線任務區。"
            };

            return lines[(tick / 150) % lines.length];
        }

        private void drawMovingNpcs(Graphics2D g2, List<HallNpc> sortedNpcs) {
            for (HallNpc npc : sortedNpcs) {
                int x = (int) npc.x;
                int y = (int) npc.y + (int) (Math.sin((tick + npc.phase) * 0.05) * 2);

                drawNpcImage(g2, npc, x, y);

                if (npc.showBubble) {
                    drawNpcBubble(g2, x - 25, y - 50, npc.line);
                }
            }
        }

        private void drawNpcImage(Graphics2D g2, HallNpc npc, int x, int y) {
            int bob = (npc.walkFrame % 2 == 0) ? 0 : -2;

            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillOval(x + 6, y + npc.h - 8, npc.w - 12, 10);

            Image img = null;
            if (npcImages != null && npc.imageIndex >= 0 && npc.imageIndex < npcImages.length) {
                img = npcImages[npc.imageIndex];
            }

            if (img != null && !shouldUseFallbackNpcImage(img)) {
                if (npc.facingRight) {
                    g2.drawImage(img, x, y + bob, npc.w, npc.h, this);
                } else {
                    g2.drawImage(img, x + npc.w, y + bob, -npc.w, npc.h, this);
                }
            } else {
                drawFallbackFullBodyNpc(g2, x, y + bob, npc.w, npc.h, npc.phase);
            }
        }


        private boolean shouldUseFallbackNpcImage(Image img) {
            if (img == null) {
                return true;
            }

            int iw = img.getWidth(this);
            int ih = img.getHeight(this);

            if (iw <= 0 || ih <= 0) {
                return true;
            }

            // 如果圖片太扁，通常代表是半身或切壞的素材。
            return ih < iw * 1.2;
        }

        private void drawFallbackFullBodyNpc(Graphics2D g2, int x, int y, int w, int h, int phase) {
            Color coat;
            if (phase % 4 == 0) {
                coat = new Color(40, 85, 105);
            } else if (phase % 4 == 1) {
                coat = new Color(95, 70, 45);
            } else if (phase % 4 == 2) {
                coat = new Color(60, 100, 70);
            } else {
                coat = new Color(85, 70, 115);
            }

            Color skin = new Color(210, 165, 115);
            Color hair = phase % 2 == 0 ? new Color(45, 32, 24) : new Color(95, 60, 35);

            int cx = x + w / 2;

            // 頭
            g2.setColor(skin);
            g2.fillOval(cx - w / 5, y + 6, w * 2 / 5, h / 4);

            // 頭髮
            g2.setColor(hair);
            g2.fillArc(cx - w / 5 - 2, y + 3, w * 2 / 5 + 4, h / 5, 0, 180);

            // 身體
            g2.setColor(coat);
            g2.fillRoundRect(cx - w / 4, y + h / 3, w / 2, h / 3, 10, 10);

            // 手
            g2.setColor(skin);
            g2.fillRoundRect(cx - w / 3, y + h / 3 + 5, w / 8, h / 4, 8, 8);
            g2.fillRoundRect(cx + w / 4, y + h / 3 + 5, w / 8, h / 4, 8, 8);

            // 腿
            g2.setColor(new Color(35, 35, 42));
            g2.fillRoundRect(cx - w / 5, y + h * 2 / 3, w / 7, h / 4, 6, 6);
            g2.fillRoundRect(cx + w / 18, y + h * 2 / 3, w / 7, h / 4, 6, 6);

            // 腳
            g2.setColor(new Color(25, 22, 20));
            g2.fillRoundRect(cx - w / 5 - 2, y + h - 8, w / 5, 6, 5, 5);
            g2.fillRoundRect(cx + w / 18 - 2, y + h - 8, w / 5, 6, 5, 5);
        }

        private void drawNpcBubble(Graphics2D g2, int x, int y, String text) {
            g2.setColor(new Color(0, 0, 0, 130));
            g2.fillRoundRect(x + 4, y + 4, 230, 46, 14, 14);

            g2.setColor(new Color(240, 235, 220, 235));
            g2.fillRoundRect(x, y, 230, 46, 14, 14);

            g2.setColor(new Color(85, 60, 35));
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(x, y, 230, 46, 14, 14);

            g2.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));
            g2.setColor(new Color(35, 28, 22));
            g2.drawString(text, x + 12, y + 28);
        }

        private void drawBottomPrompt(Graphics2D g2) {
            drawPanel(g2, 480, 762, 640, 70);
            g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 22));
            g2.setColor(new Color(255, 230, 160));

            if (tavernHovered) {
                g2.drawString("點左側酒館區域｜點右側任務板區域｜ESC 離開", 555, 805);
            } else if (boardHovered) {
                g2.drawString("點左側酒館區域｜點右側任務板區域｜ESC 離開", 555, 805);
            } else if (nearCaptain) {
                g2.drawString("點左側酒館區域｜點右側任務板區域｜ESC 離開", 555, 805);
            } else {
                g2.drawString("點左側酒館區域｜點右側任務板區域｜ESC 離開", 555, 805);
            }
        }

        private void drawCloseButton(Graphics2D g2) {
            drawButton(g2, closeRect, "離開大廳");
        }

        private void drawMessage(Graphics2D g2) {
            g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 15));
            g2.setColor(new Color(255, 235, 170));
            g2.drawString(message, 55, 845);
        }

        private void drawPanel(Graphics2D g2, int x, int y, int w, int h) {
            g2.setColor(new Color(0, 0, 0, 145));
            g2.fillRoundRect(x + 7, y + 7, w, h, 20, 20);

            g2.setPaint(new GradientPaint(x, y, new Color(12, 46, 60, 232), x, y + h, new Color(4, 18, 30, 232)));
            g2.fillRoundRect(x, y, w, h, 20, 20);

            g2.setColor(new Color(210, 145, 58));
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(x + 2, y + 2, w - 4, h - 4, 20, 20);
        }

        private void drawButton(Graphics2D g2, Rectangle rect, String text) {
            Point mouse = getMousePosition();
            boolean hover = mouse != null && rect.contains(mouse);

            g2.setPaint(new GradientPaint(
                rect.x,
                rect.y,
                hover ? new Color(45, 110, 135) : new Color(25, 75, 95),
                rect.x,
                rect.y + rect.height,
                hover ? new Color(10, 55, 75) : new Color(5, 35, 50)
            ));

            g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 14, 14);

            g2.setColor(new Color(235, 185, 75));
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(rect.x + 2, rect.y + 2, rect.width - 4, rect.height - 4, 14, 14);

            g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 20));
            g2.setColor(new Color(255, 235, 170));
            FontMetrics fm = g2.getFontMetrics();
            int tx = rect.x + rect.width / 2 - fm.stringWidth(text) / 2;
            int ty = rect.y + rect.height / 2 + fm.getAscent() / 2 - 4;
            g2.drawString(text, tx, ty);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            Point p = e.getPoint();
            tavernHovered = tavernRect.contains(p);
            nearCaptain = captainRect.contains(p);
            boardHovered = missionBoardRect.contains(p);

            if (tavernHovered || nearCaptain || boardHovered || closeRect.contains(p)) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else {
                setCursor(Cursor.getDefaultCursor());
            }

            repaint();
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            Point p = e.getPoint();

            if (closeRect.contains(p)) {
                dispose();
                return;
            }

            if (missionBoardRect.contains(p)) {
                openOrFocusWindow(MissionBoardView.class);
                message = "已開啟任務公告板。";
                repaint();
                return;
            }

            if (tavernRect.contains(p)) {
                openOrFocusWindow(TavernView.class);
                message = "已開啟酒館補給。";
                repaint();
                return;
            }

            requestFocusInWindow();
        }

        private void openOrFocusWindow(Class<?> windowClass) {
            for (Window window : Window.getWindows()) {
                if (windowClass.isInstance(window) && window.isDisplayable()) {
                    window.toFront();
                    window.requestFocus();
                    return;
                }
            }

            if (windowClass == TavernView.class) {
                new TavernView();
            } else if (windowClass == MissionBoardView.class) {
                new MissionBoardView();
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                dispose();
                return;
            }

            if (e.getKeyCode() == KeyEvent.VK_T) {
                openOrFocusWindow(TavernView.class);
                message = "已開啟酒館補給。";
                repaint();
                return;
            }

            if (e.getKeyCode() == KeyEvent.VK_M) {
                openOrFocusWindow(MissionBoardView.class);
                message = "已開啟任務公告板。";
                repaint();
                return;
            }

            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                if (nearCaptain) {
                    captainTalking = !captainTalking;
                    message = captainTalking ? "任務接取人正在說話。" : "任務接取人對話已關閉。";
                    repaint();
                }
            }
        }

        @Override public void mouseDragged(MouseEvent e) {}
        @Override public void mousePressed(MouseEvent e) {}
        @Override public void mouseReleased(MouseEvent e) {}
        @Override public void mouseEntered(MouseEvent e) {}
        @Override public void mouseExited(MouseEvent e) {}
        @Override public void keyReleased(KeyEvent e) {}
        @Override public void keyTyped(KeyEvent e) {}
    }

    static class HallNpc {
        int imageIndex;
        double x;
        double y;
        int w;
        int h;
        int phase;
        double vx;
        double minX;
        double maxX;
        boolean facingRight = true;
        int walkFrame = 0;
        String line;
        boolean showBubble = false;
        int talkTimer;

        public HallNpc(int imageIndex, double x, double y, int w, int h, int phase, double vx, double minX, double maxX, String line) {
            this.imageIndex = imageIndex;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.phase = phase;
            this.vx = vx;
            this.line = line;
            this.talkTimer = 90 + phase;
            this.minX = minX;
            this.maxX = maxX;
            this.facingRight = vx >= 0;
        }
    }
}
