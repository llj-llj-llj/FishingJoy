import javax.swing.*;
import java.awt.*;

public class GameFrame {
    public static void main(String[] args){
        JFrame gameFrame = new JFrame();
        GamePanel gamePanel = new GamePanel();

        gameFrame.add(gamePanel);

        Insets insets = gameFrame.getInsets();
        gameFrame.setSize(
                gamePanel.getBgWidth()+insets.left+insets.right,
                gamePanel.getBgHeight()+insets.top+insets.bottom
        );

        gameFrame.setLocationRelativeTo(null);
        gameFrame.setResizable(true);
        gameFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        gameFrame.setVisible(true);
    }
}
