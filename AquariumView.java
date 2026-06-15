import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;

public class AquariumView extends JPanel {

    public static final int SCREEN_WIDTH = 1600;
    public static final int SCREEN_HEIGHT = 900;

    private AquariumPanel aquariumPanel;
    private ActionListener backToLandAction;

    public AquariumView(ActionListener backToLandAction) {
        this.backToLandAction = backToLandAction;

        setLayout(new BorderLayout());
        setFocusable(true);

        aquariumPanel = new AquariumPanel();
        add(aquariumPanel, BorderLayout.CENTER);
    }

    public void stopAquarium() {
        if (aquariumPanel != null) {
            aquariumPanel.stopAnimation();
        }
    }

    class AquariumPanel extends JPanel implements MouseMotionListener, MouseListener, KeyListener {

        private Image backgroundImg;
        private Image guideImg;
        private Image visitor1Img;
        private Image visitor2Img;
        private Image visitor3Img;

        private Timer timer;
        private int tick = 0;

        private List<DisplayFish> displayFishList = new ArrayList<>();
        private List<VisitorNpc> visitors = new ArrayList<>();
        private List<FeedButton> feedButtons = new ArrayList<>();
        private List<FallingFeed> fallingFeeds = new ArrayList<>();

        private DisplayFish hoveredFish = null;
        private boolean guideHovered = false;
        private boolean feedShopVisible = false;
        private String selectedFeedType = "basic";
        private String message = "歡迎來到水族館。點導覽員可以把儲藏箱的魚放進水族館。";

        private Rectangle guideRect = new Rectangle(1085, 515, 120, 170);

        private Rectangle buyButtonRect = new Rectangle(1210, 555, 145, 50);
        private Rectangle feedButtonRect = new Rectangle(1370, 555, 145, 50);
        private Rectangle feedShopCloseRect = new Rectangle(1500, 172, 28, 28);

        // 已調整：往上移，避免跟底部訊息重疊
        private Rectangle exitButtonRect = new Rectangle(55, 730, 170, 52);

        private Rectangle tankRect = new Rectangle(75, 90, 1060, 545);

        public AquariumPanel() {
            setFocusable(true);
            addMouseMotionListener(this);
            addMouseListener(this);
            addKeyListener(this);

            loadImages();
            createDisplayFish();
            createVisitors();
            createFeedButtons();

            timer = new Timer(16, e -> {
                tick++;
                AquariumManager.updatePassiveIncomeSystem();
                updateFallingFeed();
                updateFish();
                repaint();
            });

            timer.start();

            SwingUtilities.invokeLater(() -> requestFocusInWindow());
        }

        public void stopAnimation() {
            if (timer != null) {
                timer.stop();
            }
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

        private Point toGamePoint(Point p) {
            if (p == null) {
                return new Point(-9999, -9999);
            }

            return new Point(
                (int) (p.x / getScaleX()),
                (int) (p.y / getScaleY())
            );
        }

        private void loadImages() {
            backgroundImg = loadImage("assets/aquarium_bg.png");
            guideImg = loadImage("assets/aquarium_guide.png");
            visitor1Img = loadImage("assets/aquarium_visitor1.png");
            visitor2Img = loadImage("assets/aquarium_visitor2.png");
            visitor3Img = loadImage("assets/aquarium_visitor3.png");
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

        private void createDisplayFish() {
            displayFishList.clear();

            List<AquariumManager.AquariumFish> fishList = AquariumManager.getAquariumFishList();

            for (int i = 0; i < fishList.size(); i++) {
                AquariumManager.AquariumFish entry = fishList.get(i);

                double x = tankRect.x + 95 + (i * 135) % 820;
                double y = tankRect.y + 90 + (i * 82) % 365;
                double speed = 0.55 + (i % 5) * 0.18;
                int direction = i % 2 == 0 ? 1 : -1;

                displayFishList.add(new DisplayFish(entry, x, y, speed, direction));
            }
        }

        private void createVisitors() {
            visitors.clear();

            visitors.add(new VisitorNpc(visitor1Img, 75, 628, 74, 126, 12));
            visitors.add(new VisitorNpc(visitor2Img, 168, 606, 63, 110, 55));
            visitors.add(new VisitorNpc(visitor3Img, 250, 638, 82, 136, 98));
            visitors.add(new VisitorNpc(visitor1Img, 365, 612, 69, 118, 141));
            visitors.add(new VisitorNpc(visitor2Img, 455, 648, 88, 142, 184));
            visitors.add(new VisitorNpc(visitor3Img, 570, 604, 60, 106, 227));
            visitors.add(new VisitorNpc(visitor1Img, 655, 632, 78, 130, 270));
            visitors.add(new VisitorNpc(visitor2Img, 755, 616, 70, 118, 313));
            visitors.add(new VisitorNpc(visitor3Img, 850, 642, 84, 138, 356));
            visitors.add(new VisitorNpc(visitor1Img, 960, 610, 64, 112, 399));
        }

        private void createFeedButtons() {
            feedButtons.clear();

            feedButtons.add(new FeedButton("basic", "基礎飼料", "少量恢復飽食度。", 1210, 245, 145, 82));
            feedButtons.add(new FeedButton("premium", "高級飼料", "明顯恢復飽食度。", 1370, 245, 145, 82));
            feedButtons.add(new FeedButton("color", "增色飼料", "中量恢復飽食度。", 1210, 348, 145, 82));
            feedButtons.add(new FeedButton("growth", "成長飼料", "大幅恢復飽食度。", 1370, 348, 145, 82));
        }

        private int getFullness(AquariumManager.AquariumFish entry) {
            return AquariumManager.getFullness(entry);
        }

        private void updateFallingFeed() {
            Iterator<FallingFeed> it = fallingFeeds.iterator();

            while (it.hasNext()) {
                FallingFeed feed = it.next();
                feed.y += feed.speed;
                feed.x += Math.sin((tick + feed.phase) * 0.05) * 0.45;

                if (feed.y > tankRect.y + tankRect.height - 40) {
                    it.remove();
                }
            }
        }

        private void updateFish() {
            for (DisplayFish fish : displayFishList) {
                FallingFeed targetFeed = findNearestFeed(fish);

                if (targetFeed != null) {
                    moveFishTowardFeed(fish, targetFeed);
                } else {
                    swimNormally(fish);
                }

                keepFishInsideTank(fish);
            }

            Iterator<FallingFeed> feedIterator = fallingFeeds.iterator();

            while (feedIterator.hasNext()) {
                FallingFeed feed = feedIterator.next();

                DisplayFish eater = findFishTouchingFeed(feed);

                if (eater != null) {
                    boolean success = AquariumManager.useFeed(feed.feedType, eater.entry);

                    if (success) {
                        message = eater.entry.getFish().getName()
                            + " 吃掉了「"
                            + AquariumManager.getFeedName(feed.feedType)
                            + "」，飽食度上升。";
                    } else {
                        message = "飼料庫存不足，請先購買。";
                    }

                    feedIterator.remove();
                }
            }
        }

        private FallingFeed findNearestFeed(DisplayFish fish) {
            FallingFeed nearest = null;
            double bestDistance = Double.MAX_VALUE;

            for (FallingFeed feed : fallingFeeds) {
                double dx = feed.x - (fish.x + fish.getWidth() / 2.0);
                double dy = feed.y - (fish.y + fish.getHeight() / 2.0);
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance < bestDistance) {
                    bestDistance = distance;
                    nearest = feed;
                }
            }

            if (bestDistance > 380) {
                return null;
            }

            return nearest;
        }

        private void moveFishTowardFeed(DisplayFish fish, FallingFeed feed) {
            double fishCenterX = fish.x + fish.getWidth() / 2.0;
            double fishCenterY = fish.y + fish.getHeight() / 2.0;

            double dx = feed.x - fishCenterX;
            double dy = feed.y - fishCenterY;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance < 1) {
                return;
            }

            double chaseSpeed = fish.speed + 0.85;
            fish.x += dx / distance * chaseSpeed;
            fish.y += dy / distance * chaseSpeed * 0.65;
            fish.direction = dx >= 0 ? 1 : -1;
        }

