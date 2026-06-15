import java.util.Map;

public class WeaponCraftTest {
    public static void main(String[] args) {
        InventoryManager.resetGame();

        Weapon rifle = findWeapon("水下步槍");
        Weapon sniper = findWeapon("狙擊槍");

        Map<String, Integer> sniperRecipe = WeaponManager.getRecipe(sniper);
        int gearBefore = InventoryManager.getStorageMaterialCount("鏽蝕齒輪");

        if (WeaponManager.craftWeapon(sniper)) {
            throw new AssertionError("Insufficient materials must not craft a weapon");
        }

        if (InventoryManager.getStorageMaterialCount("鏽蝕齒輪") != gearBefore) {
            throw new AssertionError("Failed crafting must not consume materials");
        }

        InventoryManager.addCurrentMaterial("鏽蝕齒輪", 1);
        InventoryManager.addCurrentMaterial("潮蝕木材", 2);
        InventoryManager.moveCurrentMaterialsToStorage();

        if (!WeaponManager.canCraft(rifle)) {
            throw new AssertionError("Rifle should be craftable with its full recipe");
        }

        if (!WeaponManager.craftWeapon(rifle) || !WeaponManager.isOwned(rifle)) {
            throw new AssertionError("Crafted rifle must become owned");
        }

        if (InventoryManager.getStorageMaterialCount("鏽蝕齒輪") != 0
            || InventoryManager.getStorageMaterialCount("潮蝕木材") != 0) {
            throw new AssertionError("Successful crafting must consume the recipe");
        }

        System.out.println("Weapon crafting checks passed: " + sniperRecipe);
    }

    private static Weapon findWeapon(String name) {
        for (Weapon weapon : WeaponManager.getAllWeapons()) {
            if (weapon.getName().equals(name)) {
                return weapon;
            }
        }

        throw new AssertionError("Missing weapon: " + name);
    }
}
