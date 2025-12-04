public enum WebType {

    WEB1(1, 1, 2, "images/web1.png", "普通小网，命中率低"),
    WEB2(2, 2, 4, "images/web2.png", "稍大一些，适合小鱼群"),
    WEB3(3, 3, 7, "images/web3.png", "中型网，效果稳定"),
    WEB4(4, 4, 10, "images/web4.png", "强化渔网，能捕中中等鱼"),
    WEB5(5, 5, 15, "images/web5.png", "高级渔网，适合中型群鱼"),
    WEB6(6, 6, 22, "images/web6.png", "强力渔网，捕捉高价值鱼类"),
    WEB7(7, 10, 50, "images/web7.png", "顶级渔网，适用于鲨鱼等稀有鱼");

    public final int level;          // 网等级
    public final int attack;         // 攻击力（减少鱼HP）
    public final int cost;           // 消耗金币
    public final String spritePath;  // 网图片
    public final String description; // 说明

    WebType(int level, int attack, int cost, String path, String description) {
        this.level = level;
        this.attack = attack;
        this.cost = cost;
        this.spritePath = path;
        this.description = description;
    }

    /** 根据子弹/炮台等级取网类型 */
    public static WebType fromLevel(int level) {
        level = Math.max(1, Math.min(7, level));
        return values()[level - 1];
    }
}