        private void swimNormally(DisplayFish fish) {
            fish.x += fish.speed * fish.direction;
            fish.y += Math.sin((tick + fish.waveOffset) * 0.035) * 0.45;
        }

        private void keepFishInsideTank(DisplayFish fish) {
            int fishW = fish.getWidth();
            int fishH = fish.getHeight();

            int leftLimit = tankRect.x + 15;
            int rightLimit = tankRect.x + tankRect.width - fishW - 15;
            int topLimit = tankRect.y + 45;
            int bottomLimit = tankRect.y + tankRect.height - fishH - 80;

            if (fish.x < leftLimit) {
                fish.x = leftLimit;
                fish.direction = 1;
            }

            if (fish.x > rightLimit) {
                fish.x = rightLimit;
                fish.direction = -1;
            }

            if (fish.y < topLimit) {
                fish.y = topLimit;
            }

            if (fish.y > bottomLimit) {
                fish.y = bottomLimit;
            }
        }

        private DisplayFish findFishTouchingFeed(FallingFeed feed) {
            Rectangle feedRect = feed.getBounds();

            for (DisplayFish fish : displayFishList) {
                if (fish.getBounds().intersects(feedRect)) {
                    return fish;
                }
            }

            return null;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            double scaleX = getWidth() / (double) SCREEN_WIDTH;
            double scaleY = getHeight() / (double) SCREEN_HEIGHT;

            g2.scale(scaleX, scaleY);

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            drawBackground(g2);
            drawFallingFeed(g2);
            drawFish(g2);
            drawVisitors(g2);
            drawGuide(g2);

            if (feedShopVisible) {
                drawProgramFeedShop(g2);
            } else {
                drawClosedFeedShopHint(g2);
            }

            drawIncomePanel(g2);
            drawExitButton(g2);
            drawBottomFishInfo(g2);
            drawGuideSpeech(g2);
            drawMessage(g2);

            g2.dispose();
        }

        private void drawBackground(Graphics2D g2) {
            if (backgroundImg != null) {
                g2.drawImage(backgroundImg, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, this);
                return;
            }

            GradientPaint bg = new GradientPaint(
                0,
                0,
                new Color(6, 18, 30),
                0,
                SCREEN_HEIGHT,
                new Color(1, 5, 10)
            );

            g2.setPaint(bg);
            g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

            g2.setColor(new Color(30, 150, 190));
            g2.fillRoundRect(tankRect.x, tankRect.y, tankRect.width, tankRect.height, 40, 40);

            g2.setColor(new Color(180, 120, 45));
            g2.setStroke(new BasicStroke(8));
            g2.drawRoundRect(tankRect.x, tankRect.y, tankRect.width, tankRect.height, 40, 40);

            g2.setFont(new Font("Serif", Font.BOLD, 48));
            g2.setColor(new Color(255, 220, 130));
            g2.drawString("AQUARIUM", 520, 60);
        }

