package statusforge.patches;

import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.state.MainGame;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.drawOptions.DrawOptionsBox;
import net.bytebuddy.asm.Advice;
import statusforge.StatusForgeStatBar;

/**
 * The vanilla HUD positions buff icons and the click-through hitbox from the
 * vanilla status box. Report our bar area instead so nothing overlaps.
 */
@ModMethodPatch(target = MainGame.class, name = "getStatusDrawBox", arguments = { PlayerMob.class })
public class MainGameStatusDrawBoxPatch {

    @Advice.OnMethodExit
    static void onExit(@Advice.Argument(0) PlayerMob player,
            @Advice.Return(readOnly = false) DrawOptionsBox result) {
        DrawOptionsBox box = StatusForgeStatBar.getStatusBox(player);
        if (box != null) {
            result = box;
        }
    }
}
