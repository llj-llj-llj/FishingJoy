public enum FishType {

    FISH1("金鳞小鱼", 1, 10, 1, "images/fish1.png", "最常见的小鱼，速度快", 8),
    FISH2("小丑鱼", 2, 20, 2, "images/fish2.png", "海底最可爱的鱼类", 8),
    FISH3("赤红大鱼", 3, 30, 3, "images/fish3.png", "体型大，价值高", 8),
    FISH4("蓝帆鱼", 2, 25, 2, "images/fish4.png", "蓝色闪光，非常灵活", 8),
    FISH5("河豚", 4, 40, 3, "images/fish5.png", "遇到攻击会鼓起身体", 8),
    FISH6("章鱼", 5, 50, 4, "images/fish6.png", "触手灵活，为海底一霸", 12),
    FISH7("水母", 3, 35, 2, "images/fish7.png", "具有麻痹效果（可扩展）", 10),
    FISH8("鮟鱇鱼", 5, 60, 5, "images/fish8.png", "深海凶狠鱼类，血量高", 12),
    FISH9("黄斑鱼", 4, 45, 3, "images/fish9.png", "罕见的高价值鱼", 12),
    FISH10("海龟", 7, 80, 5, "images/fish10.png", "超级肉盾，非常难捕获", 10),

    SHARK_BLUE("蓝锤头鲨", 20, 200, 20, "images/shark1.png", "强力深海掠食者", 12),
    SHARK_GOLD("金锤头鲨", 30, 300, 40, "images/shark2.png", "罕见黄金鲨鱼，价值极高", 12);

    public final String name;
    public final int hp;
    public final int score;
    public final int energy;
    public final String spritePath;
    public final String description;
    public final int totalFrames;

    FishType(String name, int hp, int score, int energy,
             String spritePath, String description, int totalFrames) {

        this.name = name;
        this.hp = hp;
        this.score = score;
        this.energy = energy;
        this.spritePath = spritePath;
        this.description = description;
        this.totalFrames = totalFrames;
    }
}
