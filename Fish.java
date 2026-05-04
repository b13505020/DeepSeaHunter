import java.awt.Rectangle;
import java.util.Random;

public class Fish extends Item {
    private double x, y, velX, velY, targetX, targetY;
    private int hp;
    private boolean isDead = false, isFacingRight = true;
    private int fishSize = 50;
    private Random random = new Random();
    private double waveOffset;

    public Fish(String name, double weight, int price, String imagePath, int maxHp, int rarityStars) {
        super(name, weight, price, imagePath, rarityStars);
        this.hp = maxHp;
        this.x = random.nextInt(1400) + 100;
        if (name.equals("螃蟹")) { this.y = 820; this.fishSize = 45; }
        else if (name.equals("鯊魚")) { this.y = random.nextInt(400) + 100; this.fishSize = 100; }
        else { this.y = random.nextInt(600) + 100; }
        this.waveOffset = random.nextDouble() * Math.PI * 2;
        setNewTarget();
    }

    public void setNewTarget() {
        this.targetX = random.nextInt(1500);
        this.targetY = getName().equals("螃蟹") ? 820 : random.nextInt(700) + 50;
    }

    public void updatePhysics(int px, int py) {
        if (isDead) return;
        double dx = targetX - x, dy = targetY - y, d = Math.sqrt(dx*dx + dy*dy);
        double accel = 0.05 + (getRarityStars() * 0.03); 
        if (d > 20) { velX += (dx/d)*accel; velY += (dy/d)*accel; } else setNewTarget();
        double pdx = x-px, pdy = y-py, pDist = Math.sqrt(pdx*pdx + pdy*pdy);
        if (pDist < 180) {
            double scare = 0.2 + (getRarityStars() * 0.1);
            velX += (pdx/pDist)*scare;
            if (!getName().equals("螃蟹")) velY += (pdy/pDist)*scare;
        }
        double fric = getName().equals("鯊魚") ? 0.96 : 0.94;
        velX *= fric; velY *= fric;
        x += velX;
        if (!getName().equals("螃蟹")) y += velY + Math.sin(System.currentTimeMillis()*0.003 + waveOffset)*0.5;
        else y = 820;
        if (velX > 0.1) isFacingRight = true; else if (velX < -0.1) isFacingRight = false;
        if (x<0 || x>1550) { velX *= -1; setNewTarget(); }
        if (!getName().equals("螃蟹") && (y<0 || y>850)) { velY *= -1; setNewTarget(); }
    }

    public void takeDamage(int dmg) {
        if (!isDead) {
            hp -= dmg;
            if (hp <= 0) { this.isDead = true; this.velX = 0; this.velY = 0; }
        }
    }

    public Rectangle getBounds() { return new Rectangle((int)x, (int)y, fishSize, fishSize); }
    public int getX() { return (int)x; }
    public int getY() { return (int)y; }
    public int getFishSize() { return fishSize; }
    public boolean isFacingRight() { return isFacingRight; }
    public boolean isDead() { return isDead; }
}