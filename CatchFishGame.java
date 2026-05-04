import javax.swing.*;
import java.awt.*;

public class CatchFishGame {
    public CatchFishGame(Fish fish) {
        ImageIcon icon = new ImageIcon(fish.getImagePath());
        Image scaled = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        String stars = "⭐".repeat(fish.getRarityStars());
        String msg = String.format("✨ 捕捉成功： %s ✨\n難易度：%s\n重量：%.1f kg\n價值：$%d", 
                                   fish.getName(), stars, fish.getWeight(), fish.getPrice());
        JOptionPane.showMessageDialog(null, msg, "捕捉成功", JOptionPane.INFORMATION_MESSAGE, new ImageIcon(scaled));
    }
}