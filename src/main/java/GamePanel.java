import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel {

    private BufferedImage bgImage, bottomImage;
    private BufferedImage energyBarImage;
    private int bgWidth, bgHeight;

    private final List<Fish> fishes = new ArrayList<>();
    private final int fishNum = 20;

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


    // 底部栏内部炮槽中心
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
            fishes.add(FishFactory.spawnNormal(bgWidth, bgHeight));
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

                cannon.rotateTo(e.getX(), e.getY());
                cannon.shoot();

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

            // ===== 1. 更新鱼 =====
            for (Fish fish : fishes) {
                fish.update(bgWidth, bgHeight, freezeMode);
            }

            // ===== 2. 检测死亡鱼并触发金币动画 =====
            for (Fish fish : fishes) {
                if (fish.dead && !fish.remove) {

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
                    fish.remove = true;
                }
            }

            // ===== 3. 删除离场 + 删除 dead fish =====
            fishes.removeIf(f -> f.remove || f.isOutOfScreen(bgWidth, bgHeight));

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

            repaint();
        });




        timer.start();
    }
}
