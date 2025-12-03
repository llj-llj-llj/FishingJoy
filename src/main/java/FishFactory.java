import java.util.Random;

public class FishFactory {

    private static final Random random = new Random();

    public static FishType randomNormalFish() {
        return FishType.values()[random.nextInt(10)];
    }

    public static FishType randomShark() {
        return random.nextBoolean() ? FishType.SHARK_BLUE : FishType.SHARK_GOLD;
    }

    public static Fish spawnNormal(int w, int h) {

        int r = random.nextInt(100);
        FishType type;

        if (r < 90) type = randomNormalFish();
        else if (r < 98) type = FishType.FISH9;
        else type = randomShark();

        return new Fish(w, h, type);
    }

    public static Fish spawnFeast(int w, int h) {

        int r = random.nextInt(100);
        FishType type;

        if (r < 40) type = FishType.SHARK_GOLD;
        else if (r < 80) type = FishType.SHARK_BLUE;
        else type = randomNormalFish();

        return new Fish(w, h, type);
    }
}
