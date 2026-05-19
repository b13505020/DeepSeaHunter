import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class WeaponManager {

    private static ArrayList<Weapon> allWeapons = new ArrayList<>();
    private static HashMap<String, Integer> weaponPrices = new HashMap<>();
    private static HashMap<String, String> weaponImages = new HashMap<>();
    private static HashSet<String> ownedWeaponNames = new HashSet<>();

    static {
        registerWeapon(new Weapon("初級魚槍", 1, 600), "assets/pistol.png", 0, true);
        registerWeapon(new Weapon("水下步槍", 2, 750), "assets/rifle.png", 600, false);
        registerWeapon(new Weapon("狙擊槍", 6, 1300), "assets/sniper.png", 1800, false);
        registerWeapon(new Weapon("網槍", 1, 500), "assets/netgun.png", 900, false);
        registerWeapon(new Weapon("睡眠槍", 1, 650), "assets/sleepygun.png", 1000, false);
        registerWeapon(new Weapon("麻醉槍", 1, 650), "assets/dartgun.png", 1000, false);
        registerWeapon(new Weapon("榴彈發射器", 8, 450), "assets/grenede.png", 2200, false);
        registerWeapon(new Weapon("寒冰槍", 2, 700), "assets/freezegun.png", 1600, false);
    }

    private static void registerWeapon(Weapon weapon, String imagePath, int price, boolean ownedAtStart) {
        allWeapons.add(weapon);
        weaponImages.put(weapon.getName(), imagePath);
        weaponPrices.put(weapon.getName(), price);

        if (ownedAtStart) {
            ownedWeaponNames.add(weapon.getName());
        }
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
