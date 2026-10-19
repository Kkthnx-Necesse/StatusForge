package statusforge;

import java.awt.Color;
import java.awt.Rectangle;

import necesse.engine.Settings;
import necesse.engine.localization.Localization;
import necesse.engine.state.MainGame;
import necesse.engine.util.GameMath;
import necesse.engine.window.GameWindow;
import necesse.engine.window.WindowManager;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.Renderer;
import necesse.gfx.drawOptions.DrawOptionsBox;
import necesse.gfx.drawOptions.DrawOptionsList;
import necesse.gfx.drawOptions.StringDrawOptions;
import necesse.gfx.gameFont.FontManager;
import necesse.gfx.gameFont.FontOptions;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.gameTooltips.GameTooltipManager;
import necesse.gfx.gameTooltips.StringTooltips;
import necesse.gfx.gameTooltips.TooltipLocation;

public class StatusForgeStatBar {

    // Define colors as static constants to avoid allocation every frame
    private static final Color HEALTH_COLOR = new Color(220, 50, 50);
    private static final Color RESILIENCE_COLOR = new Color(255, 233, 73);
    private static final Color HUNGER_COLOR = new Color(150, 75, 0);
    private static final Color MANA_COLOR = new Color(51, 133, 224);

    // Layout constants shared by drawing and by the bounds reported to the vanilla HUD
    private static final int TOP_Y = 32;
    private static final int BAR_WIDTH = 200;
    private static final int BAR_HEIGHT = 12;
    private static final int ICON_SIZE = 16;
    private static final int ICON_SPACING = 8;
    private static final int ICON_OFFSET = ICON_SIZE + ICON_SPACING;
    private static final int HOVER_PADDING = 6;

    // Define font options as static constant
    private static final FontOptions TEXT_FONT_OPTIONS = new FontOptions(12).outline().color(Color.WHITE);

    public static void draw(MainGame mainGame) {
        if (mainGame.getClient() == null) {
            return;
        }

        PlayerMob player = mainGame.getClient().getPlayer();
        if (player == null) {
            return;
        }

        GameWindow window = WindowManager.getWindow();
        int startX = window.getHudWidth() / 2 - (BAR_WIDTH + ICON_OFFSET) / 2;
        int mouseX = window.mousePos().hudX;
        int mouseY = window.mousePos().hudY;
        int step = getStep();
        int y = TOP_Y;

        DrawOptionsList drawOptions = new DrawOptionsList();

        // Text and tooltip strings are only built for the bar the mouse is over,
        // so an idle HUD does no string work per frame.

        // --- Health Bar ---
        int health = player.getHealth();
        int maxHealth = player.getMaxHealth();
        boolean hovered = isHovered(startX, y, mouseX, mouseY);
        String text = null;
        if (hovered) {
            text = health + " / " + maxHealth;
            addTooltip("healthbartip", health + "/" + maxHealth);
        }
        drawBar(startX, y, getPercent(health, maxHealth), HEALTH_COLOR, Settings.UI.heart_fill, text, drawOptions);
        y += step;

        // --- Resilience Bar ---
        if (hasResilienceBar(player)) {
            int resilience = (int) player.getResilience();
            int maxResilience = player.getMaxResilience();
            hovered = isHovered(startX, y, mouseX, mouseY);
            text = null;
            if (hovered) {
                text = resilience + " / " + maxResilience;
                addTooltip("resiliencebartip", resilience + "/" + maxResilience);
            }
            drawBar(startX, y, getPercent(player.getResilience(), maxResilience), RESILIENCE_COLOR,
                    Settings.UI.resilience_fill, text, drawOptions);
            y += step;
        }

        // --- Food/Hunger Bar ---
        if (hasHungerBar(player)) {
            float hunger = GameMath.limit(player.hungerLevel, 0.0F, 1.0F);
            hovered = isHovered(startX, y, mouseX, mouseY);
            text = null;
            if (hovered) {
                text = (int) Math.ceil(hunger * 100.0F) + "%";
                addTooltip("hungerbartip", text);
            }
            drawBar(startX, y, hunger, HUNGER_COLOR, Settings.UI.food_fill, text, drawOptions);
            y += step;
        }

        // --- Mana Bar ---
        if (hasManaBar(player)) {
            float mana = player.getMana();
            int maxMana = player.getMaxMana();
            hovered = isHovered(startX, y, mouseX, mouseY);
            text = null;
            if (hovered) {
                int shownMana = (int) Math.ceil(mana);
                text = shownMana + " / " + maxMana;
                addTooltip("manabartip", shownMana + "/" + maxMana);
            }
            drawBar(startX, y, getPercent(mana, maxMana), MANA_COLOR, StatusForge.manaIcon, text, drawOptions);
        }

        drawOptions.draw();
    }

