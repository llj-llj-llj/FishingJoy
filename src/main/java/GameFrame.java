import javax.swing.*;

public class GameFrame {

    public static void main(String[] args) {

        // 创建窗口
        JFrame frame = new JFrame("Fishing Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        // 创建游戏面板
        GamePanel gamePanel = new GamePanel();
        frame.add(gamePanel);

        // 让窗口大小自动匹配游戏背景图
        frame.pack();

        // 根据背景图实际宽高设置窗口大小
        frame.setSize(
                gamePanel.getBgWidth(),
                gamePanel.getBgHeight()
        );

        frame.setLocationRelativeTo(null);  // 居中
        frame.setVisible(true);
    }
}
