import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class CoinAnimation {

    private final ArrayList<BufferedImage> frames = new ArrayList<>();
    private int frameIndex = 0;

    public int x, y;
    private double vx, vy;      // 速度
    private double gravity = 0.35;

    private float alpha = 1f;   // 透明度
    private boolean finished = false;

    private BufferedImage textImg;   // 显示 +10 文字
    private int textOffsetY = -20;

    public CoinAnimation(BufferedImage sheet, int frameCount,
                         int startX, int startY,
                         int targetX, int targetY,
                         BufferedImage textImg)
    {
        int w = sheet.getWidth();
        int h = sheet.getHeight() / frameCount;

        for (int i = 0; i < frameCount; i++)
            frames.add(sheet.getSubimage(0, i * h, w, h));

        this.x = startX;
        this.y = startY;
        this.textImg = textImg;

        // 飞向目标
        this.vx = (targetX - startX) / 18.0;
        this.vy = (targetY - startY) / 18.0 - 5;
    }

    public void update() {
        if (finished) return;

        frameIndex = (frameIndex + 1) % frames.size();

        // 抛物线
        x += vx;
        y += vy;
        vy += gravity;

        // 渐隐
        alpha -= 0.03f;
        if (alpha <= 0) finished = true;
    }

    public void draw(Graphics2D g2) {
        if (finished) return;

        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        g2.drawImage(frames.get(frameIndex), x, y, null);

        if (textImg != null)
            g2.drawImage(textImg, x, y + textOffsetY, null);

        g2.setComposite(old);
    }

    public boolean isFinished() {
        return finished;
    }
}
