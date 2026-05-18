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

    List<Fish> storageList;
    List<Fish> currentDiveList;

    Map<String, Integer> currentMaterials;
    Map<String, Integer> storageMaterials;

    Set<String> unlockedFish;

    public SaveData() {
        storageList = new ArrayList<>();
        currentDiveList = new ArrayList<>();

        currentMaterials = new LinkedHashMap<>();
        storageMaterials = new LinkedHashMap<>();

        unlockedFish = new HashSet<>();
    }
}