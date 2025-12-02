import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel {

    private BufferedImage bgImage, bottomImage;
    private int bgWidth, bgHeight;

    private final int fishNum = 20;
    private final List<Fish> fishes = new ArrayList<>();

    private Cannon cannon;
    private int bottomBarHeight;

    // 在 GamePanel 类里加上（和 fishes、cannon 同级）
    private final Random random = new Random();

    // 鱼群刷新的计时器（按帧计数）
    private int groupTimer = 0;

    // 鱼群刷新的间隔帧数（比如 180 帧 ≈ 3 秒，取决于 Timer 间隔）
    private static final int GROUP_INTERVAL = 180;


    // 底部栏内部炮槽中心（我们已经精确计算过了）
    private final int bottomBarLocalCannonX = 427;


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 背景（无缩放）
        g.drawImage(bgImage, 0, 0, null);

        // 鱼
        for (Fish fish : fishes) {
            fish.draw(g);
        }

        // 炮台
        cannon.draw(g);

        // 底部栏
        int bottomBarX = (bgWidth - bottomImage.getWidth()) / 2;
        int bottomBarY = bgHeight - bottomBarHeight - 30;
        g.drawImage(bottomImage, bottomBarX, bottomBarY, null);
    }

    /**
     * 生成一批鱼群（同方向成群进入）
     */
    private void spawnFishGroup(int count) {

        int panelW = bgWidth;
        int panelH = bgHeight;

        // 随机决定从左边还是右边进入
        boolean fromLeft = random.nextBoolean();

        // 鱼群“主路线”的 Y
        int baseY = random.nextInt(panelH - 100) + 50;

        // 统一水平速度
        double speedX = 2 + random.nextDouble() * 1.5;  // 2~3.5
        if (!fromLeft) speedX = -speedX;

        for (int i = 0; i < count; i++) {

            Fish fish = new Fish(panelW, panelH);

            // —— 覆盖 Fish 自己的出生位置，强制做成鱼群 ——
            if (fromLeft) {
                fish.x = -fish.w - i * 40;                 // 从左侧排队进场
            } else {
                fish.x = panelW + fish.w + i * 40;         // 从右侧排队进场
            }

            fish.y = baseY + random.nextInt(80) - 40;      // 上下轻微乱一点

            fish.targetX = fromLeft ? panelW + 200 : -200; // 目标游出对侧
            fish.targetY = fish.y + random.nextInt(120) - 60;

            fish.speedX = speedX;
            fish.speedY = (random.nextDouble() - 0.5) * 0.8;

            fishes.add(fish);
        }
    }


    public int getBgHeight() {
        return bgHeight;
    }

    public int getBgWidth() {
        return bgWidth;
    }


    public GamePanel() {

        // === 加载背景和底部栏 ===
        bgImage = ImageUtil.getImage("images/game_bg_2_hd.jpg");
        bottomImage = ImageUtil.getImage("images/bottom-bar.png");

        bgWidth = bgImage.getWidth();
        bgHeight = bgImage.getHeight();
        bottomBarHeight = bottomImage.getHeight();

        // 初始化鱼
        for (int i = 0; i < fishNum; i++) {
            fishes.add(new Fish(bgWidth, bgHeight));
        }

        // ===== 计算底部栏绘制位置（固定窗口，不需要随窗口变化） =====
        int bottomBarX = (bgWidth - bottomImage.getWidth()) / 2;
        int cannonCenterX = bottomBarX + bottomBarLocalCannonX;

        // 初始化炮台
        cannon = new Cannon(bgWidth, bgHeight, bottomBarHeight, cannonCenterX);

        // 炮台旋转监听
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                cannon.updateRotate(e.getX(), e.getY());
            }
        });

        // 开火监听
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                cannon.shoot();
            }
        });

        // 定时器刷新动画（固定窗口大小，不需要更新位置）
        Timer timer = new Timer(100, e -> {

            int w = bgWidth;
            int h = bgHeight;

            // 1. 更新所有鱼
            for (Fish fish : fishes) {
                fish.update(w, h);
            }

            // 2. 删除游出屏幕的鱼
            fishes.removeIf(f -> f.isOutOfScreen(w, h));

            // 3. 保持零散鱼数量
            while (fishes.size() < 15) {
                fishes.add(new Fish(w, h));
            }

            // 4. 鱼群计时 & 不停刷鱼群
            groupTimer++;
            if (groupTimer >= GROUP_INTERVAL) {
                spawnFishGroup(8);   // 每次刷 8 条鱼的鱼群
                groupTimer = 0;
            }

            // 5. 更新炮台动画
            cannon.update();

            repaint();

        });
        timer.start();

    }
}