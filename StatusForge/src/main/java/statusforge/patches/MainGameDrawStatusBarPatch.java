package statusforge.patches;

import statusforge.StatusForgeStatBar;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.state.MainGame;
import net.bytebuddy.asm.Advice;

@ModMethodPatch(target = MainGame.class, name = "drawStatusBar", arguments = {})
public class MainGameDrawStatusBarPatch {

    @Advice.OnMethodEnter(skipOn = Advice.OnDefaultValue.class)
    static boolean onEnter(@Advice.This MainGame mainGame) {
        StatusForgeStatBar.draw(mainGame);
        return false;
    }
}
