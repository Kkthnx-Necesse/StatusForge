package statusforge;

import org.lwjgl.glfw.GLFW;

import necesse.engine.Settings;
import necesse.engine.input.Control;
import necesse.engine.localization.message.StaticMessage;
import necesse.engine.modLoader.ModSettings;
import necesse.engine.modLoader.annotations.ModEntry;
import necesse.gfx.gameTexture.GameTexture;

@ModEntry
public class StatusForge {

    public static GameTexture manaIcon;
    public static final StatusForgeSettings settings = new StatusForgeSettings();
    public static Control toggleValuesControl;

    /** True once the optional SettingsForge library has taken over the always show values option. */
    private static boolean usingLibrary;

    public void init() {
        // Shows up under this mod in the Controls menu, so players can rebind it
        toggleValuesControl = Control.addModControl(new Control(GLFW.GLFW_KEY_F8, "statusforge.togglevalues",
                new StaticMessage("Toggle bar values")));
        System.out.println("Thanks for using StatusForge!");
    }

    public ModSettings initSettings() {
        try {
            // With SettingsForge installed the option lives on its Mod Settings page. The saved key is the same
            ModSettings fromLibrary = StatusForgeOptions.create();
            usingLibrary = true;
            return fromLibrary;
        } catch (LinkageError notInstalled) {
            return settings;
        }
    }

    public void initResources() {
        // Sometimes your textures will have a black or other outline unintended under
        // rotation or scaling
        // This is caused by alpha blending between transparent pixels and the edge
        // To fix this, run the preAntialiasTextures gradle task
        // It will process your textures and save them again with a fixed alpha edge
        // color

        manaIcon = GameTexture.fromFile("misc/mana");
    }

    public static boolean alwaysShowValues() {
        return usingLibrary ? StatusForgeOptions.getAlwaysShowValues() : settings.alwaysShowValues;
    }

    private static void setAlwaysShowValues(boolean value) {
        if (usingLibrary) {
            StatusForgeOptions.setAlwaysShowValues(value);
        } else {
            settings.alwaysShowValues = value;
        }
    }

    /** Called once per frame. Flips the always show values option when the key is pressed. */
    public static void pollToggle() {
        if (toggleValuesControl != null && toggleValuesControl.isPressed()) {
            setAlwaysShowValues(!alwaysShowValues());
            Settings.saveClientSettings();
        }
    }

}
