import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


public class WeaponManager {

    private static ArrayList<Weapon> allWeapons = new ArrayList<>();
    private static HashMap<String, Integer> weaponPrices = new HashMap<>();
    private static HashMap<String, String> weaponImages = new HashMap<>();
    private static HashSet<String> ownedWeaponNames = new HashSet<>();
    private static HashMap<String, LinkedHashMap<String, Integer>> weaponRecipes = new HashMap<>();


    static {
        registerWeapon(new Weapon("初級魚槍", 1, 600), "assets/pistol.png", 0, true);
        registerWeapon(new Weapon("水下步槍", 2, 750), "assets/rifle.png", 600, false);
        registerWeapon(new Weapon("狙擊槍", 6, 1300), "assets/sniper.png", 1800, false);
        registerWeapon(new Weapon("網槍", 1, 500), "assets/netgun.png", 900, false);
        registerWeapon(new Weapon("睡眠槍", 1, 650), "assets/sleepygun.png", 1000, false);
        registerWeapon(new Weapon("麻醉槍", 1, 650), "assets/dartgun.png", 1000, false);
        registerWeapon(new Weapon("榴彈發射器", 8, 450), "assets/grenede.png", 2200, false);
        registerWeapon(new Weapon("寒冰槍", 2, 700), "assets/freezegun.png", 1600, false);
        registerRecipe("水下步槍", "鏽蝕齒輪", 1, "潮蝕木材", 2);
        registerRecipe("狙擊槍", "鏽蝕齒輪", 2, "海蝕石", 3, "纜繩鉤環", 1);
        registerRecipe("網槍", "纜繩鉤環", 2, "貝殼碎片", 3);
        registerRecipe("睡眠槍", "珊瑚碎枝", 2, "貝殼碎片", 2);
        registerRecipe("麻醉槍", "珊瑚碎枝", 3, "巨螺殼", 1);
        registerRecipe("榴彈發射器", "鏽蝕齒輪", 3, "海蝕石", 4);
        registerRecipe("寒冰槍", "珊瑚碎枝", 3, "巨螺殼", 2, "海蝕石", 2);
    }

    private static void registerWeapon(Weapon weapon, String imagePath, int price, boolean ownedAtStart) {
        allWeapons.add(weapon);
        weaponImages.put(weapon.getName(), imagePath);
        weaponPrices.put(weapon.getName(), price);

        if (ownedAtStart) {
            ownedWeaponNames.add(weapon.getName());
        }
    }

    private static void registerRecipe(String weaponName, Object... materialAndAmounts) {
        LinkedHashMap<String, Integer> recipe = new LinkedHashMap<>();

        for (int i = 0; i + 1 < materialAndAmounts.length; i += 2) {
            String materialName = (String) materialAndAmounts[i];
            int amount = (Integer) materialAndAmounts[i + 1];
            recipe.put(materialName, amount);
        }

        weaponRecipes.put(weaponName, recipe);
    }

    public static ArrayList<Weapon> getAllWeapons() {
        return new ArrayList<>(allWeapons);
    }

    public static ArrayList<Weapon> getOwnedWeapons() {
        ArrayList<Weapon> ownedWeapons = new ArrayList<>();

        for (Weapon weapon : allWeapons) {
            if (isOwned(weapon)) {
                ownedWeapons.add(weapon);
            }
        }

        return ownedWeapons;
    }

    public static String getImagePath(Weapon weapon) {
        return weaponImages.getOrDefault(weapon.getName(), "");
    }

    public static int getPrice(Weapon weapon) {
        return weaponPrices.getOrDefault(weapon.getName(), 0);
    }

    public static boolean isOwned(Weapon weapon) {
        return ownedWeaponNames.contains(weapon.getName());
    }

    public static boolean buyWeapon(Weapon weapon) {
        if (isOwned(weapon)) {
            return false;
        }

        int price = getPrice(weapon);

        if (!InventoryManager.spendMoney(price)) {
            return false;
        }

        ownedWeaponNames.add(weapon.getName());
        InventoryManager.saveGame();
        return true;
    }
    
    public static Map<String, Integer> getRecipe(Weapon weapon) {
        LinkedHashMap<String, Integer> recipe = weaponRecipes.get(weapon.getName());

        if (recipe == null) {
            return new LinkedHashMap<>();
        }

        return new LinkedHashMap<>(recipe);
    }

    public static boolean canCraft(Weapon weapon) {
        if (weapon == null || isOwned(weapon)) {
            return false;
        }

        Map<String, Integer> recipe = getRecipe(weapon);
        return !recipe.isEmpty() && InventoryManager.hasStorageMaterials(recipe);
    }

    public static boolean craftWeapon(Weapon weapon) {
        if (!canCraft(weapon)) {
            return false;
        }

        if (!InventoryManager.spendStorageMaterials(getRecipe(weapon))) {
            return false;
        }

        ownedWeaponNames.add(weapon.getName());
        InventoryManager.saveGame();
        return true;
    }
    
    public static Set<String> getOwnedWeaponNamesSnapshot() {
        return new HashSet<>(ownedWeaponNames);
    }

    public static void setOwnedWeaponNames(Set<String> names) {
        ownedWeaponNames.clear();

        if (names == null || names.isEmpty()) {
            ownedWeaponNames.add("初級魚槍");
            return;
        }

        for (String name : names) {
            if (weaponImages.containsKey(name)) {
                ownedWeaponNames.add(name);
            }
        }

        if (ownedWeaponNames.isEmpty()) {
            ownedWeaponNames.add("初級魚槍");
        }
    }

    public static void resetOwnedWeapons() {
        ownedWeaponNames.clear();
        ownedWeaponNames.add("初級魚槍");
    }
}
