package statusforge.patches;

import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.state.MainGame;
import necesse.engine.window.GameWindow;
import net.bytebuddy.asm.Advice;
import statusforge.StatusForge;

/** Polls the StatusForge toggle key once per frame, the same place vanilla polls its own. */
@ModMethodPatch(target = MainGame.class, name = "frameTick", arguments = { TickManager.class, GameWindow.class })
public class MainGameFrameTickPatch {

    @Advice.OnMethodEnter
    static void onEnter() {
        StatusForge.pollToggle();
    }
}
