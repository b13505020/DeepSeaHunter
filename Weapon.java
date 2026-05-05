public class Weapon {
    private String name;
    private int damage;
    private int range; // 射程（子彈消失前的飛行距離）

    public Weapon(String name, int damage, int range) {
        this.name = name;
        this.damage = damage;
        this.range = range;
    }

    public String getName() {
        return name;
    }

    public int getDamage() {
        return damage;
    }

    public int getRange() {
        return range;
    }
}