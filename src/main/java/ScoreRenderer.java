import java.awt.*;
import java.awt.image.BufferedImage;

public class ScoreRenderer {

    private BufferedImage numberSheet;
    private BufferedImage[] numbers;
    private int digitW;
    private int digitH;

    public ScoreRenderer(String sheetPath) {

        numberSheet = ImageUtil.getImage(sheetPath);
        if (numberSheet == null) {
            System.err.println(" 数字图片加载失败: " + sheetPath);
            return;
        }

        int totalDigits = 10; // 0~9 共 10 个数字

        digitW = numberSheet.getWidth();           // 数字宽度 = 图片宽度
        digitH = numberSheet.getHeight() / totalDigits; // 图片竖切 10 份

        numbers = new BufferedImage[10];

        for (int digit = 0; digit < 10; digit++) {
            int srcIndex = 9 - digit;

            numbers[digit] = numberSheet.getSubimage(
                    0,
                    srcIndex * digitH,
                    digitW,
                    digitH
            );
        }
    }


        /** 接受 string，可补 0 */
    public void drawScore(Graphics2D g2, String scoreStr, int x, int y) {
        if (numbers == null) return;

        for (int i = 0; i < scoreStr.length(); i++) {
            int digit = scoreStr.charAt(i) - '0';
            g2.drawImage(numbers[digit], x, y, null);
            x += digitW*1.17;
        }
    }
}
