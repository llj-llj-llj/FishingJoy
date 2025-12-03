import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

public class ImageUtil {
    public static BufferedImage getImage(String path) {
        try {
            // 先尝试从 classpath 读取
            InputStream is = ImageUtil.class.getClassLoader().getResourceAsStream(path);
            if (is != null) {
                return ImageIO.read(is);
            }

            // 再尝试从文件系统读取
            File file = new File(path);
            if (file.exists()) {
                return ImageIO.read(file);
            }

            System.err.println("Image not found: " + path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
