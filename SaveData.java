import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SaveData implements Serializable {
    private static final long serialVersionUID = 1L;

    int money;

    int oxygenLevel;
    int suitLevel;
    int backpackLevel;

    // 魚類儲藏箱
    List<Fish> storageList;

    // 本次潛水背包
    List<Fish> currentDiveList;

    // 水族館展示中的魚
    List<Fish> aquariumFishList;

    // 圖鑑解鎖魚
    Set<String> unlockedFish;

    // 沙灘素材：本次暫存
    Map<String, Integer> currentMaterials;

    // 沙灘素材：永久儲藏
    Map<String, Integer> storageMaterials;

    // 已擁有武器
    // 重點：這裡要用 Set<String>，不要用 List<String>
    Set<String> ownedWeapons;

    public SaveData() {
        storageList = new ArrayList<>();
        currentDiveList = new ArrayList<>();
        aquariumFishList = new ArrayList<>();

        unlockedFish = new HashSet<>();

        currentMaterials = new LinkedHashMap<>();
        storageMaterials = new LinkedHashMap<>();

        ownedWeapons = new HashSet<>();
    }
}