import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import javax.imageio.ImageIO;

public class StorageView extends JFrame {
    public StorageView() {
        setTitle("永久儲藏箱 (永久保存)");
        setSize(760, 620);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);

        JPanel container = new JPanel(new BorderLayout(10, 10));
        container.setBackground(new Color(40, 40, 50));
        container.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel header = new JLabel("我的儲藏物資 (成功結算後在此保存)");
        header.setForeground(Color.YELLOW);
        header.setFont(new Font("Microsoft JhengHei", Font.BOLD, 22));
        container.add(header, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(40, 40, 50));

        addFishSection(contentPanel);
        addMaterialSection(contentPanel);

        JScrollPane scroll = new JScrollPane(contentPanel);
        scroll.getViewport().setBackground(new Color(40, 40, 50));
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        container.add(scroll, BorderLayout.CENTER);

        add(container);
        setVisible(true);
    }

    private void addFishSection(JPanel contentPanel) {
        JLabel fishTitle = createSectionTitle("魚類儲藏");
        contentPanel.add(fishTitle);

        JPanel fishGrid = new JPanel(new GridLayout(0, 4, 10, 10));
        fishGrid.setBackground(new Color(40, 40, 50));
        fishGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1000));

        List<Fish> myStorage = InventoryManager.getStorage();

        if (myStorage.isEmpty()) {
            fishGrid.add(createEmptyLabel("目前沒有魚。"));
        } else {
            for (Fish f : myStorage) {
                fishGrid.add(createFishCard(f));
            }
        }

        contentPanel.add(fishGrid);
        contentPanel.add(Box.createVerticalStrut(25));
    }

    private void addMaterialSection(JPanel contentPanel) {
        JLabel materialTitle = createSectionTitle("沙灘素材儲藏");
        contentPanel.add(materialTitle);

        JPanel materialGrid = new JPanel(new GridLayout(0, 4, 10, 10));
        materialGrid.setBackground(new Color(40, 40, 50));
        materialGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1000));

        Map<String, Integer> materials = InventoryManager.getStorageMaterials();

        boolean hasAnyMaterial = false;
        for (int count : materials.values()) {
            if (count > 0) {
                hasAnyMaterial = true;
                break;
            }
        }

        if (!hasAnyMaterial) {
            materialGrid.add(createEmptyLabel("目前沒有沙灘素材。"));
        } else {
            for (Map.Entry<String, Integer> entry : materials.entrySet()) {
                if (entry.getValue() > 0) {
                    materialGrid.add(createMaterialCard(entry.getKey(), entry.getValue()));
                }
            }
        }

        contentPanel.add(materialGrid);
    }

    private JLabel createSectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(new Color(255, 220, 120));
        label.setFont(new Font("Microsoft JhengHei", Font.BOLD, 20));
        label.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel createEmptyLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(Color.LIGHT_GRAY);
        label.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        label.setOpaque(true);
        label.setBackground(new Color(60, 60, 75));
        label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        return label;
    }

    private JPanel createFishCard(Fish f) {
        JPanel itemBox = new JPanel(new BorderLayout());
        itemBox.setBackground(new Color(60, 60, 75));
        itemBox.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        ImageIcon rawIcon = new ImageIcon(f.getImagePath());
        JLabel imgLabel;

        if (rawIcon.getIconWidth() > 0) {
            Image img = rawIcon.getImage().getScaledInstance(90, 75, Image.SCALE_SMOOTH);
            imgLabel = new JLabel(new ImageIcon(img), SwingConstants.CENTER);
        } else {
            imgLabel = new JLabel("No Image", SwingConstants.CENTER);
            imgLabel.setForeground(Color.WHITE);
        }

        JLabel nameLabel = new JLabel(f.getName(), SwingConstants.CENTER);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));

        itemBox.add(imgLabel, BorderLayout.CENTER);
        itemBox.add(nameLabel, BorderLayout.SOUTH);

        return itemBox;
    }

    private JPanel createMaterialCard(String name, int count) {
        JPanel itemBox = new JPanel(new BorderLayout());
        itemBox.setBackground(new Color(55, 65, 80));
        itemBox.setBorder(BorderFactory.createLineBorder(new Color(160, 190, 210)));

        String imagePath = InventoryManager.getMaterialImagePath(name);

        JLabel imgLabel;

        try {
            BufferedImage raw = ImageIO.read(new File(imagePath));

            // 這次用「邊緣泛洪去背」，比單純白色判斷更有效
            BufferedImage transparentImg = removeBackgroundByFloodFill(raw);

            Image img = transparentImg.getScaledInstance(95, 85, Image.SCALE_SMOOTH);
            imgLabel = new JLabel(new ImageIcon(img), SwingConstants.CENTER);

        } catch (Exception e) {
            imgLabel = new JLabel("Material", SwingConstants.CENTER);
            imgLabel.setForeground(Color.WHITE);
        }

        JLabel nameLabel = new JLabel(
            "<html><center>" + name + "<br>x " + count + "</center></html>",
            SwingConstants.CENTER
        );

        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));

        itemBox.add(imgLabel, BorderLayout.CENTER);
        itemBox.add(nameLabel, BorderLayout.SOUTH);

        return itemBox;
    }

    // =========================
    // 強化版：從圖片邊緣開始去背
    // =========================
    private BufferedImage removeBackgroundByFloodFill(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();

        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        // 先完整複製圖片
        Graphics2D g2 = result.createGraphics();
        g2.drawImage(src, 0, 0, null);
        g2.dispose();

        boolean[][] visited = new boolean[w][h];
        Queue<Point> queue = new LinkedList<>();

        // 把四邊都加入 queue，因為白底一定連到圖片邊緣
        for (int x = 0; x < w; x++) {
            queue.add(new Point(x, 0));
            queue.add(new Point(x, h - 1));
        }

        for (int y = 0; y < h; y++) {
            queue.add(new Point(0, y));
            queue.add(new Point(w - 1, y));
        }

        while (!queue.isEmpty()) {
            Point p = queue.poll();

            int x = p.x;
            int y = p.y;

            if (x < 0 || x >= w || y < 0 || y >= h) {
                continue;
            }

            if (visited[x][y]) {
                continue;
            }

            visited[x][y] = true;

            int argb = result.getRGB(x, y);

            if (!isBackgroundLike(argb)) {
                continue;
            }

            // 轉透明
            result.setRGB(x, y, 0x00000000);

            queue.add(new Point(x + 1, y));
            queue.add(new Point(x - 1, y));
            queue.add(new Point(x, y + 1));
            queue.add(new Point(x, y - 1));
        }

        // 再補一次：把很淡的殘留白邊也清掉
        cleanRemainingLightPixels(result);

        return result;
    }

    private boolean isBackgroundLike(int argb) {
        int a = (argb >> 24) & 0xff;
        int r = (argb >> 16) & 0xff;
        int g = (argb >> 8) & 0xff;
        int b = argb & 0xff;

        if (a < 20) {
            return true;
        }

        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));

        // 白、灰白、米白、淡背景
        boolean bright = r > 185 && g > 185 && b > 185;
        boolean lowSaturation = max - min < 70;

        return bright && lowSaturation;
    }

    private void cleanRemainingLightPixels(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = img.getRGB(x, y);

                int a = (argb >> 24) & 0xff;
                int r = (argb >> 16) & 0xff;
                int g = (argb >> 8) & 0xff;
                int b = argb & 0xff;

                if (a < 20) {
                    img.setRGB(x, y, 0x00000000);
                    continue;
                }

                int max = Math.max(r, Math.max(g, b));
                int min = Math.min(r, Math.min(g, b));

                boolean veryLight = r > 215 && g > 215 && b > 215;
                boolean grayOrWhite = max - min < 80;

                if (veryLight && grayOrWhite) {
                    img.setRGB(x, y, 0x00000000);
                }
            }
        }
    }
}