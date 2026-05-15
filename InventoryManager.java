import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

    // 存檔位置
    private static final String SAVE_FOLDER = "saves";
    private static final String SAVE_FILE = SAVE_FOLDER + "/deepsea_save.dat";

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

    public static int sellFish(Fish fish) {
        if (storageList.remove(fish)) {
            money += fish.getPrice();
            return fish.getPrice();
        }

        return 0;
    }

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

    public static int sellAllStorageFish() {
        int total = 0;

        for (Fish f : storageList) {
            total += f.getPrice();
        }

        storageList.clear();
        money += total;

        return total;
    }

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

    public static List<Fish> getMyBackpack() {
        return currentDiveList;
    }

    public static void moveToStorage() {
        storageList.addAll(currentDiveList);
        currentDiveList.clear();
    }

    public static void clearCurrentDive() {
        currentDiveList.clear();
    }

    public static boolean addFish(Fish fish) {
        if (currentDiveList.size() >= getBackpackCapacity()) {
            return false;
        }

        currentDiveList.add(fish);
        return true;
    }

    public static List<Fish> getStorage() {
        return storageList;
    }

    public static List<Fish> getInventory() {
        return storageList;
    }

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

    public static double getMaxOxygenTime() {
        if (oxygenLevel == 1) {
            return 60.0;
        }

        if (oxygenLevel == 2) {
            return 90.0;
        }

        return 120.0;
    }

    public static int getMaxDepth() {
        if (suitLevel == 1) {
            return 800;
        }

        if (suitLevel == 2) {
            return 1500;
        }

        return 2400;
    }

    public static int getBackpackCapacity() {
        if (backpackLevel == 1) {
            return 5;
        }

        if (backpackLevel == 2) {
            return 8;
        }

        return 12;
    }

    // =========================
    // 升級價格
    // =========================

    public static int getOxygenUpgradeCost() {
        if (oxygenLevel == 1) {
            return 500;
        }

        if (oxygenLevel == 2) {
            return 1200;
        }

        return -1;
    }

    public static int getSuitUpgradeCost() {
        if (suitLevel == 1) {
            return 800;
        }

        if (suitLevel == 2) {
            return 1800;
        }

        return -1;
    }

    public static int getBackpackUpgradeCost() {
        if (backpackLevel == 1) {
            return 600;
        }

        if (backpackLevel == 2) {
            return 1500;
        }

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

    // =========================
    // 存檔 / 讀檔 / 新遊戲
    // =========================

    public static boolean hasSaveFile() {
        File file = new File(SAVE_FILE);
        return file.exists() && file.isFile();
    }

    public static boolean saveGame() {
        try {
            File folder = new File(SAVE_FOLDER);

            if (!folder.exists()) {
                folder.mkdirs();
            }

            SaveData data = new SaveData();

            data.money = money;
            data.oxygenLevel = oxygenLevel;
            data.suitLevel = suitLevel;
            data.backpackLevel = backpackLevel;

            data.storageList = new ArrayList<>(storageList);
            data.currentDiveList = new ArrayList<>(currentDiveList);

            data.unlockedFish = CollectionManager.getUnlockedFishSnapshot();

            ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(SAVE_FILE)
            );

            out.writeObject(data);
            out.close();

            System.out.println("✅ 遊戲已自動存檔：" + SAVE_FILE);
            return true;

        } catch (Exception e) {
            System.out.println("❌ 存檔失敗");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean loadGame() {
        if (!hasSaveFile()) {
            return false;
        }

        try {
            ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(SAVE_FILE)
            );

            SaveData data = (SaveData) in.readObject();
            in.close();

            money = data.money;

            oxygenLevel = clampLevel(data.oxygenLevel);
            suitLevel = clampLevel(data.suitLevel);
            backpackLevel = clampLevel(data.backpackLevel);

            if (data.storageList != null) {
                storageList = new ArrayList<>(data.storageList);
            } else {
                storageList = new ArrayList<>();
            }

            if (data.currentDiveList != null) {
                currentDiveList = new ArrayList<>(data.currentDiveList);
            } else {
                currentDiveList = new ArrayList<>();
            }

            CollectionManager.setUnlockedFish(data.unlockedFish);

            System.out.println("✅ 讀取存檔成功：" + SAVE_FILE);
            return true;

        } catch (Exception e) {
            System.out.println("❌ 讀取存檔失敗");
            e.printStackTrace();
            return false;
        }
    }

    public static void resetGame() {
        storageList.clear();
        currentDiveList.clear();

        money = 0;

        oxygenLevel = 1;
        suitLevel = 1;
        backpackLevel = 1;

        CollectionManager.resetUnlockedFish();

        System.out.println("已建立新遊戲資料");
    }

    private static int clampLevel(int level) {
        if (level < 1) {
            return 1;
        }

        if (level > MAX_LEVEL) {
            return MAX_LEVEL;
        }

        return level;
    }
}