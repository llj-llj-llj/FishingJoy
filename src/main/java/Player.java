public class Player {

    public int score = 1000;         // 积分
    public int energy = 100;      // 初始能量
    public int bulletDamage = 1;  // 渔网等级

    private static final int MAX_ENERGY = 100;

    /** 消耗能量 */
    public boolean useEnergy(int amount) {
        if (energy < amount) return false;
        energy -= amount;
        return true;
    }

    /** 增加奖励 */
    public void addReward(int scoreGain, int energyGain) {
        score += scoreGain;
        energy += energyGain;
        if (energy > MAX_ENERGY) energy = MAX_ENERGY;
    }

    /** 升级渔网 */
    public boolean upgrade() {
        int cost = (bulletDamage + 1) * 50;
        if (score >= cost) {
            score -= cost;
            bulletDamage++;
            return true;
        }
        return false;
    }

    /** 返回能量百分比（0~1）用于绘制能量条 */
    public float getEnergyPercent() {
        return Math.min(1f, energy / (float) MAX_ENERGY);
    }
}
