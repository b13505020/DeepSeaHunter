import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class BeachMaterial {

    private String name;
    private String imagePath;

    private int x;
    private int y;
    private int width;
    private int height;

    private boolean collected = false;

    private BufferedImage image;

    public BeachMaterial(String name, String imagePath, int x, int y, int width, int height) {
        this.name = name;
        this.imagePath = imagePath;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        loadImage();
    }

    private void loadImage() {
        try {
            BufferedImage raw = ImageIO.read(new File(imagePath));
            image = removeWhiteBackground(raw);
        } catch (Exception e) {
            System.out.println("找不到沙灘素材圖片：" + imagePath);
            image = null;
        }
    }

    // 把白色 / 淺灰色背景轉透明
    private BufferedImage removeWhiteBackground(BufferedImage src) {
        BufferedImage result = new BufferedImage(
            src.getWidth(),
            src.getHeight(),
            BufferedImage.TYPE_INT_ARGB
        );

        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int argb = src.getRGB(x, y);

                int a = (argb >> 24) & 0xff;
                int r = (argb >> 16) & 0xff;
                int g = (argb >> 8) & 0xff;
                int b = argb & 0xff;

                if (a < 10) {
                    result.setRGB(x, y, 0x00000000);
                    continue;
                }

                if (isWhiteBackgroundPixel(r, g, b)) {
                    result.setRGB(x, y, 0x00000000);
                } else {
                    result.setRGB(x, y, argb);
                }
            }
        }

        return result;
    }

    private boolean isWhiteBackgroundPixel(int r, int g, int b) {
        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));

        boolean veryBright = r > 238 && g > 238 && b > 238;
        boolean closeToWhiteOrGray = max - min < 28;

        return veryBright && closeToWhiteOrGray;
    }

    public void draw(Graphics2D g2, int cameraX) {
        if (collected) {
            return;
        }

        int screenX = x - cameraX;

        if (image != null) {
            g2.drawImage(image, screenX, y, width, height, null);
        } else {
            g2.setColor(new Color(255, 220, 80));
            g2.fillOval(screenX, y, width, height);

            g2.setColor(Color.BLACK);
            g2.drawOval(screenX, y, width, height);

            g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
            g2.drawString(name, screenX, y - 5);
        }
    }

    public Rectangle getCollectBox() {
        return new Rectangle(
            x - 25,
            y - 25,
            width + 50,
            height + 50
        );
    }

    public String getName() {
        return name;
    }

    public boolean isCollected() {
        return collected;
    }

    public void setCollected(boolean collected) {
        this.collected = collected;
    }
}