        private void drawFallingFeed(Graphics2D g2) {
            for (FallingFeed feed : fallingFeeds) {
                Color color = getFeedColor(feed.feedType);

                g2.setColor(new Color(0, 0, 0, 95));
                g2.fillOval((int) feed.x - 3, (int) feed.y + 6, 18, 7);

                g2.setColor(color);

                for (int i = 0; i < 5; i++) {
                    int px = (int) feed.x + (i * 5) % 13;
                    int py = (int) feed.y + (i * 7) % 15;
                    g2.fillOval(px, py, 6, 6);
                }

                g2.setColor(new Color(255, 255, 255, 80));
                g2.drawOval((int) feed.x - 4, (int) feed.y - 4, 24, 24);
            }
        }

        private Color getFeedColor(String feedType) {
            if (feedType.equals("premium")) {
                return new Color(230, 190, 70);
            }

            if (feedType.equals("color")) {
                return new Color(220, 80, 100);
            }

            if (feedType.equals("growth")) {
                return new Color(95, 210, 95);
            }

            return new Color(190, 115, 42);
        }

        private void drawFish(Graphics2D g2) {
            for (DisplayFish displayFish : displayFishList) {
                drawOneFish(g2, displayFish);
            }
        }

        private void drawOneFish(Graphics2D g2, DisplayFish displayFish) {
            Fish fish = displayFish.entry.getFish();
            int x = (int) displayFish.x;
            int y = (int) displayFish.y;
            int w = displayFish.getWidth();
            int h = displayFish.getHeight();
            int dir = displayFish.direction;

            ImageIcon icon = new ImageIcon(fish.getImagePath());

            g2.setColor(new Color(0, 0, 0, 75));
            g2.fillOval(x + 8, y + h - 7, w - 18, 12);

            if (displayFish == hoveredFish) {
                g2.setColor(new Color(255, 230, 120, 105));
                g2.setStroke(new BasicStroke(3));
                g2.drawOval(x - 6, y - 6, w + 12, h + 12);
            }

            if (icon.getIconWidth() > 0) {
                Image img = icon.getImage();

                if (dir > 0) {
                    g2.drawImage(img, x, y, x + w, y + h, 0, 0, icon.getIconWidth(), icon.getIconHeight(), this);
                } else {
                    g2.drawImage(img, x + w, y, x, y + h, 0, 0, icon.getIconWidth(), icon.getIconHeight(), this);
                }
            } else {
                drawFallbackFish(g2, x, y, w, h, dir, fish.getName());
            }
        }

        private void drawFallbackFish(Graphics2D g2, int x, int y, int w, int h, int dir, String name) {
            g2.setColor(getFishColor(name));
            g2.fillOval(x, y + h / 4, w - 18, h / 2);

            Polygon tail = new Polygon();

            if (dir > 0) {
                tail.addPoint(x - 8, y + h / 2);
                tail.addPoint(x - 30, y + h / 4);
                tail.addPoint(x - 30, y + h * 3 / 4);
            } else {
                tail.addPoint(x + w - 8, y + h / 2);
                tail.addPoint(x + w + 12, y + h / 4);
                tail.addPoint(x + w + 12, y + h * 3 / 4);
            }

            g2.fillPolygon(tail);

            g2.setColor(Color.WHITE);

            if (dir > 0) {
                g2.fillOval(x + w - 34, y + h / 2 - 7, 8, 8);
                g2.setColor(Color.BLACK);
                g2.fillOval(x + w - 31, y + h / 2 - 5, 3, 3);
            } else {
                g2.fillOval(x + 16, y + h / 2 - 7, 8, 8);
                g2.setColor(Color.BLACK);
                g2.fillOval(x + 18, y + h / 2 - 5, 3, 3);
            }
        }

        private Color getFishColor(String name) {
            if (name.contains("小丑")) return new Color(255, 150, 55);
            if (name.contains("藍")) return new Color(70, 160, 255);
            if (name.contains("蝶")) return new Color(255, 215, 80);
            if (name.contains("沙丁")) return new Color(170, 220, 240);
            if (name.contains("蟹")) return new Color(210, 95, 60);
            return new Color(130, 220, 200);
        }

        private void drawVisitors(Graphics2D g2) {
            for (VisitorNpc visitor : visitors) {
                int xOffset = (int) (Math.sin((tick + visitor.phase) * 0.035) * 2);
                int yOffset = (int) (Math.sin((tick + visitor.phase) * 0.075) * 3);

                int shadowAlpha = Math.min(135, Math.max(70, visitor.y - 560));
                g2.setColor(new Color(0, 0, 0, shadowAlpha));
                g2.fillOval(
                    visitor.x + xOffset + visitor.w / 8,
                    visitor.y + yOffset + visitor.h - 8,
                    visitor.w * 3 / 4,
                    12
                );

                if (visitor.image != null) {
                    g2.drawImage(visitor.image, visitor.x + xOffset, visitor.y + yOffset, visitor.w, visitor.h, this);
                } else {
                    drawPixelVisitor(g2, visitor.x + xOffset, visitor.y + yOffset, visitor.w, visitor.h, visitor.phase);
                }
            }
        }

