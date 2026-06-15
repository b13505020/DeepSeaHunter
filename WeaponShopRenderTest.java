import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

public class WeaponShopRenderTest {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                InventoryManager.resetGame();
                InventoryManager.addCurrentMaterial("鏽蝕齒輪", 1);
                InventoryManager.addCurrentMaterial("潮蝕木材", 2);
                InventoryManager.addCurrentMaterial("珊瑚碎枝", 1);
                InventoryManager.moveCurrentMaterialsToStorage();

                WeaponShopScreen screen = new WeaponShopScreen(e -> {});
                screen.setSize(1600, 900);
                screen.doLayout();

                BufferedImage image = new BufferedImage(
                    1600,
                    900,
                    BufferedImage.TYPE_INT_ARGB
                );
                Graphics2D graphics = image.createGraphics();
                screen.printAll(graphics);
                graphics.dispose();

                ImageIO.write(image, "png", new File("/tmp/weapon_shop_preview.png"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        System.exit(0);
    }
}