    private static boolean hasResilienceBar(PlayerMob player) {
        return player.getMaxResilience() > 0;
    }

    private static boolean hasHungerBar(PlayerMob player) {
        return player.getLevel() != null && player.getLevel().getWorldSettings() != null
                && player.getLevel().getWorldSettings().playerHunger();
    }

    private static boolean hasManaBar(PlayerMob player) {
        return player.getLevel() != null && player.usesMana() && player.isManaBarVisible();
    }

    private static int getStep() {
        return BAR_HEIGHT + Settings.UI.formSpacing + 4;
    }

    /** Fill fraction clamped to 0..1, safe against a zero or negative max. */
    private static float getPercent(float current, float max) {
        if (max <= 0) {
            return 0.0F;
        }
        return GameMath.limit(current / max, 0.0F, 1.0F);
    }

    private static void addTooltip(String key, String value) {
        GameTooltipManager.addTooltip(
                new StringTooltips(Localization.translate("ui", key, "value", value)),
                TooltipLocation.PLAYER);
    }

    /**
     * The area our bars occupy. Returned from the patched getStatusDrawBox so the
     * vanilla HUD (buff icons, click-through hitbox) lays out around our bars
     * instead of the hidden vanilla hearts.
     */
    public static DrawOptionsBox getStatusBox(PlayerMob player) {
        if (player == null) {
            return null;
        }
        int bars = 1;
        if (hasResilienceBar(player)) {
            bars++;
        }
        if (hasHungerBar(player)) {
            bars++;
        }
        if (hasManaBar(player)) {
            bars++;
        }
        int width = BAR_WIDTH + ICON_OFFSET;
        int x = WindowManager.getWindow().getHudWidth() / 2 - width / 2;
        return new StatusBox(new Rectangle(x, TOP_Y, width, bars * getStep()));
    }

    private static final class StatusBox implements DrawOptionsBox {
        private final Rectangle bounds;

        StatusBox(Rectangle bounds) {
            this.bounds = bounds;
        }

        @Override
        public Rectangle getBoundingBox() {
            return bounds;
        }

        @Override
        public void draw() {
            // Bars are drawn by MainGameDrawStatusBarPatch
        }
    }

    // Plain bounds check, avoids allocating a Rectangle per bar per frame
    private static boolean isHovered(int x, int y, int mouseX, int mouseY) {
        int width = BAR_WIDTH + ICON_OFFSET;
        int height = BAR_HEIGHT + HOVER_PADDING * 2;
        return mouseX >= x && mouseX < x + width && mouseY >= y - HOVER_PADDING && mouseY < y - HOVER_PADDING + height;
    }

    private static void drawBar(int x, int y, float percent, Color color, GameTexture icon, String text,
            DrawOptionsList drawOptions) {
        final int width = BAR_WIDTH;
        final int height = BAR_HEIGHT;

        // Icon sits on the left, the bar always starts after the icon slot
        if (icon != null) {
            drawOptions.add(icon.initDraw().size(ICON_SIZE, ICON_SIZE).pos(x, y + height / 2 - ICON_SIZE / 2));
        }
        final int barX = x + ICON_OFFSET;

        // --- Themed Border Logic ---

        // 1. Draw Themed Background (Form Center)
        // Dynamic offset calculation: Primal is 8, we want 6. Legacy is 5, we want 3.
        // Ghost is 6, but needs 0 offset (5) to fill properly.
        final int edgeOffset;
        if (Settings.UI.texturesPath.equals("ghost")) {
            edgeOffset = Settings.UI.form.edgeMargin;
        } else {
            edgeOffset = Settings.UI.form.edgeMargin - 2;
        }
        drawOptions.add(() -> Settings.UI.form.getCenterDrawOptions(barX - edgeOffset, y - edgeOffset,
                width + (edgeOffset * 2), height + (edgeOffset * 2)).draw());

        // 2. Draw Progress Bar Fill
        int filled = (int) (percent * (width - 2));
        if (filled > 0) {
            drawOptions.add(Renderer.initQuadDraw(filled, height - 2)
                    .color(color)
                    .pos(barX + 1, y + 1));
        }

        // 3. Draw Themed Edge (The Border Frame)
        drawOptions.add(() -> Settings.UI.form.getEdgeDrawOptions(barX, y, width, height).draw());

        // 4. Draw Text (Only if Hovered, caller passes null otherwise)
        if (text != null) {
            int textWidth = FontManager.bit.getWidthCeil(text, TEXT_FONT_OPTIONS);
            int textHeight = (int) FontManager.bit.getHeight(text, TEXT_FONT_OPTIONS);
            int textX = barX + width / 2 - textWidth / 2;
            int textY = y + height / 2 - textHeight / 2;
            drawOptions.add(new StringDrawOptions(TEXT_FONT_OPTIONS, text).pos(textX, textY));
        }
    }

}
