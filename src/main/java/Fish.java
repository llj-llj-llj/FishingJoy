import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Fish {

    private List<BufferedImage> swimAnim = new ArrayList<>();
    private List<BufferedImage> deathAnim = new ArrayList<>();

    private int totalFrames;
    private int currentFrame = 0;

    int x;
    int y;
    int w;
    private int h;
    private int activeAnim = 0;    // 刚开始默认 animA

    double targetX;
    double targetY;   // 当前目标点
    double speedX;
    double speedY;     // 每帧移动速度

    private static final int[] FRAME_COUNTS = {8, 8, 8, 8, 8, 12, 10, 12, 12, 10};
    static final Random random = new Random();

    public Fish(int panelW, int panelH) {

        int index = random.nextInt(FRAME_COUNTS.length) + 1;
        BufferedImage sheet = ImageUtil.getImage("images/fish" + index + ".png");
        if (sheet == null) return;

        totalFrames = FRAME_COUNTS[index - 1];
        w = sheet.getWidth();
        h = sheet.getHeight() / totalFrames;

        int half = totalFrames / 2;
        for (int i = 0; i < half; i++) swimAnim.add(sheet.getSubimage(0, i * h, w, h));
        for (int i = half; i < totalFrames; i++) deathAnim.add(sheet.getSubimage(0, i * h, w, h));

        // =====================================================
        // 关键：从屏幕两侧生成
        // =====================================================
        boolean fromLeft = random.nextBoolean();

        if (fromLeft) {
            x = -w - random.nextInt(150);            // 左边界外出生
            targetX = panelW + w + 150;              // 朝右游出
        } else {
            x = panelW + random.nextInt(150);        // 右边界外出生
            targetX = -w - 150;                      // 朝左游出
        }

        y = random.nextInt(panelH - h);              // 垂直位置随机

        // 随机垂直偏移游动目标（更自然）
        targetY = random.nextInt(panelH - h);

        // 基础速度
        speedX = 2 + random.nextDouble() * 2;        // 2~4
        speedY = (random.nextDouble() - 0.5) * 1.5;  // 上下漂浮 -0.75~0.75

        activeAnim = 0;
    }


    // 更新鱼状态
    public void update(int panelW, int panelH) {
        currentFrame++;
        if (activeAnim == 0 && !swimAnim.isEmpty()) currentFrame %= swimAnim.size();
        if (activeAnim == 1 && !deathAnim.isEmpty()) currentFrame %= deathAnim.size();

        // 向目标点移动
        double dx = targetX - x;
        double dy = targetY - y;

        // 水平移动
        if (Math.abs(dx) < speedX) {
            x = (int) targetX;
        } else {
            x += dx > 0 ? speedX : -speedX;
        }

        // 垂直移动
        if (Math.abs(dy) < speedY) {
            y = (int) targetY;
        } else {
            y += dy > 0 ? speedY : -speedY;
        }

        // 如果到达目标点，生成新目标
        if ((int)x == (int)targetX && (int)y == (int)targetY) {
            targetX = x + random.nextInt(200) - 100; // ±100随机偏移
            targetY = y + random.nextInt(200) - 100;
            // 限制在屏幕内
            if (targetX < 0) targetX = 0;
            if (targetX > panelW - w) targetX = panelW - w;
            if (targetY < 0) targetY = 0;
            if (targetY > panelH - h) targetY = panelH - h;

            // 随机更新速度
            speedX = 1 + random.nextDouble() * 2;
            speedY = 0.5 + random.nextDouble();
        }
    }

    // 绘制鱼
    public void draw(Graphics g) {
        if (swimAnim.isEmpty() || deathAnim.isEmpty()) return;

        BufferedImage frame = activeAnim == 0 ? swimAnim.get(currentFrame) : deathAnim.get(currentFrame);

        // 根据目标点判断左右翻转
        boolean movingRight = targetX > x;
        if (!movingRight) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.drawImage(frame, x + w, y, -w, h, null);
        } else {
            g.drawImage(frame, x, y, null);
        }
    }

    public boolean isOutOfScreen(int panelW, int panelH) {
        return x < -w * 2 || x > panelW + w * 2;
    }


    public void playSwimAnim() { activeAnim = 0; currentFrame = 0; }
    public void playDeathAnim() { activeAnim = 1; currentFrame = 0; }
}
