import java.util.*;

public class CollectionManager {
    // 使用 TreeSet 讓圖鑑按字母順序排列
    private static Set<String> unlockedFish = new TreeSet<>();
    private static Map<String, Integer> fishStars = new HashMap<>();
    private static Map<String, String> fishImages = new HashMap<>();

    // 靜態初始化：註冊所有 8 種魚類
    static {
        registerFish("沙丁魚", 1, "assets/fish_sardine.png");
        registerFish("金魚", 1, "assets/fish_goldfish.png");
        registerFish("小丑魚", 2, "assets/fish_clownfish.png");
        registerFish("螃蟹", 2, "assets/fish_crab.png");
        registerFish("神仙魚", 3, "assets/fish_angelfish.png");
        registerFish("河豚", 3, "assets/fish_pufferfish.png");
        registerFish("藍倒吊", 4, "assets/fish_surgefish.png");
        registerFish("綠鰻魚", 4, "assets/fish_green.png");
        registerFish("鯊魚", 5, "assets/fish_shark.png");
    }

    private static void registerFish(String name, int stars, String path) {
        fishStars.put(name, stars);
        fishImages.put(name, path);
    }

    // 解鎖圖鑑的方法
    public static void unlock(String name) {
        if (fishImages.containsKey(name)) {
            unlockedFish.add(name);
        }
    }

    public static Set<String> getUnlockedFish() { return unlockedFish; }
    public static int getStars(String name) { return fishStars.getOrDefault(name, 1); }
    public static String getImagePath(String name) { return fishImages.getOrDefault(name, ""); }
}