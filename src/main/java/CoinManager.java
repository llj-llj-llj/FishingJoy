import java.awt.*;
import java.util.ArrayList;

public class CoinManager {

    private ArrayList<CoinAnimation> coins = new ArrayList<>();

    public void add(CoinAnimation c) {
        coins.add(c);
    }

    public void update() {
        coins.removeIf(CoinAnimation::isFinished);
        for (CoinAnimation c : coins) c.update();
    }

    public void draw(Graphics2D g2) {
        for (CoinAnimation c : coins) c.draw(g2);
    }
}

