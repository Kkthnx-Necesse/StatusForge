package statusforge;

import necesse.engine.modLoader.annotations.ModEntry;
import necesse.gfx.gameTexture.GameTexture;

@ModEntry
public class StatusForge {

    public static GameTexture manaIcon;

    public void init() {
        System.out.println("Thanks for using StatusForge!");
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

}