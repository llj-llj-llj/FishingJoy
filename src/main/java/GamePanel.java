import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel {

    BufferedImage bgImage, bottomImage;
    private int bgWidth, bgHeight;

    private final int fishNum = 20;
    private final List<Fish> fishes = new ArrayList<>();

    public GamePanel() {

        // 加载背景与底部栏
        bgImage = ImageUtil.getImage("images/game_bg_2_hd.jpg");
        bottomImage = ImageUtil.getImage("images/bottom-bar.png");

        if (bgImage != null) {
            bgWidth = bgImage.getWidth();
            bgHeight = bgImage.getHeight();
        }

        // 初始化鱼
        for (int i = 0; i < fishNum; i++) {
            fishes.add(new Fish(bgWidth, bgHeight));
        }

        // 启动定时器（每 200ms 更新一次）
        Timer timer = new Timer(200, e -> {
            for (Fish fish : fishes) {
                fish.update(bgWidth,bgHeight);
            }
            repaint();
        });
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 背景
        // 动态获取当前 panel 宽高
        int w = getWidth();
        int h = getHeight();

        // 根据 panel 实时缩放绘制背景图
        g.drawImage(bgImage, 0, 0, w, h, null);

        // 所有鱼
        for (Fish fish : fishes) {
            fish.draw(g);
        }

        // 底部栏
        int bottomX = (w - bottomImage.getWidth()) / 2;
        int bottomY = h - bottomImage.getHeight();
        g.drawImage(bottomImage, bottomX, bottomY, this);
    }

    public int getBgHeight() {
        return bgHeight;
    }

    public int getBgWidth() {
        return bgWidth;
    }
}
