import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Cannon {

    /** 单个等级的炮台资源与对齐信息 */
    private static class CannonLevel {
        List<BufferedImage> idleFrames = new ArrayList<>();
        List<BufferedImage> fireFrames = new ArrayList<>();
        int width;
        int height;
        int contentBottomY; // 静止帧非透明内容的底部 y，用来贴到底部栏
    }

    // 所有等级
    private final List<CannonLevel> levels = new ArrayList<>();

    // 当前位置 & 尺寸
    private int x, y;
    private int w, h;

    // 旋转角度
    private double angle = 0;

    // 当前等级索引（0-based）
    private int levelIndex = 0;

    // 动画状态
    private int frameIdle = 0;
    private int frameFire = 0;
    private boolean shooting = false;

    // 底部栏信息缓存
    private int cachedPanelH;
    private int cachedBottomBarHeight;

    // 炮槽中心 X（由外部根据 UI 图确定）
    private int cannonCenterX;

    // 如果炮台默认朝上，用 +PI/2；如果默认朝右就不用加
    private static final double ANGLE_OFFSET = Math.PI / 2;


    /**
     * @param panelW            画布宽
     * @param panelH            画布高
     * @param bottomBarHeight   底部 UI 占用的高度（bottom.png 的高度 72）
     * @param cannonCenterX     底部 UI 上炮槽中心的 X 像素坐标（全局坐标）
     */
    public Cannon(int panelW, int panelH, int bottomBarHeight, int cannonCenterX) {

        this.cannonCenterX = cannonCenterX;
        this.cachedPanelH = panelH;
        this.cachedBottomBarHeight = bottomBarHeight;

        // ===== 添加不同等级的炮台资源 =====
        // 你现在这张图：5 帧竖排，0 静止，1~4 发射
        for(int i =1;i <=7 ;i++){
            addLevel("images/cannon"+i+".png", 5, 0, 1, 4);
        }

        setLevel(0);
    }

    /** 加载一个等级的炮台 */
    private void addLevel(String path,
                          int frameCount,
                          int idleIndex,
                          int fireStartIndex,
                          int fireEndIndex) {

        BufferedImage sheet = ImageUtil.getImage(path);
        CannonLevel level = new CannonLevel();

        int w = sheet.getWidth();
        int h = sheet.getHeight() / frameCount;

        BufferedImage[] rawFrames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            rawFrames[i] = sheet.getSubimage(0, i * h, w, h);
        }

        // 以静止帧内容中心为基准，对齐所有帧，避免旋转时“滑动”
        int idleCenterY = calcContentCenterY(rawFrames[idleIndex]);

        BufferedImage[] aligned = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            aligned[i] = alignFrameToCenter(rawFrames[i], idleCenterY);
        }

        // 对齐后，用静止帧计算内容底部 y，用于贴到底部栏上边缘
        int bottomY = calcContentBottomY(aligned[idleIndex]);

        level.idleFrames.add(aligned[idleIndex]);

        for (int i = fireStartIndex; i <= fireEndIndex && i < frameCount; i++) {
            level.fireFrames.add(aligned[i]);
        }

        level.width = w;
        level.height = h;
        level.contentBottomY = bottomY;

        levels.add(level);
    }

    /** 切换炮台等级（0-based） */
    public void setLevel(int level) {
        if (level < 0 || level >= levels.size()) return;
        this.levelIndex = level;

        CannonLevel current = levels.get(levelIndex);
        this.w = current.width;
        this.h = current.height;

        // 切等级时重新算一次位置
        updatePosition();
    }

    /** 重新计算炮台位置（居炮槽中心，贴到底部栏） */
    private void updatePosition() {
        CannonLevel current = levels.get(levelIndex);

        // 水平：炮槽中心减去半宽
        x = cannonCenterX - current.width / 2;

        // 垂直：静止帧非透明内容底部 = 画布高度 - bottomBarHeight
        y = cachedPanelH - cachedBottomBarHeight - current.contentBottomY;
    }

    /** 外部如果想微调中心位置，可以调用 */
    public void setCannonCenterX(int cannonCenterX) {
        this.cannonCenterX = cannonCenterX;
        updatePosition();
    }

    /** 计算一帧非透明内容的垂直中心 y */
    private int calcContentCenterY(BufferedImage img) {
        int height = img.getHeight();
        int width = img.getWidth();
        int top = height;
        int bottom = -1;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int alpha = (img.getRGB(x, y) >>> 24) & 0xff;
                if (alpha != 0) {
                    if (y < top) top = y;
                    if (y > bottom) bottom = y;
                }
            }
        }

        if (bottom == -1) {
            return height / 2; // 全透明兜底
        }
        return (top + bottom) / 2;
    }

    /** 计算一帧非透明内容的底部 y */
    private int calcContentBottomY(BufferedImage img) {
        int height = img.getHeight();
        int width = img.getWidth();

        for (int y = height - 1; y >= 0; y--) {
            for (int x = 0; x < width; x++) {
                int alpha = (img.getRGB(x, y) >>> 24) & 0xff;
                if (alpha != 0) {
                    return y;
                }
            }
        }
        return height - 1;
    }

    /** 将一帧在垂直方向平移，使内容中心对齐到 targetCenterY */
    private BufferedImage alignFrameToCenter(BufferedImage src, int targetCenterY) {
        int width = src.getWidth();
        int height = src.getHeight();

        int currentCenterY = calcContentCenterY(src);
        int offsetY = targetCenterY - currentCenterY;

        BufferedImage dst = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = dst.createGraphics();
        g2.drawImage(src, 0, offsetY, null);
        g2.dispose();

        return dst;
    }

    /** 鼠标移动时更新旋转角度（逻辑角度，指向鼠标） */
    public void updateRotate(int mouseX, int mouseY) {
        double cx = x + w / 2.0;
        double cy = y + h / 2.0;

        double dx = mouseX - cx;
        double dy = mouseY - cy;

        angle = Math.atan2(dy, dx);  // 逻辑上炮口应该对准的方向
    }




    /** 点击开火 */
    public void shoot() {
        shooting = true;
        frameFire = 0;
    }

    /** 每帧更新动画 */
    public void update() {
        CannonLevel current = levels.get(levelIndex);

        if (shooting) {
            frameFire++;
            if (frameFire >= current.fireFrames.size()) {
                frameFire = 0;
                shooting = false;
            }
        } else {
            frameIdle = 0; // 目前 idle 只有一帧
        }
    }

    /** 绘制炮台 */
    public void draw(Graphics g) {
        CannonLevel current = levels.get(levelIndex);
        Graphics2D g2 = (Graphics2D) g;

        BufferedImage frame = shooting
                ? current.fireFrames.get(frameFire)
                : current.idleFrames.get(frameIdle);

        double cx = x + w / 2.0;
        double cy = y + h / 2.0;

        double visualAngle = angle + ANGLE_OFFSET; // ☆ 关键：加偏移

        g2.rotate(visualAngle, cx, cy);
        g2.drawImage(frame, x, y, null);
        g2.rotate(-visualAngle, cx, cy);
    }

    /** 炮口中心，用于生成子弹 */
    /** 炮口中心，用于生成子弹 */
    public Point getMuzzlePosition() {
        double cx = x + w / 2.0;
        double cy = y + h / 2.0;

        double radius = h * 0.45; // 炮口离中心的距离，自己微调
        double visualAngle = angle + ANGLE_OFFSET; // ☆ 和 draw 保持一致

        double mx = cx + Math.cos(visualAngle) * radius;
        double my = cy + Math.sin(visualAngle) * radius;

        return new Point((int) mx, (int) my);
    }

    public double getShotAngle() {
        return angle + ANGLE_OFFSET;
    }



    public double getAngle() {
        return angle;
    }
}
