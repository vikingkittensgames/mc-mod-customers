package com.vikingkittens.mc.customers.client.customer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.authlib.GameProfile;
import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.customer.CustomerShiftFinishedPayload;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.Map;
import java.util.UUID;

public class CustomerShiftFinishedScreen extends Screen {
    private static final int IMAGE_WIDTH = 256;
    private static final int IMAGE_HEIGHT = 210;
    private static final int TEXT_COLOR = 0xFF3F3028;
    private static final int STAR_SIZE = 32;
    private static final int STAR_GAP = 8;
    private static final int PLAYER_HEAD_SIZE = 16;
    private static final int PLAYER_ROW_WIDTH = 220;

    private static final ResourceLocation RECEIPT_TEXTURE = texture("reciept.png");
    private static final ResourceLocation STAR_TEXTURE = texture("star.png");
    private static final ResourceLocation HALF_STAR_TEXTURE = texture("halfstar.png");
    private static final ResourceLocation NO_STAR_TEXTURE = texture("nostar.png");
    private static final ResourceLocation BREAKFAST_SHIFT_TEXTURE = texture("shift_breakfast.png");
    private static final ResourceLocation DAY_SHIFT_TEXTURE = texture("shift_day.png");
    private static final ResourceLocation DINNER_SHIFT_TEXTURE = texture("shift_dinner.png");
    private static final ResourceLocation LUNCH_SHIFT_TEXTURE = texture("shift_lunch.png");
    private static final ResourceLocation NIGHT_SHIFT_TEXTURE = texture("shift_night.png");

    private final CustomerShiftFinishedPayload payload;
    private int leftPos;
    private int topPos;

    public CustomerShiftFinishedScreen(CustomerShiftFinishedPayload payload) {
        super(Component.translatable("screen.customers.shift_finished"));
        this.payload = payload;
    }

    private static ResourceLocation texture(String fileName) {
        return ResourceLocation.fromNamespaceAndPath(Customers.MODID, "textures/gui/" + fileName);
    }

    @Override
    protected void init() {
        leftPos = (width - IMAGE_WIDTH) / 2;
        topPos = (height - IMAGE_HEIGHT) / 2;
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

        graphics.blit(RECEIPT_TEXTURE, leftPos, topPos, 0.0F, 0.0F, IMAGE_WIDTH, IMAGE_HEIGHT, 256, 256);
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
        graphics.blit(getShiftTexture(payload.spawnerMode()), leftPos + 225, topPos + 12,
                0.0F, 0.0F, 16, 16, 16, 16);
    }

    private void renderStars(GuiGraphics graphics) {
        int totalWidth = STAR_SIZE * 5 + STAR_GAP * 4;
        int x = leftPos + (IMAGE_WIDTH - totalWidth) / 2;
        for (int index = 0; index < 5; index++) {
            graphics.blit(
                    getStarTexture(getStarState(payload.percentComplete(), index)),
                    x + index * (STAR_SIZE + STAR_GAP),
                    topPos + 32,
                    0.0F,
                    0.0F,
                    STAR_SIZE,
                    STAR_SIZE,
                    STAR_SIZE,
                    STAR_SIZE
            );
        }
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
        String playerName = profile == null ? playerId.toString().substring(0, 8) : profile.getName();
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

    private static ResourceLocation getShiftTexture(CustomerSpawnerMode spawnerMode) {
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

    private static ResourceLocation getStarTexture(StarState state) {
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
