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

    private final java.util.List<Bullet> bullets = new ArrayList<>();
    private final List<Web> webs = new ArrayList<>();

    private final Random random = new Random();

    // 鱼群刷新的计时器（按帧计数）
    private int groupTimer = 0;

    // 鱼群刷新的间隔帧数（比如 180 帧 ≈ 3 秒，取决于 Timer 间隔）
    private static final int GROUP_INTERVAL = 180;


    // 底部栏内部炮槽中心（
    private final int bottomBarLocalCannonX = 427;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // --- 抗锯齿 ---
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // --- 1. 绘制背景（居中显示） ---
        int x = (getWidth() - bgImage.getWidth()) / 2;
        int y = (getHeight() - bgImage.getHeight()) / 2;
        g2.drawImage(bgImage, x, y, null);

        // --- 2. 加水下蓝色渐变蒙版（更梦幻） ---
        GradientPaint gp = new GradientPaint(
                0, 0, new Color(0, 80, 200, 70),
                0, getHeight(), new Color(0, 0, 50, 180)
        );
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // --- 3. 添加光效（上方亮、下方暗）---
        GradientPaint light = new GradientPaint(
                getWidth() / 2f, 0, new Color(255, 255, 255, 120),
                getWidth() / 2f, getHeight() / 2f, new Color(255, 255, 255, 0)
        );
        g2.setPaint(light);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // --- 4. 绘制鱼 ---
        for (Fish fish : fishes) {
            fish.draw(g2);
        }

        // --- 5. 底部栏 ---
        int bottomBarX = (bgWidth - bottomImage.getWidth()) / 2;
        int bottomBarY = bgHeight - bottomBarHeight - 20; // 稍微抬高一点更美观
        g2.drawImage(bottomImage, bottomBarX, bottomBarY, null);

        // --- 6. 绘制炮台 ---
        cannon.draw(g2);
        // ---7  绘制子弹 ---
        for (Bullet b : bullets) {
            b.draw(g2);
        }
        // --- 8. 绘制渔网 ---
        for (Web w : webs) {
            w.draw(g2);
        }

// 可以顺便删除已经标记为 remove 的网
        webs.removeIf(Web::isRemove);


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

            // 获取一条鱼（使用工厂）
            Fish fish = FishFactory.spawnNormal(bgWidth, bgHeight);

            // —— 覆盖 Fish 自己的出生位置，强制做成鱼群 ——
            if (fromLeft) {
                fish.x = -fish.w - i * 40;                 // 从左侧排队进场
            } else {
                fish.x = panelW + fish.w + i * 40;         // 从右侧排队进场
            }

            fish.y = baseY + random.nextInt(80) - 40;      // 上下微乱

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
            fishes.add(FishFactory.spawnNormal(bgWidth, bgHeight)
            );
        }

        // ===== 计算底部栏绘制位置（固定窗口，不需要随窗口变化） =====
        int bottomBarX = (bgWidth - bottomImage.getWidth()) / 2;
        int cannonCenterX = bottomBarX + bottomBarLocalCannonX;

        // 初始化炮台
        cannon = new Cannon(bgWidth, bgHeight, bottomBarHeight, cannonCenterX);

        // 炮台旋转监听
        addMouseMotionListener(new MouseAdapter() {
        });


        // 开火监听
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {

                // 如果点的是按钮，则不旋转、不发射
                if (cannon.onButtonClick(e.getX(), e.getY()))
                    return;

                // 炮口立即指向鼠标
                cannon.rotateTo(e.getX(), e.getY());

                // 发射
                cannon.shoot();
                // 获取炮口尖端
                Point muzzle = cannon.getMuzzlePoint();
                double angle = cannon.getAngle();
                int cannonLevel = cannon.getLevel();
                // 发射子弹
                // 发射子弹（使用角度而不是目标点）
                bullets.add(new Bullet(muzzle.x, muzzle.y, angle,cannonLevel));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                cannon.onMouseReleased();
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
                fishes.add(FishFactory.spawnNormal(bgWidth, bgHeight)
                );
            }

            // 4. 鱼群计时 & 不停刷鱼群
            groupTimer++;
            if (groupTimer >= GROUP_INTERVAL) {
                spawnFishGroup(8);   // 每次刷 8 条鱼的鱼群
                groupTimer = 0;
            }

            // 5. 更新炮台动画
            cannon.update();
            //6.更新子弹
            for (Bullet b : bullets) {
                b.update();
                // 检查每条鱼
                for (Fish f : fishes) {
                    if (!b.isVanished() && b.intersectsFish(f)) {
                        // 生成网（等级等于炮台等级）
                        Web web = b.createWeb();
                        webs.add(web);
                        b.vanish();   //  子弹不再绘制
                        b.setRemove(true);     // 让子弹停下
                        break; // 子弹只撞一次就结束
                    }
                }
            }
            // 删除出屏幕的子弹
            bullets.removeIf(b -> b.isOutOfScreen(bgWidth, bgHeight));

            // 更新渔网
            for (Web web : webs) {
                web.update();
            }
             // 移除已经透明消失的网
            webs.removeIf(Web::isRemove);


            repaint();

        });
        timer.start();

    }
}