package com.vikingkittens.mc.customers.client.supplier;

import java.util.List;
import java.util.stream.IntStream;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.client.common.AppearanceListWidget;
import com.vikingkittens.mc.customers.supplier.SupplierSpawnerBlockMenu;

public class SupplierSpawnerBlockScreen
        extends AbstractContainerScreen<SupplierSpawnerBlockMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    Customers.MODID,
                    "textures/gui/supplier_spawner_ui.png"
            );
    private static final ResourceLocation AUTO_COST_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    Customers.MODID,
                    "textures/gui/supplier_spawner_ui_auto.png"
            );
    private static final int TEXTURE_WIDTH = 288;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int APPEARANCE_LIST_X = 177;
    private static final int APPEARANCE_LIST_Y = 29;
    private static final int APPEARANCE_LIST_WIDTH = 99;
    private static final int APPEARANCE_LIST_HEIGHT = 185;
    private static final int MANUAL_COST_TOGGLE_X = 156;
    private static final int MANUAL_COST_Y = 126;
    private static final int MANUAL_COST_TOGGLE_SIZE = 12;
    private static final ResourceLocation MANUAL_COST =
            ResourceLocation.fromNamespaceAndPath(Customers.MODID, "textures/gui/cost.png");
    private static final ResourceLocation AUTOMATIC_COST =
            ResourceLocation.fromNamespaceAndPath(Customers.MODID, "textures/gui/costauto.png");
    private ManualCostButton manualCostButton;

    public SupplierSpawnerBlockScreen(
            SupplierSpawnerBlockMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title);
        imageWidth = TEXTURE_WIDTH;
        imageHeight = 222;
        inventoryLabelY = 128;
    }

    @Override
    protected void init() {
        super.init();
        manualCostButton = addRenderableWidget(new ManualCostButton(
                leftPos + MANUAL_COST_TOGGLE_X,
                topPos + MANUAL_COST_Y
        ));

        addRenderableWidget(new AppearanceListWidget(
                font,
                leftPos + APPEARANCE_LIST_X,
                topPos + APPEARANCE_LIST_Y,
                APPEARANCE_LIST_WIDTH,
                APPEARANCE_LIST_HEIGHT,
                Component.translatable("screen.customers.supplier_spawner.appearance"),
                appearanceNames(),
                menu::isAppearanceEnabled,
                index -> send(menu.appearanceButtonId(index))
        ));
    }

    @Override
    protected void containerTick() {
        manualCostButton.visible = menu.isEconomyEnabled() && !menu.isForceAutoCost();
    }

    @Override
    protected void renderBg(
            GuiGraphics graphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        graphics.blit(
                menu.usesAutomaticCost() ? AUTO_COST_TEXTURE : TEXTURE,
                leftPos,
                topPos,
                0.0F,
                0.0F,
                imageWidth,
                imageHeight,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );
    }

    @Override
    protected void renderLabels(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        super.renderLabels(graphics, mouseX, mouseY);
        graphics.drawString(
                font,
                Component.translatable(
                        "screen.customers.supplier_spawner.appearance"
                ),
                177,
                17,
                0x404040,
                false
        );
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderAutomaticCosts(graphics);
        renderTooltip(graphics, mouseX, mouseY);
        renderAutomaticCostTooltip(graphics, mouseX, mouseY);
    }

    private List<Component> appearanceNames() {
        return IntStream.range(0, menu.getAppearanceIds().size())
                .mapToObj(menu::getAppearanceName)
                .toList();
    }

    private void renderAutomaticCosts(GuiGraphics graphics) {
        if (!menu.usesAutomaticCost()) {
            return;
        }
        for (int row = 0; row < 6; row++) {
            for (int pair = 0; pair < 4; pair++) {
                ItemStack cost = menu.getAutomaticCost(row, pair);
                if (!cost.isEmpty()) {
                    int x = leftPos + SupplierSpawnerBlockMenu.getContainerSlotX(pair * 2 + 1);
                    int y = topPos + 18 + row * 18;
                    graphics.renderItem(cost, x, y);
                    graphics.renderItemDecorations(font, cost, x, y);
                }
            }
        }
    }

    private void renderAutomaticCostTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!menu.usesAutomaticCost()) {
            return;
        }
        for (int row = 0; row < 6; row++) {
            for (int pair = 0; pair < 4; pair++) {
                int x = leftPos + SupplierSpawnerBlockMenu.getContainerSlotX(pair * 2 + 1);
                int y = topPos + 18 + row * 18;
                ItemStack cost = menu.getAutomaticCost(row, pair);
                if (!cost.isEmpty() && mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                    graphics.renderTooltip(font, cost, mouseX, mouseY);
                    return;
                }
            }
        }
    }

    private void send(int id) {
        minecraft.gameMode.handleInventoryButtonClick(
                menu.containerId,
                id
        );
    }

    private class ManualCostButton extends AbstractButton {
        private ManualCostButton(int x, int y) {
            super(x, y, MANUAL_COST_TOGGLE_SIZE, MANUAL_COST_TOGGLE_SIZE, Component.literal("$"));
        }

        @Override
        public void onPress() {
            send(menu.autoCostButtonId());
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.blit(
                    menu.isAutoCost() ? AUTOMATIC_COST : MANUAL_COST,
                    getX(),
                    getY(),
                    0,
                    0,
                    MANUAL_COST_TOGGLE_SIZE,
                    MANUAL_COST_TOGGLE_SIZE,
                    MANUAL_COST_TOGGLE_SIZE,
                    MANUAL_COST_TOGGLE_SIZE
            );
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narration) {}
    }
}
