import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Fish {

    public final FishType type;
    public final String name;
    public final int maxHp;
    public int hp;
    public final int score;
    public final int energy;

    public final boolean specialFreeze;
    public final boolean highValue;

    private boolean dead = false;
    private boolean remove = false;

    private List<BufferedImage> swimAnim = new ArrayList<>();
    private List<BufferedImage> deathAnim = new ArrayList<>();

    private int currentFrame = 0;
    private int activeAnim = 0;

    public int x, y, w, h;
    double targetX, targetY;
    double speedX, speedY;

    private static final Random random = new Random();

    public Fish(int panelW, int panelH, FishType type) {

        this.type = type;
        this.name = type.name;
        this.maxHp = type.hp;
        this.hp = type.hp;
        this.score = type.score;
        this.energy = type.energy;

        this.specialFreeze = (type == FishType.FISH7);
        this.highValue = (type == FishType.SHARK_BLUE || type == FishType.SHARK_GOLD);

        BufferedImage sheet = ImageUtil.getImage(type.spritePath);
        if (sheet == null) {
            System.err.println("Fish sprite missing: " + type.spritePath);
            return;
        }

        int totalFrames = type.totalFrames;
        this.w = sheet.getWidth();
        this.h = sheet.getHeight() / totalFrames;

        int half = totalFrames / 2;

        for (int i = 0; i < half; i++)
            swimAnim.add(sheet.getSubimage(0, i * h, w, h));

        for (int i = half; i < totalFrames; i++)
            deathAnim.add(sheet.getSubimage(0, i * h, w, h));

        boolean fromLeft = random.nextBoolean();
        if (fromLeft) {
            x = -w - random.nextInt(150);
            targetX = panelW + w + 200;
        } else {
            x = panelW + random.nextInt(150);
            targetX = -w - 200;
        }

        y = random.nextInt(panelH - h);
        targetY = random.nextInt(panelH - h);

        speedX = 2 + random.nextDouble() * 2;
        speedY = (random.nextDouble() - 0.5) * 1.5;
    }

    public void update(int panelW, int panelH) {

        if (dead) {
            currentFrame++;
            if (currentFrame >= deathAnim.size())
                remove = true;
            return;
        }

        currentFrame = (currentFrame + 1) % swimAnim.size();

        double dx = targetX - x;
        double dy = targetY - y;

        x += Math.signum(dx) * speedX;
        y += Math.signum(dy) * speedY;

        if (Math.abs(dx) < speedX && Math.abs(dy) < speedY) {
            targetX = random.nextInt(panelW);
            targetY = random.nextInt(panelH);
        }
    }

    public void hit(int dmg) {
        if (dead) return;
        hp -= dmg;
        if (hp <= 0) {
            hp = 0;
            dead = true;
            activeAnim = 1;
            currentFrame = 0;
        }
    }

    public boolean shouldRemove() {
        return remove;
    }

    public void draw(Graphics g) {
        BufferedImage frame = (!dead ? swimAnim.get(currentFrame) : deathAnim.get(currentFrame));

        boolean right = targetX > x;
        if (!right) {
            Graphics2D g2 = (Graphics2D) g;
            g2.drawImage(frame, x + w, y, -w, h, null);
        } else {
            g.drawImage(frame, x, y, null);
        }
    }

    public boolean isOutOfScreen(int w, int h) {
        return x < -this.w * 2 || x > w + this.w * 2;
    }
}
