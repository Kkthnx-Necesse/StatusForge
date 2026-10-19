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
    private static final int ICON_OFFSET = 24;

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
        int hudWidth = window.getHudWidth();
        int centerX = hudWidth / 2;
        int currentY = TOP_Y;
        int barWidth = BAR_WIDTH;
        int barHeight = BAR_HEIGHT;
        int iconOffset = ICON_OFFSET;
        int startX = centerX - (barWidth + iconOffset) / 2;

        int mouseX = window.mousePos().hudX;
        int mouseY = window.mousePos().hudY;

        DrawOptionsList drawOptions = new DrawOptionsList();

        // --- Health Bar ---
        float healthPerc = (float) player.getHealth() / player.getMaxHealth();

        // Optimize hover check: Use math instead of creating Rectangle logic
        boolean healthHovered = isHovered(startX, currentY - 6, barWidth + iconOffset, barHeight + 12, mouseX, mouseY);

        drawBar(
                startX, currentY, barWidth, barHeight,
                healthPerc,
                HEALTH_COLOR,
                Settings.UI.heart_fill,
                player.getHealth() + " / " + player.getMaxHealth(),
                healthHovered,
                drawOptions);

        if (healthHovered) {
            GameTooltipManager.addTooltip(
                    new StringTooltips(Localization.translate("ui", "healthbartip", "value",
                            player.getHealth() + "/" + player.getMaxHealth())),
                    TooltipLocation.PLAYER);
        }

        currentY += barHeight + Settings.UI.formSpacing + 4;

        // --- Resilience Bar ---
        if (hasResilienceBar(player)) {
            float resiliencePerc = player.getResilience() / player.getMaxResilience();
            boolean resilienceHovered = isHovered(startX, currentY - 6, barWidth + iconOffset, barHeight + 12, mouseX,
                    mouseY);

            drawBar(
                    startX, currentY, barWidth, barHeight,
                    resiliencePerc,
                    RESILIENCE_COLOR,
                    Settings.UI.resilience_fill,
                    (int) player.getResilience() + " / " + player.getMaxResilience(),
                    resilienceHovered,
                    drawOptions);

            if (resilienceHovered) {
                GameTooltipManager.addTooltip(
                        new StringTooltips(Localization.translate("ui", "resiliencebartip", "value",
                                (int) player.getResilience() + "/" + player.getMaxResilience())),
                        TooltipLocation.PLAYER);
            }

            currentY += barHeight + Settings.UI.formSpacing + 4;
        }

        // --- Food/Hunger Bar ---
        if (hasHungerBar(player)) {
            float hungerLevel = Math.min(1.0F, player.hungerLevel);
            boolean foodHovered = isHovered(startX, currentY - 6, barWidth + iconOffset, barHeight + 12, mouseX,
                    mouseY);

            drawBar(
                    startX, currentY, barWidth, barHeight,
                    hungerLevel,
                    HUNGER_COLOR,
                    Settings.UI.food_fill,
                    (int) Math.ceil(hungerLevel * 100.0F) + "%",
                    foodHovered,
                    drawOptions);

            if (foodHovered) {
                GameTooltipManager.addTooltip(
                        new StringTooltips(Localization.translate("ui", "hungerbartip", "value",
                                (int) Math.ceil(hungerLevel * 100.0F) + "%")),
                        TooltipLocation.PLAYER);
            }

            currentY += barHeight + Settings.UI.formSpacing + 4;
        }

        // --- Mana Bar ---
        if (hasManaBar(player)) {
            final float mana = player.getMana();
            final int maxMana = player.getMaxMana();
            float manaPerc = GameMath.limit(mana / maxMana, 0.0F, 1.0F);
            boolean manaHovered = isHovered(startX, currentY - 6, barWidth + iconOffset, barHeight + 12, mouseX,
                    mouseY);

            drawBar(
                    startX, currentY, barWidth, barHeight,
                    manaPerc,
                    MANA_COLOR,
                    StatusForge.manaIcon,
                    (int) Math.ceil(mana) + " / " + maxMana,
                    manaHovered,
                    drawOptions);

            if (manaHovered) {
                GameTooltipManager.addTooltip(
                        new StringTooltips(Localization.translate("ui", "manabartip", "value",
                                (int) Math.ceil(mana) + "/" + maxMana)),
                        TooltipLocation.PLAYER);
            }
            currentY += barHeight + Settings.UI.formSpacing + 4;
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
        int hudWidth = WindowManager.getWindow().getHudWidth();
        int width = BAR_WIDTH + ICON_OFFSET;
        int step = BAR_HEIGHT + Settings.UI.formSpacing + 4;
        return new StatusBox(new Rectangle(hudWidth / 2 - width / 2, TOP_Y, width, bars * step));
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

    // Helper for hover check
    private static boolean isHovered(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static void drawBar(int x, int y, int width, int height, float percent, Color color, GameTexture icon,
            String text, boolean isHovered, DrawOptionsList drawOptions) {
        int iconSize = 16;
        int iconSpacing = 8;
        int barX = x;

        // Draw Icon
        if (icon != null) {
            drawOptions.add(icon.initDraw().size(iconSize, iconSize).pos(x, y + height / 2 - iconSize / 2));
            barX += iconSize + iconSpacing;
        } else {
            barX += iconSize + iconSpacing;
        }

        int finalBarX = barX;

        // --- Themed Border Logic ---

        // 1. Draw Themed Background (Form Center)
        // Dynamic offset calculation: Primal is 8, we want 6. Legacy is 5, we want 3.
        // Ghost is 6, but needs 0 offset (5) to fill properly.
        int edgeOffset;
        if (Settings.UI.texturesPath.equals("ghost")) {
            edgeOffset = Settings.UI.form.edgeMargin;
        } else {
            edgeOffset = Settings.UI.form.edgeMargin - 2;
        }
        drawOptions
                .add(() -> Settings.UI.form.getCenterDrawOptions(finalBarX - edgeOffset, y - edgeOffset,
                        width + (edgeOffset * 2), height + (edgeOffset * 2)).draw());

        // 3. Draw Progress Bar Fill
        int fillWidthMax = width - 2;
        int filled = (int) (percent * fillWidthMax);

        if (filled > 0) {
            drawOptions.add(Renderer.initQuadDraw(filled, height - 2)
                    .color(color)
                    .pos(finalBarX + 1, y + 1));
        }

        // 4. Draw Themed Edge (The Border Frame)
        drawOptions.add(() -> Settings.UI.form.getEdgeDrawOptions(finalBarX, y, width, height).draw());

        // Draw Text (Only if Hovered)
        if (text != null && isHovered) {
            int textWidth = FontManager.bit.getWidthCeil(text, TEXT_FONT_OPTIONS);
            int textHeight = (int) FontManager.bit.getHeight(text, TEXT_FONT_OPTIONS);
            int textX = finalBarX + width / 2 - textWidth / 2;
            int textY = y + height / 2 - textHeight / 2;
            drawOptions.add(new StringDrawOptions(TEXT_FONT_OPTIONS, text).pos(textX, textY));
        }
    }

}
