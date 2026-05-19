import java.util.ArrayList;
import java.util.List;

public class AquariumManager {

    public static class AquariumFish {
        private Fish fish;
        private int hunger;
        private int maxHunger;

        public AquariumFish(Fish fish) {
            this.fish = fish;
            this.maxHunger = 100;
            this.hunger = 55;
        }

        public Fish getFish() {
            return fish;
        }

        public int getHunger() {
            return hunger;
        }

        public int getMaxHunger() {
            return maxHunger;
        }

        public void feed(int amount) {
            hunger -= amount;

            if (hunger < 0) {
                hunger = 0;
            }
        }

        public void increaseHunger(int amount) {
            hunger += amount;

            if (hunger > maxHunger) {
                hunger = maxHunger;
            }
        }
    }

    private static List<AquariumFish> aquariumFishList = new ArrayList<>();

    private static int basicFeedCount = 0;
    private static int premiumFeedCount = 0;
    private static int colorFeedCount = 0;
    private static int growthFeedCount = 0;

    private static long lastPassiveUpdateTime = System.currentTimeMillis();
    private static int lastPassiveIncome = 0;

    public static void addFish(Fish fish) {
        if (fish == null) {
            return;
        }

        aquariumFishList.add(new AquariumFish(fish));
    }

    public static List<AquariumFish> getAquariumFishList() {
        return aquariumFishList;
    }

    // 保留舊方法，避免其他舊程式還有用到 getAquariumFish()
    public static List<Fish> getAquariumFish() {
        List<Fish> result = new ArrayList<>();

        for (AquariumFish entry : aquariumFishList) {
            result.add(entry.getFish());
        }

        return result;
    }

    public static int getTotalCount() {
        return aquariumFishList.size();
    }

    public static int getTotalValue() {
        int total = 0;

        for (AquariumFish entry : aquariumFishList) {
            total += entry.getFish().getPrice();
        }

        return total;
    }

    public static int getFullness(AquariumFish entry) {
        int fullness = 100 - entry.getHunger();

        if (fullness < 0) {
            fullness = 0;
        }

        if (fullness > 100) {
            fullness = 100;
        }

        return fullness;
    }

    public static int calculatePassiveIncomePerMinute() {
        int total = 0;

        for (AquariumFish entry : aquariumFishList) {
            Fish fish = entry.getFish();

            int rarity = Math.max(1, fish.getRarityStars());
            int baseIncome = Math.max(3, fish.getPrice() / 25 + rarity * 4);
            int fullness = getFullness(entry);

            if (fullness <= 10) {
                baseIncome = 0;
            } else if (fullness <= 35) {
                baseIncome = baseIncome / 2;
            }

            total += baseIncome;
        }

        return total;
    }

    // 這個方法會讓被動收入在所有畫面都能運作。
    // MissionHudOverlay 和 AquariumView 都可以呼叫它。
    public static void updatePassiveIncomeSystem() {
        long now = System.currentTimeMillis();

        while (now - lastPassiveUpdateTime >= 60000) {
            increaseAllHungerSlowly();
            lastPassiveIncome = calculatePassiveIncomePerMinute();

            if (lastPassiveIncome > 0) {
                InventoryManager.addMoney(lastPassiveIncome);
            }

            lastPassiveUpdateTime += 60000;
        }
    }

    public static int getLastPassiveIncome() {
        return lastPassiveIncome;
    }

    public static int getSecondsToNextPassiveIncome() {
        long now = System.currentTimeMillis();
        long diff = now - lastPassiveUpdateTime;
        int seconds = 60 - (int) (diff / 1000);

        if (seconds < 0) {
            seconds = 0;
        }

        return seconds;
    }

    public static void buyFeed(String feedType) {
        int price = getFeedPrice(feedType);

        if (InventoryManager.getMoney() < price) {
            return;
        }

        InventoryManager.addMoney(-price);

        if (feedType.equals("basic")) {
            basicFeedCount++;
        } else if (feedType.equals("premium")) {
            premiumFeedCount++;
        } else if (feedType.equals("color")) {
            colorFeedCount++;
        } else if (feedType.equals("growth")) {
            growthFeedCount++;
        }
    }

    public static boolean hasFeed(String feedType) {
        return getFeedCount(feedType) > 0;
    }

    public static int getFeedCount(String feedType) {
        if (feedType.equals("basic")) {
            return basicFeedCount;
        }

        if (feedType.equals("premium")) {
            return premiumFeedCount;
        }

        if (feedType.equals("color")) {
            return colorFeedCount;
        }

        if (feedType.equals("growth")) {
            return growthFeedCount;
        }

        return 0;
    }

    public static int getFeedPrice(String feedType) {
        if (feedType.equals("basic")) {
            return 80;
        }

        if (feedType.equals("premium")) {
            return 180;
        }

        if (feedType.equals("color")) {
            return 260;
        }

        if (feedType.equals("growth")) {
            return 350;
        }

        return 0;
    }

    public static String getFeedName(String feedType) {
        if (feedType.equals("basic")) {
            return "基礎魚飼料";
        }

        if (feedType.equals("premium")) {
            return "高級魚飼料";
        }

        if (feedType.equals("color")) {
            return "增色飼料";
        }

        if (feedType.equals("growth")) {
            return "成長飼料";
        }

        return "未知飼料";
    }

    public static int getFeedEffect(String feedType) {
        if (feedType.equals("basic")) {
            return 15;
        }

        if (feedType.equals("premium")) {
            return 35;
        }

        if (feedType.equals("color")) {
            return 25;
        }

        if (feedType.equals("growth")) {
            return 45;
        }

        return 0;
    }

    public static boolean useFeed(String feedType, AquariumFish targetFish) {
        if (targetFish == null) {
            return false;
        }

        if (!hasFeed(feedType)) {
            return false;
        }

        if (feedType.equals("basic")) {
            basicFeedCount--;
        } else if (feedType.equals("premium")) {
            premiumFeedCount--;
        } else if (feedType.equals("color")) {
            colorFeedCount--;
        } else if (feedType.equals("growth")) {
            growthFeedCount--;
        }

        targetFish.feed(getFeedEffect(feedType));
        return true;
    }

    public static void increaseAllHungerSlowly() {
        for (AquariumFish entry : aquariumFishList) {
            entry.increaseHunger(1);
        }
    }
}
