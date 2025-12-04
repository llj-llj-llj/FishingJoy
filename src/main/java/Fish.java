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

    private boolean dying = false;
    private boolean dead = false;
    private boolean remove = false;
    private boolean attackedThisFrame = false; // 新增，每帧只允许被攻击一次


    private List<BufferedImage> swimAnim = new ArrayList<>();
    private List<BufferedImage> deathAnim = new ArrayList<>();

    public int currentFrame = 0;
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

        if (dying) {
            //死亡动画逐帧播放
            currentFrame++;

            if (currentFrame >= deathAnim.size()) {
                dying = false;
                dead = true;       // 动画结束：真正死
            }
            return;
        }
        if (dead) return;// 完全死亡，不再动、不再画

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
    /** 尝试攻击鱼，确保每帧只攻击一次 */
    public void hit(int dmg) {
        if (dead||dying|| attackedThisFrame) return;
        hp -= dmg;
        if (hp <= 0) {
            hp = 0;
            dying = true;
            activeAnim = 1;
            currentFrame = 0;
        }
        attackedThisFrame = true; // 标记本帧已被攻击
    }
    /** 重置每帧攻击标记 */
    public void resetAttackFlag() {
        attackedThisFrame = false;
    }

    public boolean shouldRemove() {
        return remove;
    }
    public void markRemove() {
        remove = true;
    }
    public void draw(Graphics g) {


        BufferedImage frame ;
        // 正在播放死亡动画
        if (dying) {
            int index = Math.min(currentFrame, deathAnim.size() - 1);
            frame = deathAnim.get(index);
        }// 完全死亡：不绘制
        else if (dead){ return;}
        // 正常状态
        else {
            frame = swimAnim.get(currentFrame);
        }
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
    public Rectangle getBounds() {
        return new Rectangle(x, y, w, h);
    }

    public boolean isDead() {
        return dead;
    }

    public boolean isDying() {
        return dying;
    }



}
