import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Fish {

    private List<BufferedImage> fishAnimA = new ArrayList<>();
    private List<BufferedImage> fishAnimB = new ArrayList<>();

    private int totalFrames;
    private int currentFrame = 0;

    private int x, y, w, h;
    private int speed;
    private boolean movingRight;   // true = 向右，false = 向左
    private int activeAnim = 0;    // 0 = 动画A；1 = 动画B

    private int targetY;           // 当前目标Y位置
    private double verticalSpeed;  // 垂直移动速度，随机变化

    private static final int[] FRAME_COUNTS = {8, 8, 8, 8, 8, 12, 10, 12, 12, 10};
    private static final Random random = new Random();

    public Fish(int panelW, int panelH) {
        int index = random.nextInt(FRAME_COUNTS.length) + 1;
        BufferedImage fishImg = ImageUtil.getImage("images/fish" + index + ".png");
        if (fishImg == null) {
            System.err.println("Missing fish image: fish" + index + ".png");
            return;
        }

        totalFrames = FRAME_COUNTS[index - 1];
        w = fishImg.getWidth();
        h = fishImg.getHeight() / totalFrames;  // 单帧高度

        // 随机初始位置
        x = random.nextInt(panelW - w);
        y = random.nextInt(panelH - h);

        // 初始目标Y位置和垂直速度
        targetY = y;
        verticalSpeed = 0.5 + random.nextDouble(); // 0.5 ~ 1.5 px/frame

        // 随机速度和方向
        speed = 1 + random.nextInt(3);  // 1~3
        movingRight = random.nextBoolean();

        // 随机选择初始动画
        activeAnim = random.nextBoolean() ? 0 : 1;

        int half = totalFrames / 2;
        for (int i = 0; i < half; i++) fishAnimA.add(fishImg.getSubimage(0, i * h, w, h));
        for (int i = half; i < totalFrames; i++) fishAnimB.add(fishImg.getSubimage(0, i * h, w, h));
    }

    // 更新鱼状态
    public void update(int panelW, int panelH) {
        // 更新帧动画
        currentFrame++;
        if (activeAnim == 0 && !fishAnimB.isEmpty()) currentFrame %= fishAnimB.size();
        if (activeAnim == 1 && !fishAnimB.isEmpty()) currentFrame %= fishAnimB.size();

        // 水平移动
        x += movingRight ? speed : -speed;

        // 随机上下摆动
        if (Math.abs(y - targetY) < 1) {
            // 到达目标Y，生成下一个目标Y
            targetY = random.nextInt(panelH - h);
            verticalSpeed = 0.5 + random.nextDouble(); // 随机速度
        }
        if (y < targetY) y += verticalSpeed;
        if (y > targetY) y -= verticalSpeed;

        // 碰到边界翻转方向
        if (x > panelW - w) {
            movingRight = false;
            x = panelW - w;
            playAnimB();
        } else if (x < 0) {
            movingRight = true;
            x = 0;
            playAnimA();
        }
    }

    // 绘制鱼
    public void draw(Graphics g) {
        if (fishAnimB.isEmpty() || fishAnimB.isEmpty()) return;

        BufferedImage frame = activeAnim == 0 ? fishAnimB.get(currentFrame) : fishAnimB.get(currentFrame);

        // 水平翻转
        if (!movingRight) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.drawImage(frame, x + w, y, -w, h, null); // 翻转
        } else {
            g.drawImage(frame, x, y, null);
        }
    }

    public void playAnimA() { activeAnim = 0; currentFrame = 0; }
    public void playAnimB() { activeAnim = 1; currentFrame = 0; }
}
