import java.awt.*;

public class Bullet {
    private double x;
    private double y;
    private double startX;
    private double startY;
    private double velX;
    private double velY;

    private String weaponName;
    private int damage;
    private int range;
    private int speed;
    private int size;
    private int pierceLeft;
    private int explosionRadius;
    private int netRadius;
    private int sleepDurationMs;
    private int slowDurationMs;
    private Color color;

    public Bullet(int x, int y, double angleDegree, Weapon weapon) {
        this.x = x;
        this.y = y;
        this.startX = x;
        this.startY = y;
        this.weaponName = weapon.getName();
        this.damage = weapon.getDamage();
        this.range = weapon.getRange();

        setupByWeaponName();

        double radians = Math.toRadians(angleDegree);
        this.velX = Math.cos(radians) * speed;
        this.velY = Math.sin(radians) * speed;
    }

    private void setupByWeaponName() {
        speed = 25;
        size = 10;
        pierceLeft = 0;
        explosionRadius = 0;
        netRadius = 0;
        sleepDurationMs = 0;
        slowDurationMs = 0;
        color = Color.YELLOW;

        if (weaponName.equals("水下步槍")) {
            speed = 32;
            size = 8;
            color = new Color(80, 230, 255);
        } else if (weaponName.equals("狙擊槍")) {
            speed = 42;
            size = 6;
            pierceLeft = 2;
            color = new Color(255, 245, 170);
        } else if (weaponName.equals("網槍")) {
            speed = 18;
            size = 20;
            damage = 0;
            netRadius = 95;
            color = new Color(120, 255, 190);
        } else if (weaponName.equals("睡眠槍")) {
            speed = 24;
            size = 14;
            damage = Math.max(1, damage);
            sleepDurationMs = 3500;
            color = new Color(190, 130, 255);
        } else if (weaponName.equals("麻醉槍")) {
            speed = 28;
            size = 11;
            damage = Math.max(1, damage + 1);
            sleepDurationMs = 5200;
            color = new Color(255, 120, 200);
        } else if (weaponName.equals("榴彈發射器")) {
            speed = 16;
            size = 22;
            explosionRadius = 135;
            color = new Color(255, 135, 45);
        } else if (weaponName.equals("寒冰槍")) {
            speed = 24;
            size = 15;
            slowDurationMs = 4500;
            color = new Color(110, 225, 255);
        }
    }

    public void move() {
        x += velX;
        y += velY;
    }

    public boolean isOutOfRange() {
        double dist = Math.sqrt(Math.pow(x - startX, 2) + Math.pow(y - startY, 2));
        return dist > range;
    }

    public void registerPierceHit() {
        if (pierceLeft > 0) {
            pierceLeft--;
        }
    }

    public boolean canPierceAfterHit() {
        return pierceLeft > 0;
    }

    public boolean isPiercingBullet() {
        return weaponName.equals("狙擊槍");
    }

    public boolean isNetBullet() {
        return netRadius > 0;
    }

    public boolean isExplosionBullet() {
        return explosionRadius > 0;
    }

    public String getWeaponName() {
        return weaponName;
    }

    public int getDamage() {
        return damage;
    }

    public int getExplosionRadius() {
        return explosionRadius;
    }

    public int getNetRadius() {
        return netRadius;
    }

    public int getSleepDurationMs() {
        return sleepDurationMs;
    }

    public int getSlowDurationMs() {
        return slowDurationMs;
    }

    public Rectangle getBounds() {
        return new Rectangle(
            (int) (x - size / 2.0),
            (int) (y - size / 2.0),
            size,
            size
        );
    }

    public int getX() {
        return (int) x;
    }

    public int getY() {
        return (int) y;
    }

    public void draw(Graphics2D g2d, int cameraX, int cameraY) {
        int screenX = getX() - cameraX;
        int screenY = getY() - cameraY;

        Graphics2D bulletG = (Graphics2D) g2d.create();
        bulletG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        bulletG.setColor(color);

        if (weaponName.equals("狙擊槍")) {
            bulletG.setStroke(new BasicStroke(3));
            bulletG.drawLine(
                (int) (screenX - velX * 0.55),
                (int) (screenY - velY * 0.55),
                screenX,
                screenY
            );
        } else if (weaponName.equals("網槍")) {
            bulletG.setStroke(new BasicStroke(2));
            bulletG.drawOval(screenX - size / 2, screenY - size / 2, size, size);
            bulletG.drawLine(screenX - size / 2, screenY, screenX + size / 2, screenY);
            bulletG.drawLine(screenX, screenY - size / 2, screenX, screenY + size / 2);
        } else if (weaponName.equals("榴彈發射器")) {
            bulletG.fillOval(screenX - size / 2, screenY - size / 2, size, size);
            bulletG.setColor(new Color(255, 230, 120));
            bulletG.fillOval(screenX - 4, screenY - 4, 8, 8);
        } else if (weaponName.equals("寒冰槍")) {
            bulletG.fillOval(screenX - size / 2, screenY - size / 2, size, size);
            bulletG.setColor(Color.WHITE);
            bulletG.drawLine(screenX - 8, screenY, screenX + 8, screenY);
            bulletG.drawLine(screenX, screenY - 8, screenX, screenY + 8);
        } else {
            bulletG.fillOval(screenX - size / 2, screenY - size / 2, size, size);
        }

        bulletG.dispose();
    }
}
