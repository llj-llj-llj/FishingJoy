import javax.swing.*;
import java.awt.*;

public class GameFrame {
    public static void main(String[] args){
        GamePanel gamePanel = new GamePanel();
        JFrame gameFrame = new JFrame("Fishing Game");

        gameFrame.add(gamePanel);

        // 窗口大小完全等于背景图大小
        Insets insets = gameFrame.getInsets();
        gameFrame.setSize(
                gamePanel.getBgWidth() + insets.left + insets.right,
                gamePanel.getBgHeight() + insets.top + insets.bottom
        );

        gameFrame.setLocationRelativeTo(null);
        gameFrame.setResizable(false);            // ★ 固定窗口大小
        gameFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        gameFrame.setVisible(true);
    }
}
