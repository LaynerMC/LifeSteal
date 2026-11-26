package dev.laynermc.lifesteal.storage;

/**
 * Persistent per-player state. Hearts are stored as max-health HP
 * (1 heart = 2 HP), so the default 10 starting hearts == 20.0.
 */
public class PlayerData {

    public String name = "";
    /** Current max health in HP (hearts * 2). */
    public double hearts = 20.0;
    /** Personal heart cap in HP. -1 = use the global cap. */
    public double maxHearts = -1.0;
    public int kills = 0;
    public int deaths = 0;
    public int eliminations = 0;
    public int revives = 0;
    public boolean eliminated = false;
    /** Epoch seconds when the elimination expires. 0 = permanent. */
    public long unbanTime = 0;

    public int heartsInt() {
        return (int) Math.round(hearts / 2.0);
    }

    public int maxHeartsInt() {
        return (int) Math.round(maxHearts / 2.0);
    }
}
