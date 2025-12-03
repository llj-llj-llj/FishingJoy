import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class Bullet {

    public double x, y;       // 当前坐标
    private double vx, vy;    // 速度
    private double angle;     // 旋转角度
    private final double speed =30 ;

    private BufferedImage img;
    private boolean remove = false;
    private boolean vanished = false;
    private int level;
    /** 构造方法：起点坐标 + 角度 */
    public Bullet(double startX, double startY, double angle,int cannonLevel) {
        this.x = startX;
        this.y = startY;
        this.angle = angle;
        this.level = cannonLevel;
        // ----- 根据炮台等级读取图片 -----
        String imgPath = "images/bullet"+cannonLevel+".png";
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(imgPath);
            if (is != null) {
                img = ImageIO.read(is);
            } else {
                System.err.println("无法加载子弹图片: " + imgPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 根据角度计算速度分量
        this.vx = Math.cos(angle) * speed;
        this.vy = Math.sin(angle) * speed;

    }

    // 更新位置
    public void update() {
        if (remove || vanished) return; // 已经碰撞就不再移动
        x += vx;
        y += vy;
    }

    // 绘制子弹（带旋转）
    public void draw(Graphics g) {

            if (vanished) return;   // 已消失：不要画

        if (img != null) {
            Graphics2D g2 = (Graphics2D) g;

            // 保存当前变换
            AffineTransform oldTransform = g2.getTransform();

            // 平移到子弹位置
            g2.translate(x, y);

            // 旋转（子弹图片朝上，所以需要加上Math.PI/2）
            g2.rotate(angle + Math.PI/2);

            // 绘制图片，使图片中心对齐旋转点
            g2.drawImage(img, -img.getWidth()/2, -img.getHeight()/2, null);

            // 恢复变换
            g2.setTransform(oldTransform);
        }
    }

    public boolean isOutOfScreen(int panelW, int panelH) {
        return x < -50 || x > panelW + 50 || y < -50 || y > panelH + 50;
    }

    public boolean isRemove() {
        return remove;
    }

    public void setRemove(boolean remove) {
        this.remove = remove;
    }
    public boolean shouldRemove() {
        return remove;
    }

    public void vanish() {
        this.vanished = true;
    }

    public boolean isVanished() {
        return vanished;
    }
    /** 获取边界（用于碰撞检测） */
    public Rectangle getBounds() {
        if (img == null) {
            return new Rectangle((int)x, (int)y, 10, 10);
        }
        return new Rectangle((int)x - img.getWidth()/2, (int)y - img.getHeight()/2,
                img.getWidth(), img.getHeight());
    }
    public boolean intersectsFish(Fish fish) {
        // 子弹弹头位置（朝向角度的最前端）
        double tipX = x + Math.cos(angle) * (img.getWidth() / 2);
        double tipY = y + Math.sin(angle) * (img.getHeight() / 2);

        Rectangle fishRect = fish.getBounds();

        return fishRect.contains(tipX, tipY);
    }

    public Web createWeb() {
        return new Web((int)x, (int)y, level);
    }

}