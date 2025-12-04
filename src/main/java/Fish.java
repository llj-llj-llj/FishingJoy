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
    public int score;
    public final int energy;

    public boolean specialFreeze = false; // 是否为定身鱼（水母）
    public boolean highValue;

    public boolean dead = false;
    public boolean remove = false;

    public List<BufferedImage> swimAnim = new ArrayList<>();
    public List<BufferedImage> deathAnim = new ArrayList<>();

    public int currentFrame = 0;

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

        //  定身鱼
        this.specialFreeze = (type == FishType.FISH7);

        //  高价值鱼
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

    /** 更新鱼状态 */
    public void update(int panelW, int panelH, boolean freezeMode) {

        // 死亡动画
        if (dead) {
            currentFrame++;
            if (currentFrame >= deathAnim.size())
                remove = true;
            return;
        }

        //   冻结效果：普通鱼停住，定身鱼不受影响
        if (freezeMode && !specialFreeze) {
            currentFrame = (currentFrame + 1) % swimAnim.size(); // 还在播放动画
            return;
        }

        // 正常游动
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

    /** 被击中 */
    public void hit(int dmg) {
        if (dead) return;

        hp -= dmg;
        if (hp <= 0) {
            hp = 0;
            dead = true;
            currentFrame = 0;
        }
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
