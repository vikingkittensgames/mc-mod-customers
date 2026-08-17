package com.vikingkittens.mc.customers.client.customer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.client.common.PlayerProfileUtils;
import com.vikingkittens.mc.customers.customer.CustomerLeaderboardOpenPayload;
import com.vikingkittens.mc.customers.customer.CustomerLeaderboardScores;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerMode;

public class CustomerLeaderboardBlockScreen extends Screen {
    private static final int TEXTURE_SIZE = 256;
    private static final int BLACK_TEXT = 0x000000;
    private static final int OPAQUE_BLACK = 0xFF000000;
    private static final int CONTENT_HEIGHT = 172;
    private static final int CONTENT_WIDTH = 214;
    private static final int CONTENT_X = 24;
    private static final int CONTENT_Y = 56;
    private static final int ICON_SIZE = 16;
    private static final int PLAYER_HEAD_SIZE = 16;
    private static final int ROW_HEIGHT = 16;
    private static final int ROW_GAP = 1;
    private static final int LEVEL_SECTION_GAP = 4;
    private static final int STAR_COUNT = 5;
    private static final int TITLE_WIDTH = 185;
    private static final int TITLE_X = (TEXTURE_SIZE - TITLE_WIDTH) / 2;
    private static final int TITLE_Y = 40;
    private static final int ARROW_Y = 36;
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Customers.MODID,
            "textures/gui/leaderboard.png"
    );
    private static final ResourceLocation ARROW_LEFT = texture("arrow_left");
    private static final ResourceLocation ARROW_LEFT_PRESSED = texture("arrow_left_pressed");
    private static final ResourceLocation ARROW_RIGHT = texture("arrow_right");
    private static final ResourceLocation ARROW_RIGHT_PRESSED = texture("arrow_right_pressed");
    private static final ResourceLocation HALF_STAR = texture("halfstar_small");
    private static final ResourceLocation NO_STAR = texture("nostar_small");
    private static final ResourceLocation STAR = texture("star_small");

    private final List<ScoreGroup> groups;
    private int currentGroupIndex;
    private int scrollOffset;

    public CustomerLeaderboardBlockScreen(CustomerLeaderboardOpenPayload payload) {
        super(Component.translatable("screen.customers.customer_leaderboard"));
        groups = getScoreGroups(payload.scores());
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(Customers.MODID, "textures/gui/" + name + ".png");
    }

    private static ResourceLocation modeTexture(CustomerSpawnerMode mode) {
        return texture("mode_" + mode.getSerializedName());
    }

    @Override
    protected void init() {
        addRenderableWidget(new ArrowButton(
                (width - TEXTURE_SIZE) / 2 + TITLE_X - 16,
                (height - TEXTURE_SIZE) / 2 + ARROW_Y,
                ARROW_LEFT,
                ARROW_LEFT_PRESSED,
                () -> cycleGroup(-1)
        ));
        addRenderableWidget(new ArrowButton(
                (width - TEXTURE_SIZE) / 2 + TITLE_X + TITLE_WIDTH + 4,
                (height - TEXTURE_SIZE) / 2 + ARROW_Y,
                ARROW_RIGHT,
                ARROW_RIGHT_PRESSED,
                () -> cycleGroup(1)
        ));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        int left = (width - TEXTURE_SIZE) / 2;
        int top = (height - TEXTURE_SIZE) / 2;
        graphics.blit(TEXTURE, left, top, 0, 0, TEXTURE_SIZE, TEXTURE_SIZE);
        renderGroupTitle(graphics, left, top);
        renderScores(graphics, left, top);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int left = (width - TEXTURE_SIZE) / 2;
        int top = (height - TEXTURE_SIZE) / 2;
        if (mouseX >= left + CONTENT_X && mouseX < left + CONTENT_X + CONTENT_WIDTH
                && mouseY >= top + CONTENT_Y && mouseY < top + CONTENT_Y + CONTENT_HEIGHT) {
            scrollOffset = Mth.clamp(
                    scrollOffset - (int)Math.signum(scrollY) * (ROW_HEIGHT + ROW_GAP),
                    0,
                    getMaximumScrollOffset()
            );
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void cycleGroup(int direction) {
        if (groups.isEmpty()) {
            return;
        }
        currentGroupIndex = Math.floorMod(currentGroupIndex + direction, groups.size());
        scrollOffset = 0;
    }

    private void renderGroupTitle(GuiGraphics graphics, int left, int top) {
        if (groups.isEmpty()) {
            return;
        }
        ScoreGroup group = groups.get(currentGroupIndex);
        Component title = shouldShowSpawnerPosition(groups, group)
                ? Component.translatable(
                        "screen.customers.customer_leaderboard.group_with_position",
                        group.spawnerMode().getTitle(),
                        formatPosition(group.spawnerPosition())
                )
                : group.spawnerMode().getTitle();
        int titleX = left + TITLE_X + (TITLE_WIDTH - font.width(title)) / 2;
        graphics.blit(
                modeTexture(group.spawnerMode()),
                titleX - ICON_SIZE,
                top + TITLE_Y - (ICON_SIZE - (font.lineHeight - 1)) / 2,
                0,
                0,
                ICON_SIZE,
                ICON_SIZE,
                ICON_SIZE,
                ICON_SIZE
        );
        graphics.drawString(
                font,
                title,
                titleX,
                top + TITLE_Y,
                BLACK_TEXT,
                false
        );
    }

    private void renderScores(GuiGraphics graphics, int left, int top) {
        if (groups.isEmpty()) {
            return;
        }
        int contentLeft = left + CONTENT_X;
        int contentTop = top + CONTENT_Y;
        int y = contentTop - scrollOffset;
        graphics.enableScissor(contentLeft, contentTop, contentLeft + CONTENT_WIDTH, contentTop + CONTENT_HEIGHT);
        ScoreGroup group = groups.get(currentGroupIndex);
        for (int level : group.levels()) {
            graphics.drawString(
                    font,
                    Component.translatable("screen.customers.customer_leaderboard.level", level),
                    contentLeft,
                    y,
                    BLACK_TEXT,
                    false
            );
            graphics.fill(
                    contentLeft,
                    y + font.lineHeight,
                    contentLeft + CONTENT_WIDTH,
                    y + font.lineHeight + 2,
                    OPAQUE_BLACK
            );
            y += ROW_HEIGHT;
            for (ScoreEntry score : group.scoresForLevel(level)) {
                renderScore(graphics, score, contentLeft, y);
                y += ROW_HEIGHT + ROW_GAP;
            }
            y += LEVEL_SECTION_GAP;
        }
        graphics.disableScissor();
    }

    private void renderScore(GuiGraphics graphics, ScoreEntry score, int x, int y) {
        int centeredHeadY = getRowElementY(y, PLAYER_HEAD_SIZE);
        PlayerFaceRenderer.draw(
                graphics,
                PlayerProfileUtils.getPicture(score.playerId()),
                x,
                centeredHeadY,
                PLAYER_HEAD_SIZE,
                false,
                false
        );
        graphics.drawString(
                font,
                PlayerProfileUtils.getName(score.playerId()),
                x + PLAYER_HEAD_SIZE + 2,
                getRowElementY(y, font.lineHeight - 1),
                BLACK_TEXT,
                false
        );

        int starsX = x + CONTENT_WIDTH - STAR_COUNT * ICON_SIZE;
        String percentage = Math.round(score.score() * 100.0F) + "%";
        graphics.drawString(
                font,
                percentage,
                starsX - 4 - font.width(percentage),
                getRowElementY(y, font.lineHeight - 1),
                BLACK_TEXT,
                false
        );
        for (int index = 0; index < STAR_COUNT; index++) {
            graphics.blit(
                    getStarTexture(score.score(), index),
                    starsX + index * ICON_SIZE,
                    getRowElementY(y, ICON_SIZE),
                    0,
                    0,
                    ICON_SIZE,
                    ICON_SIZE,
                    ICON_SIZE,
                    ICON_SIZE
            );
        }
    }

    private int getMaximumScrollOffset() {
        return Math.max(0, getContentHeight() - CONTENT_HEIGHT);
    }

    static int getRowElementY(int rowY, int elementHeight) {
        return rowY + (ROW_HEIGHT - elementHeight) / 2;
    }

    private int getContentHeight() {
        if (groups.isEmpty()) {
            return 0;
        }
        int height = 0;
        for (int level : groups.get(currentGroupIndex).levels()) {
            height += getLevelSectionHeight(groups.get(currentGroupIndex).scoresForLevel(level).size());
        }
        return height;
    }

    static int getLevelSectionHeight(int playerScoreCount) {
        return ROW_HEIGHT + playerScoreCount * (ROW_HEIGHT + ROW_GAP) + LEVEL_SECTION_GAP;
    }

    private static ResourceLocation getStarTexture(float percentage, int index) {
        return switch (CustomerShiftFinishedScreen.getStarState(percentage, index)) {
            case FULL -> STAR;
            case HALF -> HALF_STAR;
            case EMPTY -> NO_STAR;
        };
    }

    static List<ScoreGroup> getScoreGroups(Map<CustomerLeaderboardScores.Key, Float> scores) {
        Map<ScoreGroupKey, Map<Integer, List<ScoreEntry>>> entriesByGroup = new HashMap<>();
        scores.forEach((key, score) -> entriesByGroup
                .computeIfAbsent(
                        new ScoreGroupKey(key.spawnerPosition(), key.spawnerMode()),
                        ignored -> new HashMap<>()
                )
                .computeIfAbsent(key.level(), ignored -> new ArrayList<>())
                .add(new ScoreEntry(key.playerId(), score)));
        return entriesByGroup.entrySet().stream()
                .map(entry -> {
                    entry.getValue().values().forEach(entries -> entries.sort(
                            Comparator.comparing(ScoreEntry::score)
                                    .reversed()
                                    .thenComparing(ScoreEntry::playerId)
                    ));
                    return new ScoreGroup(
                            entry.getKey().spawnerPosition(),
                            entry.getKey().spawnerMode(),
                            entry.getValue()
                    );
                })
                .sorted(
                        Comparator.comparing(ScoreGroup::spawnerMode)
                                .thenComparing(group -> group.spawnerPosition().getX())
                                .thenComparing(group -> group.spawnerPosition().getY())
                                .thenComparing(group -> group.spawnerPosition().getZ())
                )
                .toList();
    }

    static boolean shouldShowSpawnerPosition(List<ScoreGroup> groups, ScoreGroup group) {
        return groups.stream()
                .filter(otherGroup -> otherGroup.spawnerMode() == group.spawnerMode())
                .count() > 1;
    }

    private static String formatPosition(BlockPos position) {
        return position.getX() + ", " + position.getY() + ", " + position.getZ();
    }

    record ScoreEntry(UUID playerId, float score) {}

    record ScoreGroup(
            BlockPos spawnerPosition,
            CustomerSpawnerMode spawnerMode,
            Map<Integer, List<ScoreEntry>> scoresByLevel
    ) {
        List<Integer> levels() {
            return scoresByLevel.keySet().stream().sorted().toList();
        }

        List<ScoreEntry> scoresForLevel(int level) {
            return scoresByLevel.getOrDefault(level, List.of());
        }
    }

    private record ScoreGroupKey(BlockPos spawnerPosition, CustomerSpawnerMode spawnerMode) {}

    private static final class ArrowButton extends AbstractButton {
        private final ResourceLocation texture;
        private final ResourceLocation pressedTexture;
        private final Runnable action;

        private ArrowButton(
                int x,
                int y,
                ResourceLocation texture,
                ResourceLocation pressedTexture,
                Runnable action
        ) {
            super(x, y, 12, 12, Component.empty());
            this.texture = texture;
            this.pressedTexture = pressedTexture;
            this.action = action;
        }

        @Override
        public void onPress() {
            action.run();
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            ResourceLocation buttonTexture = isHoveredOrFocused() ? pressedTexture : texture;
            graphics.blit(buttonTexture, getX(), getY(), 0, 0, 12, 12, 12, 12);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narration) {}
    }
}
