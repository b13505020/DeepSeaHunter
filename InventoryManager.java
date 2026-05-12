import java.util.ArrayList;
import java.util.List;

public class InventoryManager {
    // 永久儲藏箱：成功上岸後的魚會存在這裡
    private static List<Fish> storageList = new ArrayList<>();

    // 本次潛水暫存背包：還沒成功上岸前，魚先存在這裡
    private static List<Fish> currentDiveList = new ArrayList<>();

    // 玩家真正可以花的錢
    private static int money = 0;

    // 裝備等級
    private static int oxygenLevel = 1;
    private static int suitLevel = 1;
    private static int backpackLevel = 1;

    private static final int MAX_LEVEL = 3;

    // =========================
    // 金錢相關
    // =========================

    public static int getMoney() {
        return money;
    }

    public static void addMoney(int amount) {
        money += amount;
    }

    public static boolean spendMoney(int amount) {
        if (money < amount) {
            return false;
        }

        money -= amount;
        return true;
    }

    // 賣掉一隻魚
    public static int sellFish(Fish fish) {
        if (storageList.remove(fish)) {
            money += fish.getPrice();
            return fish.getPrice();
        }

        return 0;
    }

    // 賣掉玩家選取的多隻魚
    public static int sellSelectedFish(List<Fish> selectedFish) {
        int total = 0;

        for (Fish f : selectedFish) {
            if (storageList.remove(f)) {
                total += f.getPrice();
            }
        }

        money += total;
        return total;
    }

    // 賣掉全部儲藏箱裡的魚
    public static int sellAllStorageFish() {
        int total = 0;

        for (Fish f : storageList) {
            total += f.getPrice();
        }

        storageList.clear();
        money += total;

        return total;
    }

    // 計算儲藏箱魚的總價值
    public static int getStorageValue() {
        int total = 0;

        for (Fish f : storageList) {
            total += f.getPrice();
        }

        return total;
    }

    // =========================
    // 背包 / 儲藏箱相關
    // =========================

    // 為了相容 BackpackView
    public static List<Fish> getMyBackpack() {
        return currentDiveList;
    }

    // 成功上岸時呼叫：本次潛水的魚轉移到永久儲藏箱
    public static void moveToStorage() {
        storageList.addAll(currentDiveList);
        currentDiveList.clear();
    }

    // 氧氣耗盡或失敗時呼叫：清空本次潛水背包
    public static void clearCurrentDive() {
        currentDiveList.clear();
    }

    // 抓到魚時呼叫：會檢查背包容量
    public static boolean addFish(Fish fish) {
        if (currentDiveList.size() >= getBackpackCapacity()) {
            return false;
        }

        currentDiveList.add(fish);
        return true;
    }

    // 取得永久儲藏箱
    public static List<Fish> getStorage() {
        return storageList;
    }

    // 保留舊方法名稱，避免其他檔案出錯
    public static List<Fish> getInventory() {
        return storageList;
    }

    // 總資產價值：背包 + 儲藏箱
    // 注意：這不是玩家可以花的錢，只是目前魚的總價值
    public static int getTotalPrice() {
        int total = 0;

        for (Fish f : currentDiveList) {
            total += f.getPrice();
        }

        for (Fish f : storageList) {
            total += f.getPrice();
        }

        return total;
    }

    // =========================
    // 裝備等級 getter
    // =========================

    public static int getOxygenLevel() {
        return oxygenLevel;
    }

    public static int getSuitLevel() {
        return suitLevel;
    }

    public static int getBackpackLevel() {
        return backpackLevel;
    }

    // =========================
    // 裝備效果
    // =========================

    // 氧氣瓶：影響下水秒數
    public static double getMaxOxygenTime() {
        if (oxygenLevel == 1) return 60.0;
        if (oxygenLevel == 2) return 90.0;
        return 120.0;
    }

    // 潛水衣：影響最大可下潛深度
    public static int getMaxDepth() {
        if (suitLevel == 1) return 800;
        if (suitLevel == 2) return 1500;
        return 2400;
    }

    // 背包：影響一次潛水能帶幾隻魚
    public static int getBackpackCapacity() {
        if (backpackLevel == 1) return 5;
        if (backpackLevel == 2) return 8;
        return 12;
    }

    // =========================
    // 升級價格
    // =========================

    public static int getOxygenUpgradeCost() {
        if (oxygenLevel == 1) return 500;
        if (oxygenLevel == 2) return 1200;
        return -1;
    }

    public static int getSuitUpgradeCost() {
        if (suitLevel == 1) return 800;
        if (suitLevel == 2) return 1800;
        return -1;
    }

    public static int getBackpackUpgradeCost() {
        if (backpackLevel == 1) return 600;
        if (backpackLevel == 2) return 1500;
        return -1;
    }

    // =========================
    // 升級方法
    // =========================

    public static boolean upgradeOxygen() {
        if (oxygenLevel >= MAX_LEVEL) {
            return false;
        }

        int cost = getOxygenUpgradeCost();

        if (!spendMoney(cost)) {
            return false;
        }

        oxygenLevel++;
        return true;
    }

    public static boolean upgradeSuit() {
        if (suitLevel >= MAX_LEVEL) {
            return false;
        }

        int cost = getSuitUpgradeCost();

        if (!spendMoney(cost)) {
            return false;
        }

        suitLevel++;
        return true;
    }

    public static boolean upgradeBackpack() {
        if (backpackLevel >= MAX_LEVEL) {
            return false;
        }

        int cost = getBackpackUpgradeCost();

        if (!spendMoney(cost)) {
            return false;
        }

        backpackLevel++;
        return true;
    }
}