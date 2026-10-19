package statusforge;

import necesse.engine.modLoader.ModSettings;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;

public class StatusForgeSettings extends ModSettings {

    /** When true the bar values are always shown instead of only on hover. */
    public boolean alwaysShowValues = false;

    @Override
    public void addSaveData(SaveData save) {
        save.addBoolean("alwaysShowValues", alwaysShowValues,
                "Show bar values at all times instead of only while hovering");
    }

    @Override
    public void applyLoadData(LoadData load) {
        alwaysShowValues = load.getBoolean("alwaysShowValues", false);
    }
}
