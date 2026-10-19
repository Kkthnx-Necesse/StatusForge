package statusforge;

import necesse.engine.modLoader.ModSettings;
import settingsforge.api.ModOptions;

/**
 * The only class that touches SettingsForge. Everything else reaches it through StatusForge, which loads this class
 * inside a try block, so the mod still works when SettingsForge is not installed.
 */
final class StatusForgeOptions {

    private static final String ALWAYS_SHOW_VALUES = "alwaysShowValues";

    private static ModOptions options;

    private StatusForgeOptions() {
    }

    /** Registers the checkbox on the Mod Settings page. The saved key matches StatusForgeSettings. */
    static ModSettings create() {
        options = ModOptions.create("kkthnx.statusforge").addBoolean(ALWAYS_SHOW_VALUES, false);
        return options;
    }

    static boolean getAlwaysShowValues() {
        return options.getBoolean(ALWAYS_SHOW_VALUES);
    }

    static void setAlwaysShowValues(boolean value) {
        options.setBoolean(ALWAYS_SHOW_VALUES, value);
    }
}
