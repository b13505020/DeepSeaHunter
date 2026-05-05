import java.util.ArrayList;
import java.util.List;

public class InventoryManager {
    // 1. 永久儲藏箱 (Storage)
    private static List<Fish> storageList = new ArrayList<>();
    
    // 2. 本次潛水暫存 (原本的 Backpack)
    private static List<Fish> currentDiveList = new ArrayList<>();

    // --- 新增：為了相容舊代碼 (BackpackView & ShopView) ---
    public static List<Fish> getMyBackpack() {
        return currentDiveList;
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
    // ------------------------------------------------

    // 成功上岸時呼叫：轉移至永久儲藏
    public static void moveToStorage() {
        storageList.addAll(currentDiveList);
        currentDiveList.clear();
    }

    // 失敗時呼叫：清空暫存
    public static void clearCurrentDive() {
        currentDiveList.clear();
    }

    // 抓到魚時呼叫
    public static void addFish(Fish fish) {
        currentDiveList.add(fish);
    }

    // 取得永久儲藏清單 (給 StorageView 用)
    public static List<Fish> getStorage() {
        return storageList;
    }
    
    // 取得所有資料 (若需要)
    public static List<Fish> getInventory() {
        return storageList;
    }
}