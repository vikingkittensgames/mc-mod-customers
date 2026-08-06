package com.vikingkittens.mc.customers.client.customer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.client.compatability.GuiGraphicsCUtils;
import com.vikingkittens.mc.customers.client.compatability.TextureC;
import com.vikingkittens.mc.customers.compatability.ProfileCUtils;
import com.vikingkittens.mc.customers.customer.CustomerShiftFinishedPayload;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerMode;

public class CustomerShiftFinishedScreen extends Screen {
    private static final int IMAGE_WIDTH = 256;
    private static final int IMAGE_HEIGHT = 256;
    private static final int TEXT_COLOR = 0xFF3F3028;
    private static final int STAR_SIZE = 32;
    private static final int STAR_GAP = 8;
    private static final long STAR_ANIMATION_MILLIS = 500L;
    private static final int PLAYER_HEAD_SIZE = 12;
    private static final int PLAYER_CARD_MARGIN = 16;
    private static final int PLAYER_CARD_GAP = 4;
    private static final int PLAYER_CARD_HEIGHT = 38;
    private static final int SCORE_ICON_TEXTURE_SIZE = 32;
    private static final int TEST_DUPLICATE_PLAYERS = 1;

    private static final SoundEvent BLING_SOUND = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(Customers.MODID, "bling")
    );
    private static final SoundEvent BONK_SOUND = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(Customers.MODID, "bonk")
    );

    private static final TextureC RECEIPT_TEXTURE = texture("reciept.png");
    private static final TextureC STAR_TEXTURE = texture("star.png");
    private static final TextureC SPOON_TEXTURE = texture("spoon.png");
    private static final TextureC HALF_STAR_TEXTURE = texture("halfstar.png");
    private static final TextureC NO_STAR_TEXTURE = texture("nostar.png");
    private static final TextureC BREAKFAST_SHIFT_TEXTURE = texture("shift_breakfast.png");
    private static final TextureC DAY_SHIFT_TEXTURE = texture("shift_day.png");
    private static final TextureC DINNER_SHIFT_TEXTURE = texture("shift_dinner.png");
    private static final TextureC LUNCH_SHIFT_TEXTURE = texture("shift_lunch.png");
    private static final TextureC NIGHT_SHIFT_TEXTURE = texture("shift_night.png");

    private final CustomerShiftFinishedPayload payload;
    private int leftPos;
    private int topPos;
    private long animationStartMillis;
    private final boolean[] starSoundsPlayed = new boolean[5];

    public CustomerShiftFinishedScreen(CustomerShiftFinishedPayload payload) {
        super(Component.translatable("screen.customers.shift_finished"));
        this.payload = payload;
    }

    private static TextureC texture(String fileName) {
        return new TextureC(Customers.MODID, "textures/gui/" + fileName);
    }

    @Override
    protected void init() {
        leftPos = (width - IMAGE_WIDTH) / 2;
        topPos = (height - IMAGE_HEIGHT) / 2;
        animationStartMillis = Util.getMillis();
        Arrays.fill(starSoundsPlayed, false);
        addRenderableWidget(Button.builder(
                        Component.translatable("screen.customers.shift_finished.close"),
                        button -> onClose()
                )
                .bounds(leftPos + (IMAGE_WIDTH - 100) / 2, topPos + IMAGE_HEIGHT - 24, 100, 20)
                .build());
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        GuiGraphicsCUtils.blit(
                graphics,
                RECEIPT_TEXTURE,
                leftPos,
                topPos,
                0.0F,
                0.0F,
                IMAGE_WIDTH,
                IMAGE_HEIGHT,
                256,
                256
        );
        renderShiftSummary(graphics);
        renderStars(graphics);
        renderCustomerTotals(graphics);
        renderPlayerScores(graphics);
        graphics.flush();
        RenderSystem.disableBlend();

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
    private void renderShiftSummary(GuiGraphics graphics) {
        Component summary = Component.empty()
                .append(payload.spawnerMode().getTitle())
                .append(" - ")
                .append(Math.round(payload.percentComplete() * 100.0F) + "%");
        graphics.drawString(font, summary, leftPos + 12, topPos + 12, TEXT_COLOR, false);
        GuiGraphicsCUtils.blit(graphics, getShiftTexture(payload.spawnerMode()), leftPos + 225, topPos + 12,
                0.0F, 0.0F, 16, 16, 16, 16);
    }

    private void renderStars(GuiGraphics graphics) {
        int totalWidth = STAR_SIZE * 5 + STAR_GAP * 4;
        int x = leftPos + (IMAGE_WIDTH - totalWidth) / 2;
        int y = topPos + 32;
        long elapsedMillis = Util.getMillis() - animationStartMillis;
        playStarSounds(elapsedMillis);

        for (int index = 0; index < 5; index++) {
            int starX = x + index * (STAR_SIZE + STAR_GAP);
            float centerX = starX + STAR_SIZE / 2.0F;
            float centerY = y + STAR_SIZE / 2.0F;
            float scale = getStarScale(elapsedMillis, index);

            GuiGraphicsCUtils.pushTransform(graphics);
            GuiGraphicsCUtils.translate(graphics, centerX, centerY);
            GuiGraphicsCUtils.scale(graphics, scale, scale);
            GuiGraphicsCUtils.translate(graphics, -centerX, -centerY);
            GuiGraphicsCUtils.blit(
                    graphics,
                    getStarTexture(getStarState(payload.percentComplete(), index)),
                    starX,
                    y,
                    0.0F,
                    0.0F,
                    STAR_SIZE,
                    STAR_SIZE,
                    STAR_SIZE,
                    STAR_SIZE
            );
            GuiGraphicsCUtils.popTransform(graphics);
        }
    }

    static float getStarScale(long elapsedMillis, int starIndex) {
        long starElapsedMillis = elapsedMillis - starIndex * STAR_ANIMATION_MILLIS;
        float progress = Mth.clamp(
                (float) starElapsedMillis / STAR_ANIMATION_MILLIS,
                0.0F,
                1.0F
        );
        return easeOutElastic(progress);
    }

    static float easeOutElastic(float progress) {
        if (progress == 0.0F || progress == 1.0F) {
            return progress;
        }

        double period = 2.0D * Math.PI / 3.0D;
        return (float) (
                Math.pow(2.0D, -10.0D * progress)
                        * Math.sin((progress * 10.0D - 0.75D) * period)
                        + 1.0D
        );
    }

    private void playStarSounds(long elapsedMillis) {
        for (int index = 0; index < 5; index++) {
            StarState state = getStarState(payload.percentComplete(), index);
            if (shouldPlayStarSound(elapsedMillis, index, starSoundsPlayed[index])) {
                Minecraft.getInstance().getSoundManager().play(
                        SimpleSoundInstance.forUI(getStarSound(state), 1.0F)
                );
                starSoundsPlayed[index] = true;
            }
        }
    }

    static boolean shouldPlayStarSound(
            long elapsedMillis,
            int starIndex,
            boolean alreadyPlayed
    ) {
        return !alreadyPlayed
                && elapsedMillis >= starIndex * STAR_ANIMATION_MILLIS;
    }

    static SoundEvent getStarSound(StarState state) {
        return switch (state) {
            case FULL, HALF -> BLING_SOUND;
            case EMPTY -> BONK_SOUND;
        };
    }

    private void renderCustomerTotals(GuiGraphics graphics) {
        int y = topPos + 75;
        graphics.drawString(font, Component.translatable(
                "messages.customers.scoreboard.detail.total_customers", payload.totalCustomers()
        ), leftPos + 18, y, TEXT_COLOR, false);
        graphics.drawString(font, Component.translatable(
                "messages.customers.scoreboard.detail.customers_served", payload.numCustomersServed()
        ), leftPos + 18, y + 14, TEXT_COLOR, false);
        graphics.drawString(font, Component.translatable(
                "messages.customers.scoreboard.detail.customers_gave_up", payload.numCustomersGaveUp()
        ), leftPos + 18, y + 28, TEXT_COLOR, false);
        graphics.drawString(font, Component.translatable(
                "screen.customers.shift_finished.total_items_served",
                payload.totalItemsServed()
        ), leftPos + 18, y + 42, TEXT_COLOR, false);
        graphics.drawString(font, Component.translatable(
                "screen.customers.shift_finished.total_items_crafted",
                payload.totalItemsCrafted()
        ), leftPos + 18, y + 56, TEXT_COLOR, false);
    }

    private void renderPlayerScores(GuiGraphics graphics) {
        List<UUID> playerIds = duplicatePlayers(
                getScoredPlayerIds(
                        payload.numItemsServedByPlayer(),
                        payload.numItemsCraftedByPlayer()
                ),
                TEST_DUPLICATE_PLAYERS
        );
        int playerCount = playerIds.size();
        if (playerCount == 0) {
            return;
        }

        List<String> playerNames = new ArrayList<>(playerCount);
        List<Integer> cardWidths = new ArrayList<>(playerCount);
        for (UUID playerId : playerIds) {
            String playerName = getPlayerName(playerId);
            playerNames.add(playerName);
            cardWidths.add(getPlayerCardWidth(font.width(playerName)));
        }

        int availableWidth = IMAGE_WIDTH - PLAYER_CARD_MARGIN * 2;
        List<List<Integer>> rows = getPlayerCardRows(
                cardWidths,
                availableWidth,
                PLAYER_CARD_GAP
        );
        int playerIndex = 0;
        int y = topPos + 150;
        for (List<Integer> row : rows) {
            int rowWidth = getPlayerCardRowWidth(row, PLAYER_CARD_GAP);
            int x = getCenteredPlayerCardRowX(
                    leftPos,
                    PLAYER_CARD_MARGIN,
                    availableWidth,
                    rowWidth
            );
            for (int cardWidth : row) {
                UUID playerId = playerIds.get(playerIndex);
                renderPlayerScore(
                        graphics,
                        playerId,
                        playerNames.get(playerIndex),
                        payload.numItemsServedByPlayer()
                                .getOrDefault(playerId, 0),
                        payload.numItemsCraftedByPlayer()
                                .getOrDefault(playerId, 0),
                        x,
                        y
                );
                x += cardWidth + PLAYER_CARD_GAP;
                playerIndex++;
            }
            y += PLAYER_CARD_HEIGHT;
        }
    }
    private String getPlayerName(UUID playerId) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientPacketListener connection = minecraft.getConnection();
        PlayerInfo playerInfo = connection == null
                ? null
                : connection.getPlayerInfo(playerId);
        GameProfile profile = playerInfo == null
                ? null
                : playerInfo.getProfile();
        return profile == null
                ? playerId.toString().substring(0, 8)
                : ProfileCUtils.getName(profile);
    }
    private void renderPlayerScore(
            GuiGraphics graphics,
            UUID playerId,
            String playerName,
            int servedCount,
            int craftedCount,
            int x,
            int y
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientPacketListener connection = minecraft.getConnection();
        PlayerInfo playerInfo = connection == null ? null : connection.getPlayerInfo(playerId);
        PlayerSkin skin = playerInfo == null ? DefaultPlayerSkin.get(playerId) : playerInfo.getSkin();


        PlayerFaceRenderer.draw(
                graphics,
                skin,
                x,
                y,
                PLAYER_HEAD_SIZE
        );
        graphics.drawString(
                font,
                playerName,
                x + PLAYER_HEAD_SIZE + 2,
                y + 2,
                TEXT_COLOR,
                false
        );

        int scoreY = y + PLAYER_HEAD_SIZE + 2;
        if (shouldRenderScore(servedCount)) {
            renderPlayerItemScore(
                    graphics,
                    STAR_TEXTURE,
                    servedCount,
                    x,
                    scoreY
            );
            scoreY += font.lineHeight + 1;
        }
        if (shouldRenderScore(craftedCount)) {
            renderPlayerItemScore(
                    graphics,
                    SPOON_TEXTURE,
                    craftedCount,
                    x,
                    scoreY
            );
        }
    }

    private void renderPlayerItemScore(
            GuiGraphics graphics,
            TextureC texture,
            int count,
            int x,
            int y
    ) {
        int iconSize = font.lineHeight;
        float iconScale = getScoreIconScale(iconSize);

        GuiGraphicsCUtils.pushTransform(graphics);
        GuiGraphicsCUtils.translate(graphics, x, y);
        GuiGraphicsCUtils.scale(graphics, iconScale, iconScale);
        GuiGraphicsCUtils.blit(
                graphics,
                texture,
                0,
                0,
                0.0F,
                0.0F,
                SCORE_ICON_TEXTURE_SIZE,
                SCORE_ICON_TEXTURE_SIZE,
                SCORE_ICON_TEXTURE_SIZE,
                SCORE_ICON_TEXTURE_SIZE
        );
        GuiGraphicsCUtils.popTransform(graphics);
        graphics.drawString(
                font,
                Integer.toString(count),
                x + iconSize + 2,
                y,
                TEXT_COLOR,
                false
        );
    }

    /**
     * Returns the scale needed to render a score icon at the text height.
     *
     * @param lineHeight rendered text height
     * @return icon texture scale
     */
    static float getScoreIconScale(int lineHeight) {
        return (float) lineHeight / SCORE_ICON_TEXTURE_SIZE;
    }
    /**
     * Returns every player with a served or crafted score in stable order.
     *
     * @param served served counts by player
     * @param crafted crafted counts by player
     * @return sorted player IDs
     */
    static List<UUID> getScoredPlayerIds(
            Map<UUID, Integer> served,
            Map<UUID, Integer> crafted
    ) {
        TreeSet<UUID> playerIds = new TreeSet<>();
        served.forEach((playerId, count) -> {
            if (shouldRenderScore(count)) {
                playerIds.add(playerId);
            }
        });
        crafted.forEach((playerId, count) -> {
            if (shouldRenderScore(count)) {
                playerIds.add(playerId);
            }
        });
        return List.copyOf(playerIds);
    }

    /**
     * Repeats the rendered players to support testing multi-player layouts.
     *
     * @param playerIds participating player IDs
     * @param duplicateCount number of times each player should appear
     * @return repeated player IDs
     */
    static List<UUID> duplicatePlayers(
            List<UUID> playerIds,
            int duplicateCount
    ) {
        List<UUID> duplicatedPlayers = new ArrayList<>(
                playerIds.size() * duplicateCount
        );
        for (int index = 0; index < duplicateCount; index++) {
            duplicatedPlayers.addAll(playerIds);
        }
        return List.copyOf(duplicatedPlayers);
    }

    /**
     * Calculates a player card width from its profile image and name.
     *
     * @param playerNameWidth rendered player-name width
     * @return player card width
     */
    static int getPlayerCardWidth(int playerNameWidth) {
        return PLAYER_HEAD_SIZE + 2 + playerNameWidth;
    }

    /**
     * Packs player-card widths into rows without exceeding the available width.
     *
     * @param cardWidths player-card widths
     * @param availableWidth width between the screen margins
     * @param gap space between cards
     * @return packed rows of card widths
     */
    static List<List<Integer>> getPlayerCardRows(
            List<Integer> cardWidths,
            int availableWidth,
            int gap
    ) {
        List<List<Integer>> rows = new ArrayList<>();
        List<Integer> row = new ArrayList<>();
        int rowWidth = 0;
        for (int cardWidth : cardWidths) {
            int nextWidth = row.isEmpty()
                    ? cardWidth
                    : rowWidth + gap + cardWidth;
            if (!row.isEmpty() && nextWidth > availableWidth) {
                rows.add(List.copyOf(row));
                row.clear();
                rowWidth = 0;
            }
            if (!row.isEmpty()) {
                rowWidth += gap;
            }
            row.add(cardWidth);
            rowWidth += cardWidth;
        }
        if (!row.isEmpty()) {
            rows.add(List.copyOf(row));
        }
        return List.copyOf(rows);
    }

    /**
     * Calculates the complete width of a row of player cards.
     *
     * @param cardWidths player-card widths in the row
     * @param gap space between cards
     * @return complete row width
     */
    static int getPlayerCardRowWidth(
            List<Integer> cardWidths,
            int gap
    ) {
        return cardWidths.stream().mapToInt(Integer::intValue).sum()
                + Math.max(0, cardWidths.size() - 1) * gap;
    }

    /**
     * Calculates the horizontal position that centers a player-card row.
     *
     * @param leftPos left edge of the finish screen
     * @param margin screen margin
     * @param availableWidth width between the margins
     * @param rowWidth player-card row width
     * @return centered row position
     */
    static int getCenteredPlayerCardRowX(
            int leftPos,
            int margin,
            int availableWidth,
            int rowWidth
    ) {
        return leftPos + margin + (availableWidth - rowWidth) / 2;
    }
    /**
     * Determines whether an icon and count should be shown.
     *
     * @param count player score
     * @return true for positive scores
     */
    static boolean shouldRenderScore(int count) {
        return count > 0;
    }
    private static TextureC getShiftTexture(CustomerSpawnerMode spawnerMode) {
        return switch (spawnerMode) {
            case BREAKFAST -> BREAKFAST_SHIFT_TEXTURE;
            case DAY -> DAY_SHIFT_TEXTURE;
            case DINNER -> DINNER_SHIFT_TEXTURE;
            case LUNCH -> LUNCH_SHIFT_TEXTURE;
            case NIGHT -> NIGHT_SHIFT_TEXTURE;
            case CONTINUOUS, MANUAL -> throw new IllegalArgumentException(
                    "Mode does not have a shift score: " + spawnerMode);
        };
    }

    static StarState getStarState(float percentComplete, int starIndex) {
        int filledHalfStars = Math.round(Mth.clamp(percentComplete, 0.0F, 1.0F) * 10.0F);
        int halfStarsBeforeThisStar = starIndex * 2;
        if (filledHalfStars >= halfStarsBeforeThisStar + 2) {
            return StarState.FULL;
        }
        if (filledHalfStars == halfStarsBeforeThisStar + 1) {
            return StarState.HALF;
        }
        return StarState.EMPTY;
    }

    private static TextureC getStarTexture(StarState state) {
        return switch (state) {
            case FULL -> STAR_TEXTURE;
            case HALF -> HALF_STAR_TEXTURE;
            case EMPTY -> NO_STAR_TEXTURE;
        };
    }

    enum StarState {
        EMPTY,
        HALF,
        FULL
    }
}
