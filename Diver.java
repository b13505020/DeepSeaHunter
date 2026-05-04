import java.awt.Rectangle;

public class Diver {
    private int x, y;
    private final int size = 50, speed = 15;
    private double angle = 0; 
    private Weapon currentWeapon;

    public Diver(int x, int y) {
        this.x = x; this.y = y;
        // 預設武器：小魚槍 (攻擊1, 射程600)
        this.currentWeapon = new Weapon("初級魚槍", 1, 600);
    }

    public void setWeapon(Weapon w) { this.currentWeapon = w; }
    public Weapon getWeapon() { return currentWeapon; }

    public void move(int dx, int dy) {
        x += dx * speed; y += dy * speed;
        x = Math.max(0, Math.min(x, 1550)); y = Math.max(0, Math.min(y, 850));
    }

    public void updateAngle(int mouseX, int mouseY) {
        double dx = mouseX - (x + size / 2.0);
        double dy = mouseY - (y + size / 2.0);
        double targetAngle = Math.toDegrees(Math.atan2(dy, dx));

        if (dx >= 0) {
            if (targetAngle < -45) targetAngle = -45;
            if (targetAngle > 45) targetAngle = 45;
        } else {
            if (targetAngle >= 0 && targetAngle < 135) targetAngle = 135;
            if (targetAngle < 0 && targetAngle > -135) targetAngle = -135;
        }
        this.angle = targetAngle;
    }

    public double getAngle() { return angle; }
    public Rectangle getBounds() { return new Rectangle(x, y, size, size); }
    public int getX() { return x; }
    public int getY() { return y; }
}