import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;

public class Web {

    private BufferedImage image;
    private int x, y;
    private int level;
    private boolean remove = false;
    private WebType type;

    private long createTime;       // 创建时间
    private float alpha = 1f;      // 当前透明度
    private static final long FADE_TIME = 300;  // 渐隐时间（毫秒）

    public Web(int x, int y, int level) {
        this.type = WebType.fromLevel(level);
        this.x = x;
        this.y = y;
        this.level = Math.max(1, Math.min(level, 7));
        this.createTime = System.currentTimeMillis();

        loadImage();
    }

    private void loadImage() {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(type.spritePath);
            if (is != null) {
                image = ImageIO.read(is);
            } else {
                System.err.println("无法加载网图片: " +type.spritePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 更新透明度和移除状态 */
    public void update() {
        long elapsed = System.currentTimeMillis() - createTime;

        // 渐隐
        alpha = 1f - Math.min(1f, elapsed / (float) FADE_TIME);
        if (alpha <= 0f) {
            remove = true;
        }
    }

    public boolean isRemove() {
        return remove;
    }

    /** 绘制渔网（带透明度） */
    public void draw(Graphics g) {
        if (image == null || remove) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.drawImage(image, x - image.getWidth() / 2, y - image.getHeight() / 2, null);
        g2.dispose();
    }

    /** 获取边界（碰撞检测用） */
    public Rectangle getBounds() {
        if (image == null) return new Rectangle(x, y, 1, 1);
        int bw = image.getWidth() / 2;   // 改成一半
        int bh = image.getHeight() / 2;
        return new Rectangle(x - bw/2, y - bh/2,
                bw, bh);
    }
    /** 给外部提供攻击力接口 */
    public int getAttack() {
        return type.attack;
    }

}
