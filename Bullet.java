import java.awt.Rectangle;

public class Bullet {
    private double x, y, startX, startY;
    private double velX, velY;
    private int damage, range;
    private final int speed = 25;

    public Bullet(int x, int y, double angleDegree, Weapon weapon) {
        this.x = x; this.y = y;
        this.startX = x; this.startY = y;
        this.damage = weapon.getDamage();
        this.range = weapon.getRange();
        
        double radians = Math.toRadians(angleDegree);
        this.velX = Math.cos(radians) * speed;
        this.velY = Math.sin(radians) * speed;
    }

    public void move() { x += velX; y += velY; }

    // 檢查是否超過射程
    public boolean isOutOfRange() {
        double dist = Math.sqrt(Math.pow(x - startX, 2) + Math.pow(y - startY, 2));
        return dist > range;
    }

    public int getDamage() { return damage; }
    public Rectangle getBounds() { return new Rectangle((int)x, (int)y, 10, 10); }
    public int getX() { return (int)x; }
    public int getY() { return (int)y; }
}