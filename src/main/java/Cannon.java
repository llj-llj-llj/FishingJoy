import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Cannon {
    int level = 1;
    private int totalFrames = 4;
    private int currentFrame = 0;
    private int x, y, w, h;
    private List<BufferedImage> cannonAnim = new ArrayList<>();


    public Cannon(int panelW, int panelH) {
        BufferedImage cannonImg = ImageUtil.getImage("src/main/resources/images/cannon" + level + ".png");

        w = cannonImg.getWidth();
        h = cannonImg.getHeight() / totalFrames;  // 单帧高度

        x = panelW / 2;
        y = panelH - cannonImg.getHeight();

        for (int i = 0; i < totalFrames; i++) cannonAnim.add(cannonImg.getSubimage(0, i * h, w, h));


    }

    public void update(int panelW, int panelH) {
        // 更新帧动画
        currentFrame++;

    }

    public void draw(Graphics g) {
        BufferedImage frame = cannonAnim.get(currentFrame);
        g.drawImage(frame, x, y, null);
    }
}