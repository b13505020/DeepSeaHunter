import java.util.ArrayList;

public class InventoryManager {
    private static ArrayList<Fish> myBackpack = new ArrayList<>();

    public static void addFish(Fish fish) {
        myBackpack.add(fish);
        // 修正：呼叫 CollectionManager 的 unlock 方法
        CollectionManager.unlock(fish.getName());
    }

    public static ArrayList<Fish> getMyBackpack() { return myBackpack; }
    public static int getTotalPrice() {
        int total = 0;
        for (Fish f : myBackpack) total += f.getPrice();
        return total;
    }
}