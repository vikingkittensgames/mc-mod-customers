package com.vikingkittens.mc.customers.client.customer;

import com.mojang.authlib.GameProfile;
import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.client.compatability.GuiGraphicsCUtils;
import com.vikingkittens.mc.customers.client.compatability.TextureC;
import com.vikingkittens.mc.customers.compatability.ProfileCUtils;
import com.vikingkittens.mc.customers.customer.CustomerShiftFinishedPayload;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.util.Mth;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public class CustomerShiftFinishedScreen extends Screen {
    private static final int IMAGE_WIDTH = 256;
    private static final int IMAGE_HEIGHT = 210;
    private static final int TEXT_COLOR = 0xFF3F3028;
    private static final int STAR_SIZE = 32;
    private static final int STAR_GAP = 8;
    private static final long STAR_ANIMATION_MILLIS = 500L;
    private static final int PLAYER_HEAD_SIZE = 16;
    private static final int PLAYER_ROW_WIDTH = 220;

    private static final SoundEvent BLING_SOUND = SoundEvent.createVariableRangeEvent(
            Identifier.fromNamespaceAndPath(Customers.MODID, "bling")
    );
    private static final SoundEvent BONK_SOUND = SoundEvent.createVariableRangeEvent(
            Identifier.fromNamespaceAndPath(Customers.MODID, "bonk")
    );

    private static final TextureC RECEIPT_TEXTURE = texture("reciept.png");
    private static final TextureC STAR_TEXTURE = texture("star.png");
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
        return new TextureC(
                Customers.MODID,
                "textures/gui/" + fileName
        );
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
        GuiGraphicsCUtils.blit(
                graphics,
                getShiftTexture(payload.spawnerMode()),
                leftPos + 225,
                topPos + 12,
                0.0F,
                0.0F,
                16,
                16,
                16,
                16
        );
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
    }
    private void renderPlayerScores(GuiGraphics graphics) {
        int playerCount = payload.numItemsServedByPlayer().size();
        if (playerCount == 0) {
            return;
        }

        int entryWidth = Math.min(64, PLAYER_ROW_WIDTH / playerCount);
        int rowWidth = entryWidth * playerCount;
        int x = leftPos + (IMAGE_WIDTH - rowWidth) / 2;
        int index = 0;
        for (Map.Entry<UUID, Integer> entry : payload.numItemsServedByPlayer().entrySet()) {
            renderPlayerScore(graphics, entry.getKey(), entry.getValue(), x + index * entryWidth, entryWidth);
            index++;
        }
    }

    private void renderPlayerScore(GuiGraphics graphics, UUID playerId, int itemCount, int x, int entryWidth) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientPacketListener connection = minecraft.getConnection();
        PlayerInfo playerInfo = connection == null ? null : connection.getPlayerInfo(playerId);
        GameProfile profile = playerInfo == null ? null : playerInfo.getProfile();
        PlayerSkin skin = playerInfo == null ? DefaultPlayerSkin.get(playerId) : playerInfo.getSkin();
        String playerName = profile == null
                ? playerId.toString().substring(0, 8)
                : ProfileCUtils.getName(profile);
        String visibleName = font.plainSubstrByWidth(playerName, entryWidth - 2);

        PlayerFaceRenderer.draw(graphics, skin, x + (entryWidth - PLAYER_HEAD_SIZE) / 2,
                topPos + 145, PLAYER_HEAD_SIZE);

        int centerX = x + entryWidth / 2;
        graphics.drawString(
                font,
                visibleName,
                centerX - font.width(visibleName) / 2,
                topPos + 163,
                TEXT_COLOR,
                false
        );

        Component itemCountText = Component.translatable(
                "screen.customers.shift_finished.items_served",
                itemCount
        );
        graphics.drawString(font, itemCountText, centerX - font.width(itemCountText) / 2,
                topPos + 174, TEXT_COLOR, false);
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
