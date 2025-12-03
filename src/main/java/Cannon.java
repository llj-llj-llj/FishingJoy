import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Cannon {

    /** 单个等级的炮台资源 */
    private static class CannonLevel {
        List<BufferedImage> idleFrames = new ArrayList<>();
        List<BufferedImage> fireFrames = new ArrayList<>();
        int width;
        int height;
        int contentBottomY;
    }

    private final List<CannonLevel> levels = new ArrayList<>();

    private int x, y;
    private int w, h;



    private double angle = 0;  // 炮台实际旋转角度
    private static final double ANGLE_OFFSET = Math.PI / 2;

    private int levelIndex = 0;

    private int frameFire = 0;
    private boolean shooting = false;

    private int cachedPanelH;
    private int cachedBottomBarHeight;
    private int cannonCenterX;

    // 按钮
    private BufferedImage btnAddNormal, btnAddPressed;
    private BufferedImage btnSubNormal, btnSubPressed;

    private Rectangle rectAdd, rectSub;
    private boolean addPressed = false;
    private boolean subPressed = false;


    public Cannon(int panelW, int panelH, int bottomBarHeight, int cannonCenterX) {

        this.cachedPanelH = panelH;
        this.cachedBottomBarHeight = bottomBarHeight;
        this.cannonCenterX = cannonCenterX;

        // 加载按钮资源
        btnAddNormal   = ImageUtil.getImage("src/main/resources/images/cannon_plus.png");
        btnAddPressed  = ImageUtil.getImage("src/main/resources/images/cannon_plus_down.png");
        btnSubNormal   = ImageUtil.getImage("src/main/resources/images/cannon_minus.png");
        btnSubPressed  = ImageUtil.getImage("src/main/resources/images/cannon_minus_down.png");

        updateButtonRects();

        // 如果任意一张按钮图未加载成功，立即报错提示
        if (btnAddNormal == null || btnAddPressed == null ||
                btnSubNormal == null || btnSubPressed == null) {

            System.err.println("按钮图片加载失败，请检查路径：images/cannon_xxx.png");
        }

        // 加载 7 个等级
        for (int i = 1; i <= 7; i++) {
            addLevel("images/cannon" + i + ".png", 5, 0, 1, 4);
        }

        setLevel(0);

    }

    /** 加载炮台等级 */
    private void addLevel(String path, int frameCount, int idleIndex, int fireStart, int fireEnd) {

        BufferedImage sheet = ImageUtil.getImage(path);
        CannonLevel lvl = new CannonLevel();

        int w = sheet.getWidth();
        int h = sheet.getHeight() / frameCount;
        this.w = w;
        this.h = h;

        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = sheet.getSubimage(0, i * h, w, h);
        }

        lvl.idleFrames.add(frames[idleIndex]);

        for (int i = fireStart; i <= fireEnd; i++) {
            lvl.fireFrames.add(frames[i]);
        }

        lvl.width = w;
        lvl.height = h;
        lvl.contentBottomY = calcContentBottomY(frames[idleIndex]);

        levels.add(lvl);
    }

    /** 切换炮等级 */
    public void setLevel(int lvl) {
        if (lvl < 0 || lvl >= levels.size()) return;
        this.levelIndex = lvl;
        updatePosition();
        updateButtonRects();
    }

    /** 重新计算炮台位置 */
    private void updatePosition() {
        CannonLevel lvl = levels.get(levelIndex);
        this.w = lvl.width;
        this.h = lvl.height;

        x = cannonCenterX - w / 2;
        y = cachedPanelH - cachedBottomBarHeight - lvl.contentBottomY+50;
    }

    /** 炮台立即旋转对准鼠标 */
    public void rotateTo(int mouseX, int mouseY) {
        double cx = x + w / 2.0;
        double cy = y + h / 2.0;

        angle = Math.atan2(mouseY - cy, mouseX - cx);
    }

    /** 开火 */
    public void shoot() {
        shooting = true;
        frameFire = 0;
    }

    /** 更新动画 */
    public void update() {
        CannonLevel lvl = levels.get(levelIndex);

        if (shooting) {
            frameFire++;
            if (frameFire >= lvl.fireFrames.size()) {
                shooting = false;
                frameFire = 0;
            }
        }
    }

    /** 加减按钮矩形区域 */
    private void updateButtonRects() {
        int bw = btnAddNormal.getWidth();
        int bh = btnAddNormal.getHeight();

        rectSub = new Rectangle(x - bw - 5, y + h / 2 - bh / 2, bw, bh);
        rectAdd = new Rectangle(x + w + 5, y + h / 2 - bh / 2, bw, bh);
    }

    /** 按钮点击检测 */
    public boolean onButtonClick(int mx, int my) {

        if (rectAdd.contains(mx, my)) {
            addPressed = true;
            setLevel(levelIndex + 1);
            return true; // 表示点的是按钮
        }
        if (rectSub.contains(mx, my)) {
            subPressed = true;
            setLevel(levelIndex - 1);
            return true;
        }
        return false;
    }

    public void onMouseReleased() {
        addPressed = false;
        subPressed = false;
    }

    /** 绘制炮台和按钮 */
    public void draw(Graphics g) {
        CannonLevel lvl = levels.get(levelIndex);
        Graphics2D g2 = (Graphics2D) g;

        BufferedImage frame = shooting ? lvl.fireFrames.get(frameFire) : lvl.idleFrames.get(0);

        double cx = x + w / 2.0;
        double cy = y + h / 2.0;

        double visualAngle = angle + ANGLE_OFFSET;

        g2.rotate(visualAngle, cx, cy);
        g2.drawImage(frame, x, y, null);
        g2.rotate(-visualAngle, cx, cy);

        g.drawImage(subPressed ? btnSubPressed : btnSubNormal, rectSub.x, rectSub.y, null);
        g.drawImage(addPressed ? btnAddPressed : btnAddNormal, rectAdd.x, rectAdd.y, null);
    }

    /** 底部计算 */
    private int calcContentBottomY(BufferedImage img) {
        for (int y = img.getHeight() - 1; y >= 0; y--) {
            for (int x = 0; x < img.getWidth(); x++) {
                if (((img.getRGB(x, y) >> 24) & 0xff) != 0) return y;
            }
        }
        return img.getHeight() - 1;
    }
}
