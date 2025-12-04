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
    private BufferedImage energyBarImage;
    private int bgWidth, bgHeight;

    private final int fishNum = 20;
    private final List<Fish> fishes = new ArrayList<>();


    // 玩家
    private Player player;
    private ScoreRenderer scoreRenderer;

    // 炮台
    private Cannon cannon;
    private int bottomBarHeight;

    // 金币动画
    private CoinManager coinManager = new CoinManager();
    private BufferedImage coinSilverSheet;
    private BufferedImage coinGoldSheet;
    private BufferedImage coinTextImg;


    // 特殊事件
    private boolean feastMode = false;  // 鱼群大餐
    private long feastEndTime = 0;
    private boolean freezeMode = false; // 定身事件
    private long freezeEndTime = 0;

    // ======= 大餐提示显示 =======
    private String feastHintText = null;  // 当前提示内容（瞬间设置）
    private long feastHintEndTime = 0;    // 提示结束时间
    private float feastHintAlpha = 1.0f;  // 透明度（淡出）
    private boolean feastTriggered = false;  // 是否已经触发过大餐

    //子弹
    private final java.util.List<Bullet> bullets = new ArrayList<>();
    //网
    private final List<Web> webs = new ArrayList<>();

    // 底部栏内部炮槽中心（
    private final int bottomBarLocalCannonX = 427;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // 抗锯齿
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. 背景
        int x = (getWidth() - bgImage.getWidth()) / 2;
        int y = (getHeight() - bgImage.getHeight()) / 2;
        g2.drawImage(bgImage, x, y, null);

        // 2. 渐变蒙版
        GradientPaint gp = new GradientPaint(
                0, 0, new Color(0, 80, 200, 70),
                0, getHeight(), new Color(0, 0, 50, 180)
        );
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // 3. 光效
        GradientPaint light = new GradientPaint(
                getWidth() / 2f, 0,
                new Color(255, 255, 255, 120),
                getWidth() / 2f, getHeight() / 2f,
                new Color(255, 255, 255, 0)
        );
        g2.setPaint(light);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // 4. 鱼
        for (Fish f : fishes) f.draw(g2);

        // 5. 底栏
        int bottomBarX = (bgWidth - bottomImage.getWidth()) / 2;
        int bottomBarY = bgHeight - bottomBarHeight - 20;
        g2.drawImage(bottomImage, bottomBarX, bottomBarY, null);

        // 6. 炮台
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


        // 7. HUD —— 积分
        // 在底部栏左侧显示积分
        int scoreX = bottomBarX + 20;
        int scoreY = bottomBarY + 45;
        String paddedScore = String.format("%06d", player.score);
        scoreRenderer.drawScore(g2, paddedScore, scoreX, scoreY);



        // 8. 能量条
        // --- 能量条展示 ---
        int barX = 673;
        int barY = 720;

        int fullW = energyBarImage.getWidth();
        int fullH = energyBarImage.getHeight();

        // 当前能量百分比
        float percent = player.getEnergyPercent();
        int showW = (int) (fullW * percent);

        // 裁剪并绘制能量条
        g2.setClip(barX, barY, showW, fullH);
        g2.drawImage(energyBarImage, barX, barY, null);

        // 清除剪裁
        g2.setClip(null);

        // 9.显示大餐提示
        if (feastHintText != null) {

            long now = System.currentTimeMillis();

            if (now < feastHintEndTime) {

                // 透明度从 1.0 逐渐降低到 0.0
                float progress = (feastHintEndTime - now) / 1500f;
                feastHintAlpha = Math.max(0f, Math.min(1f, progress));  // 限制范围 0-1


                Graphics2D g2d = (Graphics2D) g2.create();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, feastHintAlpha));

                g2d.setFont(new Font("微软雅黑", Font.BOLD, 60));
                g2d.setColor(new Color(255, 215, 0)); // 金黄

                // 居中显示
                int strW = g2d.getFontMetrics().stringWidth(feastHintText);
                int strX = (bgWidth - strW) / 2;
                int strY = bgHeight / 2;

                g2d.drawString(feastHintText, strX, strY);
                g2d.dispose();
            } else {
                feastHintText = null; // 显示结束
            }
        }

        //  10.金币动画绘制
        coinManager.draw(g2);



    }


    private void showFeastHint(String text) {
        feastHintText = text;
        feastHintEndTime = System.currentTimeMillis() + 3000; // 显示 3 秒
        feastHintAlpha = 1.0f;
    }


    // 启动鱼群大餐
    private void triggerFeast() {
        feastMode = true;
        showFeastHint("鱼群大餐！");
        for (int i = 0; i < 40; i++) {
            fishes.add(FishFactory.spawnNormal(bgWidth, bgHeight));
        }
        for (int i = 0; i < 15; i++) {
            fishes.add(FishFactory.spawnFeast(bgWidth, bgHeight));
        }
        feastEndTime = System.currentTimeMillis() + 10000;
    }

    public void triggerFreeze() {
        freezeMode = true;
        freezeEndTime = System.currentTimeMillis() + 3000;
    }

    public int getBgHeight() {
        return bgHeight;
    }

    public int getBgWidth() {
        return bgWidth;
    }

    // 构造方法
    public GamePanel() {

        // 加载资源
        bgImage = ImageUtil.getImage("images/game_bg_2_hd.jpg");
        bottomImage = ImageUtil.getImage("images/bottom-bar.png");
        energyBarImage = ImageUtil.getImage("images/energy-bar.png");
        coinSilverSheet = ImageUtil.getImage("images/coinAni1.png");
        coinGoldSheet = ImageUtil.getImage("images/coinAni2.png");
        coinTextImg = ImageUtil.getImage("images/coinText.png");


        bgWidth = bgImage.getWidth();
        bgHeight = bgImage.getHeight();
        bottomBarHeight = bottomImage.getHeight();

        player = new Player();
        scoreRenderer = new ScoreRenderer("images/number_black.png");


        // 初始化鱼
        for (int i = 0; i < fishNum; i++) {
            fishes.add(FishFactory.spawnNormal(bgWidth, bgHeight)
            );
        }

        // 炮台
        int cannonCenterX = (bgWidth - bottomImage.getWidth()) / 2 + bottomBarLocalCannonX;
        cannon = new Cannon(bgWidth, bgHeight, bottomBarHeight, cannonCenterX);

        // 鼠标事件
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {

                // 射击按钮判断
                if (cannon.onButtonClick(e.getX(), e.getY())) return;

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
                bullets.add(new Bullet(muzzle.x,muzzle.y,angle,cannonLevel));

                // 这里将来加入子弹消耗能量 player.useEnergy()
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                cannon.onMouseReleased();
            }
        });

        // 游戏循环
        Timer timer = new Timer(100, e -> {
            long now = System.currentTimeMillis();
            int w = bgWidth;
            int h = bgHeight;

            // 1. 更新所有鱼
            for (Fish fish : fishes) {
                fish.update(bgWidth, bgHeight, freezeMode);
            }

            // ===== 2. 检测死亡鱼并触发金币动画 =====
            for (Fish fish : fishes) {
                if (fish.isDead() && !fish.shouldRemove()) {

                    // 底部栏金币槽目标位置
                    int bottomBarX = (bgWidth - bottomImage.getWidth()) / 2;
                    int targetX = bottomBarX + 70;  // 调到你的金币槽位置
                    int targetY = bgHeight - bottomBarHeight - 40;

                    BufferedImage sheet = fish.highValue ? coinGoldSheet : coinSilverSheet;

                    coinManager.add(new CoinAnimation(
                            sheet,
                            10,
                            fish.x, fish.y,
                            targetX, targetY,
                            coinTextImg
                    ));

                    // 标记为已处理，避免重复加金币
                    fish.markRemove();
                }
            }

            // ===== 3. 删除离场 + 删除 dead fish =====
            fishes.removeIf(f -> f.shouldRemove() || f.isOutOfScreen(bgWidth, bgHeight));

            // ===== 4. 大餐触发 =====
            if (!feastTriggered && player.score >= 2000) {
                triggerFeast();
                feastTriggered = true;
            }

            // ===== 5. 大餐结束 =====
            if (feastMode && now > feastEndTime) {
                feastMode = false;
                showFeastHint("大餐结束！");
                fishes.clear();
            }

            // ===== 6. 补鱼 =====
            while (fishes.size() < 15) {
                fishes.add(FishFactory.spawnNormal(bgWidth, bgHeight));
            }

            // ===== 7. 定身结束 =====
            if (freezeMode && now > freezeEndTime) {
                freezeMode = false;
            }

            // ===== 8. 金币动画更新 =====
            coinManager.update();

            // ===== 9. 炮台动画 =====
            cannon.update();

            // ===== 10.更新子弹 =====
            for (Bullet b : bullets) {
                b.update();
                // 检查每条鱼
                for (Fish f : fishes) {
                    if (!b.isVanished() && !f.isDead() && !f.isDying()&& b.intersectsFish(f)) {
                        // 生成网（等级等于炮台等级）
                        Web web = b.createWeb();
                        webs.add(web);
                        b.vanish();   //  子弹不再绘制
                        b.setRemove(true);     // 让子弹停下
                        // 渔网创建瞬间就攻击所有碰到网的鱼
                        Rectangle wr = web.getBounds();
                        int atk = web.getAttack();

                        for (Fish fish2 : fishes) {
                            if (!fish2.shouldRemove() && wr.intersects(fish2.getBounds())) {
                                fish2.hit(atk);
                            }
                        }



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
            // 让渔网伤害鱼
            for (Web web : webs) {
                if (web.isRemove()) continue; // 完全消失就不攻击
                Rectangle wr = web.getBounds();
                int atk = web.getAttack();// 从 WebType 获取攻击力

                for (Fish fish : fishes) {
                    // 死鱼或已被移除不再计算
                    if (fish.shouldRemove()) continue;

                    // 网覆盖鱼 → 扣血
                    if (wr.intersects(fish.getBounds())) {
                        fish.hit(atk); // 每帧只会攻击一次
                    }
                }

                // 每帧更新结束，统一重置标记
                for (Fish fish : fishes) {
                    fish.resetAttackFlag();
                }
            }



            repaint();

        });
        timer.start();

    }
}