package com.vikingkittens.mc.customers.client.supplier;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.client.compatability.GuiGraphicsCUtils;
import com.vikingkittens.mc.customers.supplier.SupplierSpawnerBlockMenu;

public class SupplierSpawnerBlockScreen
        extends AbstractContainerScreen<SupplierSpawnerBlockMenu> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(
                    Customers.MODID,
                    "textures/gui/supplier_spawner_ui.png"
            );
    private static final int TEXTURE_WIDTH = 288;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int APPEARANCE_WIDGET_WIDTH = 99;
    private static final int APPEARANCE_TEXT_COLOR = 0xFF000000;
    private final List<Checkbox> appearanceCheckboxes =
            new ArrayList<>();
    private final List<Component> appearanceLabels =
            new ArrayList<>();
    private boolean synchronizingAppearanceCheckboxes;

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
        appearanceCheckboxes.clear();
        appearanceLabels.clear();

        int y = 29;
        int appearanceTextWidth = APPEARANCE_WIDGET_WIDTH
                - Checkbox.getBoxSize(font)
                - 4;
        for (int index = 0;
                index < menu.getAppearanceIds().size();
                index++) {
            int appearanceIndex = index;
            Component appearanceName =
                    menu.getAppearanceName(index);
            Checkbox checkbox = addRenderableWidget(
                    Checkbox.builder(Component.empty(), font)
                            .pos(leftPos + 177, topPos + y)
                            .selected(
                                    menu.isAppearanceEnabled(index)
                            )
                            .onValueChange((
                                    changedCheckbox,
                                    selected
                            ) -> {
                                if (!synchronizingAppearanceCheckboxes) {
                                    send(menu.appearanceButtonId(
                                            appearanceIndex
                                    ));
                                }
                            })
                            .build()
            );
            checkbox.setWidth(APPEARANCE_WIDGET_WIDTH);
            checkbox.setMessage(appearanceName);
            appearanceCheckboxes.add(checkbox);
            appearanceLabels.add(appearanceName);
            y += 20;
        }
    }

    @Override
    protected void containerTick() {
        synchronizingAppearanceCheckboxes = true;
        for (int index = 0;
                index < appearanceCheckboxes.size();
                index++) {
            Checkbox checkbox = appearanceCheckboxes.get(index);
            if (checkbox.selected()
                    != menu.isAppearanceEnabled(index)) {
                checkbox.onPress(new MouseButtonEvent(0, 0, new MouseButtonInfo(0, 0)));
            }
        }
        synchronizingAppearanceCheckboxes = false;
    }

    @Override
    protected void renderBg(
            GuiGraphics graphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        GuiGraphicsCUtils.blit(
                graphics,
                TEXTURE,
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
                0xFF404040,
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
        renderAppearanceLabels(graphics);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderAppearanceLabels(
            GuiGraphics graphics
    ) {
        for (int index = 0;
                index < appearanceCheckboxes.size();
                index++) {
            Checkbox checkbox = appearanceCheckboxes.get(index);
            Component label = appearanceLabels.get(index);
            List<net.minecraft.util.FormattedCharSequence> lines = font.split(
                    label,
                    APPEARANCE_WIDGET_WIDTH - Checkbox.getBoxSize(font) - 4
            );
            int labelHeight = lines.size() * font.lineHeight;
            int labelY = checkbox.getY()
                    + Checkbox.getBoxSize(font) / 2
                    - labelHeight / 2;
            for (int line = 0; line < lines.size(); line++) {
                graphics.drawString(
                        font,
                        lines.get(line),
                        checkbox.getX() + Checkbox.getBoxSize(font) + 4,
                        labelY + line * font.lineHeight,
                        APPEARANCE_TEXT_COLOR,
                        false
                );
            }
        }
    }

    private void send(int id) {
        minecraft.gameMode.handleInventoryButtonClick(
                menu.containerId,
                id
        );
    }
}
