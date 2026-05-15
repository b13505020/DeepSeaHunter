import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class CollectionManager {
    // 使用 TreeSet 讓圖鑑排序比較穩定
    private static Set<String> unlockedFish = new TreeSet<>();

    private static Map<String, Integer> fishStars = new HashMap<>();
    private static Map<String, String> fishImages = new HashMap<>();

    static {
        registerFish("沙丁魚", 1, "assets/fish_anchovy.png");
        registerFish("金魚", 1, "assets/fish_goldfish.png");
        registerFish("小丑魚", 2, "assets/fish_clownfish.png");
        registerFish("螃蟹", 2, "assets/fish_crab.png");
        registerFish("河豚", 3, "assets/fish_pufferfish.png");
        registerFish("刺尾魚", 3, "assets/fish_surgefish.png");
        registerFish("神仙魚", 4, "assets/fish_angelfish.png");
        registerFish("青魚", 4, "assets/fish_green.png");
        registerFish("鯊魚", 5, "assets/fish_shark.png");

        // 保留舊名字，避免之前其他地方有用到
        registerFish("藍倒吊", 4, "assets/fish_surgefish.png");
        registerFish("綠鰻魚", 4, "assets/fish_green.png");
    }

    private static void registerFish(String name, int stars, String path) {
        fishStars.put(name, stars);
        fishImages.put(name, path);
    }

    public static void unlock(String name) {
        if (fishImages.containsKey(name)) {
            unlockedFish.add(name);
        }
    }

    public static Set<String> getUnlockedFish() {
        return unlockedFish;
    }

    public static int getStars(String name) {
        return fishStars.getOrDefault(name, 1);
    }

    public static String getImagePath(String name) {
        return fishImages.getOrDefault(name, "");
    }

    // =========================
    // 存檔用
    // =========================

    public static Set<String> getUnlockedFishSnapshot() {
        return new HashSet<>(unlockedFish);
    }

    public static void setUnlockedFish(Set<String> savedUnlockedFish) {
        unlockedFish.clear();

        if (savedUnlockedFish == null) {
            return;
        }

        for (String fishName : savedUnlockedFish) {
            if (fishImages.containsKey(fishName)) {
                unlockedFish.add(fishName);
            }
        }
    }

    public static void resetUnlockedFish() {
        unlockedFish.clear();
    }
}