        private void drawPixelVisitor(Graphics2D g2, int x, int y, int w, int h, int phase) {
            Color coat = phase % 2 == 0 ? new Color(40, 70, 90) : new Color(90, 65, 45);

            g2.setColor(new Color(0, 0, 0, 100));
            g2.fillOval(x + 8, y + h - 8, w - 16, 10);

            g2.setColor(new Color(210, 165, 115));
            g2.fillOval(x + w / 2 - 13, y + 12, 26, 26);

            g2.setColor(coat);
            g2.fillRoundRect(x + w / 2 - 18, y + 38, 36, 60, 8, 8);

            g2.setColor(new Color(35, 25, 20));
            g2.fillRoundRect(x + w / 2 - 16, y + 94, 13, 38, 5, 5);
            g2.fillRoundRect(x + w / 2 + 3, y + 94, 13, 38, 5, 5);
        }

        private void drawGuide(Graphics2D g2) {
            int yOffset = (int) (Math.sin(tick * 0.05) * 2);

            if (guideImg != null) {
                g2.drawImage(guideImg, guideRect.x, guideRect.y + yOffset, guideRect.width, guideRect.height, this);
            } else {
                drawPixelGuide(g2, guideRect.x, guideRect.y + yOffset, guideRect.width, guideRect.height);
            }

            if (guideHovered) {
                g2.setColor(new Color(255, 230, 120, 95));
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(guideRect.x - 6, guideRect.y - 6, guideRect.width + 12, guideRect.height + 12, 18, 18);
            }
        }

        private void drawPixelGuide(Graphics2D g2, int x, int y, int w, int h) {
            g2.setColor(new Color(0, 0, 0, 110));
            g2.fillOval(x + 25, y + h - 12, w - 50, 14);

            g2.setColor(new Color(210, 160, 110));
            g2.fillOval(x + w / 2 - 28, y + 20, 56, 50);

            g2.setColor(new Color(18, 34, 52));
            g2.fillRoundRect(x + 30, y + 72, w - 60, 95, 16, 16);

            g2.setColor(new Color(220, 160, 60));
            g2.fillRect(x + 42, y + 92, w - 84, 6);
            g2.fillOval(x + w / 2 - 9, y + 112, 18, 18);

            g2.setColor(new Color(20, 20, 25));
            g2.fillOval(x + w / 2 - 30, y + 10, 60, 22);
            g2.fillRect(x + w / 2 - 34, y + 25, 68, 9);
        }

        private void drawClosedFeedShopHint(Graphics2D g2) {
            drawPanel(g2, 1190, 160, 350, 180);

            g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 22));
            g2.setColor(new Color(255, 220, 130));
            g2.drawString("飼料攤尚未開啟", 1258, 212);

