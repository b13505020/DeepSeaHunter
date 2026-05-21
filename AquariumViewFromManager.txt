import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AquariumView extends JFrame {

    public AquariumView() {
        setTitle("Aquarium - 水族館");
        setSize(1200, 720);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        add(new AquariumPanel());
        setVisible(true);
    }

    class AquariumPanel extends JPanel implements Runnable, KeyListener {

        private List<DisplayFish> displayFishList = new ArrayList<>();
        private int tick = 0;

        public AquariumPanel() {
            setFocusable(true);
            addKeyListener(this);
            reloadFishFromAquarium();
            new Thread(this).start();
            SwingUtilities.invokeLater(() -> requestFocusInWindow());
        }

        private void reloadFishFromAquarium() {
            displayFishList.clear();

            List<Fish> aquariumFish = AquariumManager.getAquariumFish();

            for (int i = 0; i < aquariumFish.size(); i++) {
                Fish fish = aquariumFish.get(i);

                double x = 120 + (i * 130) % 650;
                double y = 170 + (i * 85) % 350;
                double speed = 0.8 + (i % 4) * 0.25;

                displayFishList.add(new DisplayFish(fish, x, y, speed));
            }
        }

        @Override
        public void run() {
            while (true) {
                tick++;
                updateFish();
                repaint();

                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void updateFish() {
            for (DisplayFish fish : displayFishList) {
                fish.x += fish.speed * fish.direction;
                fish.y += Math.sin((tick + fish.waveOffset) * 0.04) * 0.35;

                if (fish.x < 90) {
                    fish.x = 90;
                    fish.direction = 1;
                }

                if (fish.x > 780) {
                    fish.x = 780;
                    fish.direction = -1;
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            drawBackground(g2);
            drawTank(g2);
            drawDecorations(g2);
            drawFish(g2);
            drawInfoPanel(g2);
            drawEmptyHint(g2);

            g2.dispose();
        }

        private void drawBackground(Graphics2D g2) {
            GradientPaint bg = new GradientPaint(
                0,
                0,
                new Color(8, 18, 28),
                0,
                getHeight(),
                new Color(3, 8, 14)
            );
            g2.setPaint(bg);
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(new Color(210, 145, 58));
            g2.setStroke(new BasicStroke(8));
            g2.drawRoundRect(25, 25, getWidth() - 50, getHeight() - 50, 28, 28);
        }

        private void drawTank(Graphics2D g2) {
            int x = 70;
            int y = 95;
            int w = 760;
            int h = 520;

            GradientPaint water = new GradientPaint(
                x,
                y,
                new Color(30, 145, 180, 220),
                x,
                y + h,
                new Color(8, 45, 85, 230)
            );
            g2.setPaint(water);
            g2.fillRoundRect(x, y, w, h, 25, 25);

            g2.setColor(new Color(255, 255, 255, 45));
            g2.fillRoundRect(x + 18, y + 18, w - 36, h - 36, 20, 20);

            g2.setColor(new Color(210, 145, 58));
            g2.setStroke(new BasicStroke(5));
            g2.drawRoundRect(x, y, w, h, 25, 25);

            g2.setColor(new Color(255, 255, 255, 65));
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(x + 35, y + 35, x + w - 50, y + 15);
            g2.drawLine(x + 60, y + 70, x + w - 90, y + 45);
        }

        private void drawDecorations(Graphics2D g2) {
            g2.setColor(new Color(36, 80, 55));
            for (int i = 0; i < 8; i++) {
                int baseX = 110 + i * 85;
                int baseY = 565;
                g2.setStroke(new BasicStroke(5));
                g2.drawLine(baseX, baseY, baseX - 12, baseY - 70 - i % 3 * 12);
                g2.drawLine(baseX, baseY, baseX + 10, baseY - 62 - i % 2 * 16);
            }

            g2.setColor(new Color(120, 80, 55));
            g2.fillOval(170, 565, 80, 35);
            g2.fillOval(510, 575, 100, 30);
            g2.fillOval(665, 560, 70, 28);

            g2.setColor(new Color(250, 120, 100));
            g2.fillOval(250, 540, 18, 18);
            g2.fillOval(270, 530, 14, 14);
            g2.setColor(new Color(180, 80, 210));
            g2.fillOval(610, 535, 20, 20);
            g2.fillOval(635, 548, 15, 15);
        }

        private void drawFish(Graphics2D g2) {
            for (DisplayFish fish : displayFishList) {
                drawOneFish(g2, fish);
            }
        }

        private void drawOneFish(Graphics2D g2, DisplayFish displayFish) {
            Fish fish = displayFish.fish;
            int x = (int) displayFish.x;
            int y = (int) displayFish.y;
            int dir = displayFish.direction;

            ImageIcon icon = new ImageIcon(fish.getImagePath());

            g2.setColor(new Color(0, 0, 0, 60));
            g2.fillOval(x - 6, y + 38, 72, 16);

            if (icon.getIconWidth() > 0) {
                Image img = icon.getImage();

                if (dir > 0) {
                    g2.drawImage(img, x, y, x + 70, y + 55, 0, 0, icon.getIconWidth(), icon.getIconHeight(), this);
                } else {
                    g2.drawImage(img, x + 70, y, x, y + 55, 0, 0, icon.getIconWidth(), icon.getIconHeight(), this);
                }
            } else {
                drawFallbackFish(g2, x, y, dir, fish.getName());
            }
        }

        private void drawFallbackFish(Graphics2D g2, int x, int y, int dir, String name) {
            Color color = getColorByName(name);

            g2.setColor(color);
            g2.fillOval(x, y + 10, 62, 32);

            Polygon tail = new Polygon();
            if (dir > 0) {
                tail.addPoint(x - 10, y + 26);
                tail.addPoint(x - 32, y + 12);
                tail.addPoint(x - 32, y + 40);
            } else {
                tail.addPoint(x + 72, y + 26);
                tail.addPoint(x + 94, y + 12);
                tail.addPoint(x + 94, y + 40);
            }
            g2.fillPolygon(tail);

            g2.setColor(new Color(255, 255, 255, 180));
            if (dir > 0) {
                g2.fillOval(x + 45, y + 19, 8, 8);
                g2.setColor(Color.BLACK);
                g2.fillOval(x + 48, y + 21, 3, 3);
            } else {
                g2.fillOval(x + 9, y + 19, 8, 8);
                g2.setColor(Color.BLACK);
                g2.fillOval(x + 11, y + 21, 3, 3);
            }
        }

        private Color getColorByName(String name) {
            if (name.contains("小丑")) {
                return new Color(255, 150, 55);
            }
            if (name.contains("藍")) {
                return new Color(70, 160, 255);
            }
            if (name.contains("蝶")) {
                return new Color(255, 215, 80);
            }
            if (name.contains("沙丁")) {
                return new Color(170, 220, 240);
            }
            return new Color(130, 220, 200);
        }

        private void drawInfoPanel(Graphics2D g2) {
            int x = 870;
            int y = 95;
            int w = 260;
            int h = 520;

            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillRoundRect(x + 6, y + 6, w, h, 22, 22);

            GradientPaint panel = new GradientPaint(
                x,
                y,
                new Color(15, 50, 65, 235),
                x,
                y + h,
                new Color(7, 23, 35, 235)
            );
            g2.setPaint(panel);
            g2.fillRoundRect(x, y, w, h, 22, 22);

            g2.setColor(new Color(210, 145, 58));
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(x + 2, y + 2, w - 4, h - 4, 22, 22);

            g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 28));
            g2.setColor(new Color(255, 220, 130));
            g2.drawString("水族館", x + 38, y + 55);

            g2.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
            g2.setColor(new Color(185, 235, 245));
            g2.drawString("展示從背包放入的魚。", x + 28, y + 95);
            g2.drawString("不會賣掉，也不會換錢。", x + 28, y + 120);

            g2.setColor(new Color(255, 220, 130));
            g2.drawString("操作：", x + 28, y + 175);

            g2.setColor(new Color(220, 235, 235));
            g2.drawString("ESC：關閉水族館", x + 28, y + 205);

            g2.setColor(new Color(110, 230, 255));
            g2.drawString("目前館藏：" + AquariumManager.getTotalCount() + " 隻", x + 28, y + 260);
            g2.drawString("館藏價值：$" + AquariumManager.getTotalValue(), x + 28, y + 290);

            drawFishSummary(g2, x + 28, y + 335);
        }

        private void drawFishSummary(Graphics2D g2, int startX, int startY) {
            HashMap<String, Integer> counts = new HashMap<>();

            for (Fish fish : AquariumManager.getAquariumFish()) {
                counts.put(fish.getName(), counts.getOrDefault(fish.getName(), 0) + 1);
            }

            g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
            g2.setColor(new Color(255, 220, 130));
            g2.drawString("魚種清單：", startX, startY);

            g2.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));
            g2.setColor(new Color(235, 245, 245));

            int y = startY + 30;
            int shown = 0;

            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                if (shown >= 6) {
                    g2.drawString("……", startX, y);
                    break;
                }

                g2.drawString(entry.getKey() + " x" + entry.getValue(), startX, y);
                y += 25;
                shown++;
            }
        }

        private void drawEmptyHint(Graphics2D g2) {
            if (!displayFishList.isEmpty()) {
                return;
            }

            g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 28));
            g2.setColor(new Color(255, 245, 210));
            g2.drawString("目前水族館還沒有魚", 280, 330);

            g2.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 18));
            g2.setColor(new Color(220, 240, 245));
            g2.drawString("先去 Ocean 抓魚，再從 Backpack 按「放入水族館」。", 210, 370);
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                dispose();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }
    }

    static class DisplayFish {
        Fish fish;
        double x;
        double y;
        double speed;
        int direction = 1;
        int waveOffset;

        public DisplayFish(Fish fish, double x, double y, double speed) {
            this.fish = fish;
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.waveOffset = (int) (Math.random() * 100);
        }
    }
}
