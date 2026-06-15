import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InventoryManager {
    // 永久儲藏箱：成功上岸後的魚會存在這裡
    private static List<Fish> storageList = new ArrayList<>();

    // 本次潛水暫存背包：還沒成功上岸前，魚先存在這裡
    private static List<Fish> currentDiveList = new ArrayList<>();

    // 沙灘素材：本次沙灘暫存背包
    private static LinkedHashMap<String, Integer> currentMaterialMap = createEmptyMaterialMap();

    // 沙灘素材：永久儲藏箱
    private static LinkedHashMap<String, Integer> storageMaterialMap = createEmptyMaterialMap();

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
    // 沙灘素材名稱 / 圖片
    // =========================

    private static LinkedHashMap<String, Integer> createEmptyMaterialMap() {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();

        map.put("潮蝕木材", 0);
        map.put("巨螺殼", 0);
        map.put("貝殼碎片", 0);
        map.put("珊瑚碎枝", 0);
        map.put("纜繩鉤環", 0);
        map.put("鏽蝕齒輪", 0);
        map.put("海蝕石", 0);

        return map;
    }

    private static void ensureMaterialMaps() {
        currentMaterialMap = normalizeMaterialMap(currentMaterialMap);
        storageMaterialMap = normalizeMaterialMap(storageMaterialMap);
    }

    private static LinkedHashMap<String, Integer> normalizeMaterialMap(Map<String, Integer> oldMap) {
        LinkedHashMap<String, Integer> result = createEmptyMaterialMap();

        if (oldMap == null) {
            return result;
        }

        for (String key : oldMap.keySet()) {
            int value = oldMap.getOrDefault(key, 0);

            if (result.containsKey(key)) {
                result.put(key, Math.max(0, value));
            }
        }

        return result;
    }

    public static String getMaterialImagePath(String name) {
        switch (name) {
            case "潮蝕木材":
                return "assets/tideworn_wood.png";
            case "巨螺殼":
                return "assets/giant_conch.png";
            case "貝殼碎片":
                return "assets/shell_fragments.png";
            case "珊瑚碎枝":
                return "assets/coral_branch.png";
            case "纜繩鉤環":
                return "assets/hooked_rope.png";
            case "鏽蝕齒輪":
                return "assets/rusted_gear.png";
            case "海蝕石":
                return "assets/sea_worn_stone.png";
            default:
                return "";
        }
    }

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
            saveGame();
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

        if (total > 0) {
            saveGame();
        }

        return total;
    }

    public static int sellAllStorageFish() {
        int total = 0;

        for (Fish f : storageList) {
            total += f.getPrice();
        }

        storageList.clear();
        money += total;

        if (total > 0) {
            saveGame();
        }

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
    // 魚背包 / 儲藏箱相關
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
    // 沙灘素材背包 / 儲藏箱相關
    // =========================

    public static void addCurrentMaterial(String name, int amount) {
        ensureMaterialMaps();

        if (!currentMaterialMap.containsKey(name)) {
            return;
        }

        int oldCount = currentMaterialMap.get(name);
        currentMaterialMap.put(name, oldCount + Math.max(0, amount));
    }

    public static void moveCurrentMaterialsToStorage() {
        ensureMaterialMaps();

        for (String name : currentMaterialMap.keySet()) {
            int currentCount = currentMaterialMap.getOrDefault(name, 0);

            if (currentCount > 0) {
                int oldStorageCount = storageMaterialMap.getOrDefault(name, 0);
                storageMaterialMap.put(name, oldStorageCount + currentCount);
            }
        }

        currentMaterialMap = createEmptyMaterialMap();
    }

    public static void clearCurrentMaterials() {
        currentMaterialMap = createEmptyMaterialMap();
    }

    public static Map<String, Integer> getCurrentMaterials() {
        ensureMaterialMaps();
        return new LinkedHashMap<>(currentMaterialMap);
    }

    public static Map<String, Integer> getStorageMaterials() {
        ensureMaterialMaps();
        return new LinkedHashMap<>(storageMaterialMap);
    }

    public static int getCurrentMaterialTotalCount() {
        ensureMaterialMaps();

        int total = 0;

        for (int count : currentMaterialMap.values()) {
            total += count;
        }

        return total;
    }

    public static int getStorageMaterialTotalCount() {
        ensureMaterialMaps();

        int total = 0;

        for (int count : storageMaterialMap.values()) {
            total += count;
        }

        return total;
    }

    public static int getStorageMaterialCount(String name) {
        ensureMaterialMaps();
        return storageMaterialMap.getOrDefault(name, 0);
    }

    public static boolean hasStorageMaterial(String name, int amount) {
        ensureMaterialMaps();
        return storageMaterialMap.getOrDefault(name, 0) >= amount;
    }

    public static boolean spendStorageMaterial(String name, int amount) {
        ensureMaterialMaps();

        if (!storageMaterialMap.containsKey(name)) {
            return false;
        }

        int currentCount = storageMaterialMap.get(name);

        if (currentCount < amount) {
            return false;
        }

        storageMaterialMap.put(name, currentCount - amount);
        return true;
    }

    public static boolean hasStorageMaterials(Map<String, Integer> requirements) {
        ensureMaterialMaps();

        if (requirements == null || requirements.isEmpty()) {
            return true;
        }

        for (Map.Entry<String, Integer> entry : requirements.entrySet()) {
            String name = entry.getKey();
            int amount = Math.max(0, entry.getValue());

            if (storageMaterialMap.getOrDefault(name, 0) < amount) {
                return false;
            }
        }

        return true;
    }

    public static boolean spendStorageMaterials(Map<String, Integer> requirements) {
        ensureMaterialMaps();

        if (!hasStorageMaterials(requirements)) {
            return false;
        }

        if (requirements == null) {
            return true;
        }

        for (Map.Entry<String, Integer> entry : requirements.entrySet()) {
            String name = entry.getKey();
            int amount = Math.max(0, entry.getValue());
            int currentCount = storageMaterialMap.getOrDefault(name, 0);

            storageMaterialMap.put(name, currentCount - amount);
        }

        return true;
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
        saveGame();
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
        saveGame();
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
        saveGame();
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
            ensureMaterialMaps();

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

            // 重要：保存水族館展示中的魚
            data.aquariumFishList = new ArrayList<>(AquariumManager.getAquariumFishSaveList());

            data.currentMaterials = new LinkedHashMap<>(currentMaterialMap);
            data.storageMaterials = new LinkedHashMap<>(storageMaterialMap);

            data.unlockedFish = CollectionManager.getUnlockedFishSnapshot();
            data.ownedWeapons = WeaponManager.getOwnedWeaponNamesSnapshot();

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

            // 重要：讀回水族館展示中的魚
            if (data.aquariumFishList != null) {
                AquariumManager.loadAquariumFishSaveList(data.aquariumFishList);
            } else {
                AquariumManager.loadAquariumFishSaveList(new ArrayList<>());
            }

            currentMaterialMap = normalizeMaterialMap(data.currentMaterials);
            storageMaterialMap = normalizeMaterialMap(data.storageMaterials);

            CollectionManager.setUnlockedFish(data.unlockedFish);
            WeaponManager.setOwnedWeaponNames(data.ownedWeapons);

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

        currentMaterialMap = createEmptyMaterialMap();
        storageMaterialMap = createEmptyMaterialMap();

        money = 0;

        oxygenLevel = 1;
        suitLevel = 1;
        backpackLevel = 1;

        // 重要：新遊戲時清空水族館
        AquariumManager.loadAquariumFishSaveList(new ArrayList<>());

        CollectionManager.resetUnlockedFish();
        WeaponManager.resetOwnedWeapons();

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