            g2.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
            g2.setColor(new Color(220, 235, 235));
            g2.drawString("點擊水族箱下方的小朋友，", 1230, 252);
            g2.drawString("就可以打開飼料購買與餵食介面。", 1218, 282);
        }

        private void drawProgramFeedShop(Graphics2D g2) {
            drawPanel(g2, 1190, 160, 350, 455);
            drawFeedShopCloseButton(g2);

            g2.setFont(new Font("Serif", Font.BOLD, 31));
            g2.setColor(new Color(0, 0, 0, 150));
            g2.drawString("FEED SHOP", 1265, 204);
            g2.setColor(new Color(255, 220, 130));
            g2.drawString("FEED SHOP", 1262, 201);

            g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
            g2.setColor(new Color(170, 235, 255));
            g2.drawString("金幣：$" + InventoryManager.getMoney(), 1220, 230);

            for (FeedButton button : feedButtons) {
                drawFeedButton(g2, button);
            }

            drawActionButton(g2, buyButtonRect, "購買飼料", new Color(28, 90, 115), new Color(10, 45, 62));
            drawActionButton(g2, feedButtonRect, "餵食", new Color(110, 72, 24), new Color(62, 35, 12));

            g2.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
            g2.setColor(new Color(220, 235, 235));
            g2.drawString("目前選擇：" + AquariumManager.getFeedName(selectedFeedType), 1220, 628);
        }

        private void drawFeedShopCloseButton(Graphics2D g2) {
            Point mouse = getMousePositionSafe();
            boolean hover = feedShopCloseRect.contains(mouse);

            g2.setColor(hover ? new Color(185, 70, 58) : new Color(115, 48, 40));
            g2.fillOval(feedShopCloseRect.x, feedShopCloseRect.y, feedShopCloseRect.width, feedShopCloseRect.height);

            g2.setColor(new Color(255, 230, 170));
            g2.setStroke(new BasicStroke(3));
            g2.drawLine(feedShopCloseRect.x + 8, feedShopCloseRect.y + 8, feedShopCloseRect.x + 20, feedShopCloseRect.y + 20);
            g2.drawLine(feedShopCloseRect.x + 20, feedShopCloseRect.y + 8, feedShopCloseRect.x + 8, feedShopCloseRect.y + 20);
        }

        private void drawActionButton(Graphics2D g2, Rectangle rect, String text, Color top, Color bottom) {
            boolean hover = rect.contains(getMousePositionSafe());

            g2.setPaint(new GradientPaint(rect.x, rect.y, top, rect.x, rect.y + rect.height, bottom));
            g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 14, 14);

            g2.setColor(new Color(235, 185, 75));
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(rect.x + 2, rect.y + 2, rect.width - 4, rect.height - 4, 14, 14);

            if (hover) {
                g2.setColor(new Color(255, 235, 130, 45));
                g2.fillRoundRect(rect.x + 5, rect.y + 5, rect.width - 10, rect.height - 10, 10, 10);
            }

            g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 19));
            g2.setColor(new Color(255, 235, 170));

            FontMetrics fm = g2.getFontMetrics();
            int tx = rect.x + rect.width / 2 - fm.stringWidth(text) / 2;
            int ty = rect.y + rect.height / 2 + fm.getAscent() / 2 - 4;

            g2.drawString(text, tx, ty);
        }

        private Point getMousePositionSafe() {
            Point p = getMousePosition();

            if (p == null) {
                return new Point(-9999, -9999);
            }

            return toGamePoint(p);
        }

        private void drawFeedButton(Graphics2D g2, FeedButton button) {
            boolean selected = button.feedType.equals(selectedFeedType);
            boolean hover = button.hover;

            Color top = selected ? new Color(50, 125, 145, 240) : new Color(16, 52, 66, 235);
            Color bottom = selected ? new Color(12, 60, 80, 240) : new Color(5, 24, 34, 235);

            g2.setPaint(new GradientPaint(button.x, button.y, top, button.x, button.y + button.h, bottom));
            g2.fillRoundRect(button.x, button.y, button.w, button.h, 18, 18);

            g2.setColor(selected ? new Color(255, 225, 120) : new Color(190, 135, 55));
            g2.setStroke(new BasicStroke(selected ? 4 : 2));
            g2.drawRoundRect(button.x + 2, button.y + 2, button.w - 4, button.h - 4, 18, 18);

            if (hover) {
                g2.setColor(new Color(255, 240, 130, 55));
                g2.fillRoundRect(button.x + 5, button.y + 5, button.w - 10, button.h - 10, 14, 14);
            }

            drawFeedIcon(g2, button.x + 13, button.y + 16, button.feedType);

            g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
            g2.setColor(new Color(255, 230, 150));
            g2.drawString(button.title, button.x + 52, button.y + 25);

            g2.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
            g2.setColor(new Color(200, 230, 235));
            g2.drawString("$" + AquariumManager.getFeedPrice(button.feedType), button.x + 52, button.y + 47);
            g2.drawString("庫存：" + AquariumManager.getFeedCount(button.feedType), button.x + 52, button.y + 67);
        }

        private void drawFeedIcon(Graphics2D g2, int x, int y, String feedType) {
            Color feedColor = getFeedColor(feedType);

            g2.setColor(new Color(0, 0, 0, 90));
            g2.fillOval(x - 2, y + 36, 34, 8);

            g2.setPaint(new GradientPaint(x, y, new Color(110, 160, 170), x, y + 40, new Color(36, 62, 70)));
            g2.fillRoundRect(x, y, 30, 42, 9, 9);

            g2.setColor(new Color(225, 185, 95));
            g2.fillRoundRect(x + 2, y - 5, 26, 10, 5, 5);

            g2.setColor(feedColor);

            for (int i = 0; i < 12; i++) {
                int px = x + 6 + (i * 7) % 18;
                int py = y + 14 + (i * 5) % 22;
                g2.fillOval(px, py, 5, 5);
            }

            g2.setColor(new Color(255, 255, 255, 70));
            g2.drawLine(x + 6, y + 7, x + 6, y + 32);
        }

        private void drawIncomePanel(Graphics2D g2) {
            drawPanel(g2, 1190, 650, 350, 110);

            int income = AquariumManager.calculatePassiveIncomePerMinute();

            g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 21));
            g2.setColor(new Color(255, 220, 130));
            g2.drawString("水族館被動收入", 1220, 685);

            g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
            g2.setColor(new Color(105, 230, 255));
            g2.drawString("目前：$" + income + " / 分鐘", 1220, 715);

            g2.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));
            g2.setColor(new Color(220, 235, 235));
            g2.drawString("上次收入：$" + AquariumManager.getLastPassiveIncome(), 1220, 740);
            g2.drawString("下次結算：約 " + AquariumManager.getSecondsToNextPassiveIncome() + " 秒", 1370, 740);
        }

        private void drawExitButton(Graphics2D g2) {
            drawActionButton(g2, exitButtonRect, "離開水族館", new Color(78, 52, 36), new Color(35, 24, 18));
        }

        private void drawBottomFishInfo(Graphics2D g2) {
            drawPanel(g2, 430, 760, 720, 92);

            g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 24));
            g2.setColor(new Color(255, 225, 135));

            if (hoveredFish == null) {
                g2.drawString("將滑鼠移到魚身上查看狀態", 575, 805);
                g2.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
                g2.setColor(new Color(180, 230, 240));
                g2.drawString("點導覽員管理魚。點小朋友開啟飼料介面。選飼料後按餵食。", 505, 832);
                return;
            }

            AquariumManager.AquariumFish entry = hoveredFish.entry;
            String fishName = entry.getFish().getName();
            int hunger = entry.getHunger();
            int fullness = getFullness(entry);
            int income = AquariumManager.getPassiveIncomeForFish(entry);

            if (entry.isBaby()) {
                fishName += "（小魚）";
            }

            g2.drawString(fishName, 500, 797);

            g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
            g2.setColor(new Color(220, 235, 235));
            g2.drawString("飽食度", 500, 831);

            int barX = 575;
            int barY = 817;
            int barW = 230;
            int barH = 18;

            g2.setColor(new Color(20, 28, 32));
            g2.fillRoundRect(barX, barY, barW, barH, 10, 10);

            Color fullnessColor = fullness >= 70
                ? new Color(85, 220, 120)
                : fullness >= 35
                    ? new Color(230, 170, 50)
                    : new Color(235, 70, 60);

            g2.setColor(fullnessColor);
            g2.fillRoundRect(barX, barY, (int) (barW * fullness / 100.0), barH, 10, 10);

            g2.setColor(new Color(255, 230, 140));
            g2.drawRoundRect(barX, barY, barW, barH, 10, 10);

            g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
            g2.setColor(new Color(255, 225, 135));
            g2.drawString(fullness + "%", 820, 832);

            g2.setColor(new Color(200, 230, 235));
            g2.drawString("飢餓值 " + hunger + "%", 880, 832);
            g2.drawString("收入 $" + income + "/分", 1000, 832);
        }

        private void drawGuideSpeech(Graphics2D g2) {
            if (!guideHovered) {
                return;
            }

            drawPanel(g2, 835, 475, 330, 92);

            g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
            g2.setColor(new Color(255, 225, 135));
            g2.drawString("導覽員", 860, 507);

            g2.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));
            g2.setColor(new Color(225, 240, 240));
            g2.drawString(getGuideLine(), 860, 537);
        }

        private String getGuideLine() {
            String[] lines = {
                "點我可以管理儲藏箱，把魚放進水族館。",
                "同品種有兩隻以上，20 分鐘會生小魚。",
                "飽食度越高，魚的被動收入越穩定。",
                "餵食後飼料會掉進水裡，魚會自己去吃。"
            };

            return lines[(tick / 120) % lines.length];
        }

        private void drawMessage(Graphics2D g2) {
    if (message == null || message.isEmpty()) {
        return;
    }

    g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 15));
    g2.setColor(new Color(255, 235, 170));

    // 歡迎訊息靠左顯示，放在離開按鈕上方，避免重疊
    g2.drawString(message, 30, 835);
}

        private void drawPanel(Graphics2D g2, int x, int y, int w, int h) {
            g2.setColor(new Color(0, 0, 0, 145));
            g2.fillRoundRect(x + 7, y + 7, w, h, 18, 18);

            g2.setPaint(new GradientPaint(
                x,
                y,
                new Color(12, 46, 60, 232),
                x,
                y + h,
                new Color(4, 18, 30, 232)
            ));

            g2.fillRoundRect(x, y, w, h, 18, 18);

            g2.setColor(new Color(210, 145, 58));
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(x + 2, y + 2, w - 4, h - 4, 18, 18);

            g2.setColor(new Color(105, 225, 255, 35));
            g2.fillRoundRect(x + 8, y + 8, w - 16, h - 16, 14, 14);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            Point p = toGamePoint(e.getPoint());

            hoveredFish = null;
            guideHovered = guideRect.contains(p);

            for (FeedButton button : feedButtons) {
                if (feedShopVisible) {
                    button.hover = button.getRect().contains(p);
                } else {
                    button.hover = false;
                }
            }

            for (DisplayFish fish : displayFishList) {
                if (fish.getBounds().contains(p)) {
                    hoveredFish = fish;
                    break;
                }
            }

            repaint();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            Point p = toGamePoint(e.getPoint());

            if (exitButtonRect.contains(p)) {
                stopAnimation();

                if (backToLandAction != null) {
                    backToLandAction.actionPerformed(null);
                }

                return;
            }

            if (guideRect.contains(p)) {
                openAquariumNpcDialog();
                repaint();
                return;
            }

            if (feedShopVisible && feedShopCloseRect.contains(p)) {
                feedShopVisible = false;
                message = "已關閉飼料介面。點下方小朋友可以再次開啟。";
                repaint();
                return;
            }

            for (VisitorNpc visitor : visitors) {
                Rectangle visitorRect = new Rectangle(
                    visitor.x - 10,
                    visitor.y - 10,
                    visitor.w + 20,
                    visitor.h + 20
                );

                if (visitorRect.contains(p)) {
                    feedShopVisible = true;
                    message = "小朋友：我想買飼料餵魚！飼料攤已開啟。";
                    repaint();
                    return;
                }
            }

            if (!feedShopVisible) {
                message = "請先點水族箱下方的小朋友，開啟飼料介面。或點導覽員管理魚。";
                repaint();
                return;
            }

            for (FeedButton button : feedButtons) {
                if (button.getRect().contains(p)) {
                    selectedFeedType = button.feedType;
                    message = "已選擇：「" + AquariumManager.getFeedName(button.feedType) + "」。可以按購買或餵食。";
                    repaint();
                    return;
                }
            }

            if (buyButtonRect.contains(p)) {
                int before = AquariumManager.getFeedCount(selectedFeedType);
                AquariumManager.buyFeed(selectedFeedType);
                int after = AquariumManager.getFeedCount(selectedFeedType);

                if (after > before) {
                    message = "已購買「" + AquariumManager.getFeedName(selectedFeedType) + "」。";
                } else {
                    message = "金幣不足，無法購買「" + AquariumManager.getFeedName(selectedFeedType) + "」。";
                }

                repaint();
                return;
            }

            if (feedButtonRect.contains(p)) {
                dropSelectedFeed();
                repaint();
            }
        }

        private void openAquariumNpcDialog() {
            Window owner = SwingUtilities.getWindowAncestor(AquariumView.this);
            AquariumNpcDialog dialog = new AquariumNpcDialog(owner, this);
            dialog.setVisible(true);
        }

        private void refreshAquariumFishAfterStorageChange() {
            createDisplayFish();
            message = "水族館魚群已更新。";
            repaint();
        }

        private void dropSelectedFeed() {
            if (displayFishList.isEmpty()) {
                message = "水族館裡還沒有魚，不能餵食。";
                return;
            }

            int stock = AquariumManager.getFeedCount(selectedFeedType);
            int alreadyFalling = countFallingFeed(selectedFeedType);

            if (stock <= alreadyFalling) {
                message = "沒有可用的「" + AquariumManager.getFeedName(selectedFeedType) + "」，請先購買。";
                return;
            }

            double x = tankRect.x + 120 + Math.random() * (tankRect.width - 240);
            double y = tankRect.y + 25;
            double speed = 1.15 + Math.random() * 0.7;

            fallingFeeds.add(new FallingFeed(selectedFeedType, x, y, speed));
            message = "已投放「" + AquariumManager.getFeedName(selectedFeedType) + "」，魚會自己游過去吃。";
        }

        private int countFallingFeed(String feedType) {
            int count = 0;

            for (FallingFeed feed : fallingFeeds) {
                if (feed.feedType.equals(feedType)) {
                    count++;
                }
            }

            return count;
        }

        @Override public void mousePressed(MouseEvent e) {}

        @Override public void mouseReleased(MouseEvent e) {}

        @Override public void mouseEntered(MouseEvent e) {
            requestFocusInWindow();
        }

        @Override public void mouseExited(MouseEvent e) {}

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                stopAnimation();

                if (backToLandAction != null) {
                    backToLandAction.actionPerformed(null);
                }
            }
        }

        @Override public void keyReleased(KeyEvent e) {}

        @Override public void keyTyped(KeyEvent e) {}
    }

    class AquariumNpcDialog extends JDialog {

        private AquariumPanel aquariumPanel;
        private DefaultListModel<Fish> listModel;
        private JList<Fish> fishList;
        private JLabel statusLabel;

        public AquariumNpcDialog(Window owner, AquariumPanel aquariumPanel) {
            super(owner, "導覽員 - 魚隻管理", Dialog.ModalityType.APPLICATION_MODAL);

            this.aquariumPanel = aquariumPanel;

            setSize(620, 500);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout(12, 12));

            JPanel root = new JPanel(new BorderLayout(12, 12));
            root.setBackground(new Color(20, 30, 40));
            root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

            JLabel titleLabel = new JLabel("選擇儲藏箱中的魚，可以放入水族館或賣掉");
            titleLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 20));
            titleLabel.setForeground(new Color(255, 220, 130));
            root.add(titleLabel, BorderLayout.NORTH);

            listModel = new DefaultListModel<>();
            fishList = new JList<>(listModel);
            fishList.setCellRenderer(new FishRenderer());
            fishList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            fishList.setFixedCellHeight(80);
            fishList.setBackground(new Color(35, 45, 58));

            JScrollPane scrollPane = new JScrollPane(fishList);
            scrollPane.setBorder(BorderFactory.createLineBorder(new Color(210, 145, 58), 2));
            root.add(scrollPane, BorderLayout.CENTER);

            JPanel bottomPanel = new JPanel(new BorderLayout(0, 12));
            bottomPanel.setOpaque(false);

            JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 10, 10));
            buttonPanel.setOpaque(false);

            JButton putButton = createDialogButton("放入水族館");
            JButton sellButton = createDialogButton("賣掉選取魚");
            JButton sellAllButton = createDialogButton("全部賣掉");
            JButton closeButton = createDialogButton("關閉");

            putButton.addActionListener(e -> putSelectedFishIntoAquarium());
            sellButton.addActionListener(e -> sellSelectedFish());
            sellAllButton.addActionListener(e -> sellAllFish());
            closeButton.addActionListener(e -> dispose());

            buttonPanel.add(putButton);
            buttonPanel.add(sellButton);
            buttonPanel.add(sellAllButton);
            buttonPanel.add(closeButton);

            statusLabel = new JLabel("導覽員：把魚放進水族館後，就會開始提供被動收入。");
            statusLabel.setForeground(new Color(220, 235, 235));
            statusLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));

            bottomPanel.add(buttonPanel, BorderLayout.CENTER);
            bottomPanel.add(statusLabel, BorderLayout.SOUTH);

            root.add(bottomPanel, BorderLayout.SOUTH);

            add(root);
            reloadStorageFish();
        }

        private JButton createDialogButton(String text) {
            JButton button = new JButton(text);
            button.setFocusPainted(false);
            button.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
            button.setBackground(new Color(18, 70, 92));
            button.setForeground(new Color(255, 235, 170));
            button.setBorder(BorderFactory.createLineBorder(new Color(210, 145, 58), 2));
            return button;
        }

        private void reloadStorageFish() {
            listModel.clear();

            for (Fish fish : InventoryManager.getStorage()) {
                listModel.addElement(fish);
            }
        }

        private void putSelectedFishIntoAquarium() {
            Fish selected = fishList.getSelectedValue();

            if (selected == null) {
                statusLabel.setText("請先選一隻魚。");
                return;
            }

            boolean removed = InventoryManager.getStorage().remove(selected);

            if (!removed) {
                statusLabel.setText("放入失敗，這隻魚可能已經不在儲藏箱。");
                reloadStorageFish();
                return;
            }

            AquariumManager.addFish(selected);
            aquariumPanel.refreshAquariumFishAfterStorageChange();
            reloadStorageFish();
            statusLabel.setText("已將「" + selected.getName() + "」放入水族館。");
        }

        private void sellSelectedFish() {
            Fish selected = fishList.getSelectedValue();

            if (selected == null) {
                statusLabel.setText("請先選一隻要賣的魚。");
                return;
            }

            int price = InventoryManager.sellFish(selected);
            reloadStorageFish();

            if (price > 0) {
                statusLabel.setText("已賣掉「" + selected.getName() + "」，獲得 $" + price + "。");
            } else {
                statusLabel.setText("賣出失敗，這隻魚可能已經不在儲藏箱。");
            }
        }

        private void sellAllFish() {
            if (InventoryManager.getStorage().isEmpty()) {
                statusLabel.setText("儲藏箱目前沒有魚可以賣。");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                this,
                "確定要賣掉儲藏箱裡全部的魚嗎？",
                "確認全部賣掉",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            int total = InventoryManager.sellAllStorageFish();
            reloadStorageFish();
            statusLabel.setText("已賣掉全部魚，獲得 $" + total + "。");
        }

        private String buildStars(int count) {
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < count; i++) {
                sb.append("★");
            }

            return sb.toString();
        }

        class FishRenderer extends JPanel implements ListCellRenderer<Fish> {
            private JLabel iconLabel = new JLabel();
            private JLabel nameLabel = new JLabel();
            private JLabel infoLabel = new JLabel();

            public FishRenderer() {
                setLayout(new BorderLayout(12, 0));
                setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

                iconLabel.setPreferredSize(new Dimension(72, 60));
                iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

                JPanel textPanel = new JPanel(new GridLayout(2, 1));
                textPanel.setOpaque(false);

                nameLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 17));
                infoLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));

                textPanel.add(nameLabel);
                textPanel.add(infoLabel);

                add(iconLabel, BorderLayout.WEST);
                add(textPanel, BorderLayout.CENTER);
            }

            @Override
            public Component getListCellRendererComponent(
                JList<? extends Fish> list,
                Fish fish,
                int index,
                boolean isSelected,
                boolean cellHasFocus
            ) {
                setOpaque(true);
                setBackground(isSelected ? new Color(28, 86, 108) : new Color(38, 47, 60));

                ImageIcon rawIcon = new ImageIcon(fish.getImagePath());

                if (rawIcon.getIconWidth() > 0) {
                    Image scaled = rawIcon.getImage().getScaledInstance(60, 50, Image.SCALE_SMOOTH);
                    iconLabel.setIcon(new ImageIcon(scaled));
                    iconLabel.setText("");
                } else {
                    iconLabel.setIcon(null);
                    iconLabel.setText("魚");
                    iconLabel.setForeground(Color.WHITE);
                }

                nameLabel.setText(fish.getName());
                nameLabel.setForeground(new Color(255, 230, 150));

                int incomePreview = Math.max(
                    3,
                    fish.getPrice() / 25 + Math.max(1, fish.getRarityStars()) * 4
                );

                infoLabel.setText(
                    "價格：$"
                    + fish.getPrice()
                    + "　稀有度："
                    + buildStars(fish.getRarityStars())
                    + "　預估收入：$"
                    + incomePreview
                    + "/分"
                );

                infoLabel.setForeground(new Color(205, 230, 235));

                return this;
            }
        }
    }

    static class DisplayFish {
        AquariumManager.AquariumFish entry;
        double x;
        double y;
        double speed;
        int direction;
        int waveOffset;
        int sizeBoost;

        public DisplayFish(AquariumManager.AquariumFish entry, double x, double y, double speed, int direction) {
            this.entry = entry;
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.direction = direction;
            this.waveOffset = (int) (Math.random() * 100);
            this.sizeBoost = Math.max(0, entry.getFish().getRarityStars() - 1) * 4;

            if (entry.isBaby()) {
                this.sizeBoost -= 12;
            }
        }

        public int getWidth() {
            return Math.max(38, 70 + sizeBoost);
        }

        public int getHeight() {
            return Math.max(28, 54 + sizeBoost);
        }

        public Rectangle getBounds() {
            return new Rectangle((int) x, (int) y, getWidth(), getHeight());
        }
    }

    static class VisitorNpc {
        Image image;
        int x;
        int y;
        int w;
        int h;
        int phase;

        public VisitorNpc(Image image, int x, int y, int w, int h, int phase) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.phase = phase;
        }
    }

    static class FeedButton {
        String feedType;
        String title;
        String description;
        int x;
        int y;
        int w;
        int h;
        boolean hover = false;

        public FeedButton(String feedType, String title, String description, int x, int y, int w, int h) {
            this.feedType = feedType;
            this.title = title;
            this.description = description;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        public Rectangle getRect() {
            return new Rectangle(x, y, w, h);
        }
    }

    static class FallingFeed {
        String feedType;
        double x;
        double y;
        double speed;
        int phase;

        public FallingFeed(String feedType, double x, double y, double speed) {
            this.feedType = feedType;
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.phase = (int) (Math.random() * 200);
        }

        public Rectangle getBounds() {
            return new Rectangle((int) x - 8, (int) y - 8, 30, 30);
        }
    }
}