package com.vikingkittens.mc.customers.client.customer;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerBlockMenu;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerMode;

public class CustomerSpawnerBlockScreen extends AbstractContainerScreen<CustomerSpawnerBlockMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Customers.MODID,
            "textures/gui/customer_spawner_ui.png"
    );
    private static final int TEXTURE_WIDTH = 288;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int APPEARANCE_WIDGET_WIDTH = 99;
    private static final int APPEARANCE_TEXT_COLOR = 0x000000;
    private final List<Checkbox> appearanceCheckboxes =
            new ArrayList<>();
    private final List<MultiLineLabel> appearanceLabels =
            new ArrayList<>();
    private CycleButton<CustomerSpawnerMode> modeButton;
    private EditBox maxCustomers;
    private boolean synchronizingAppearanceCheckboxes;

    public CustomerSpawnerBlockScreen(
            CustomerSpawnerBlockMenu menu,
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
        modeButton = addRenderableWidget(
                CycleButton.<CustomerSpawnerMode>builder(
                                CustomerSpawnerMode::getTitle
                        )
                        .withValues(CustomerSpawnerMode.values())
                        .withInitialValue(menu.getSpawnerMode())
                        .displayOnlyValue()
                        .create(
                                leftPos + 177,
                                topPos + 29,
                                63,
                                20,
                                Component.empty(),
                                (button, mode) -> send(
                                        menu.modeButtonId(mode)
                                )
                        )
        );
        maxCustomers = new EditBox(
                font,
                leftPos + 177,
                topPos + 65,
                32,
                18,
                Component.translatable(
                        "screen.customers.customer_spawner.max_customers"
                )
        );
        maxCustomers.setMaxLength(3);
        maxCustomers.setFilter(
                CustomerSpawnerBlockMenu::isValidMaxCustomersText
        );
        maxCustomers.setValue(Integer.toString(menu.getMaxCustomers()));
        maxCustomers.setResponder(value -> {
            if (!value.isEmpty()) {
                send(menu.maxCustomersButtonId(Integer.parseInt(value)));
            }
        });
        addRenderableWidget(maxCustomers);

        int y = 101;
        int appearanceTextWidth = APPEARANCE_WIDGET_WIDTH
                - Checkbox.getBoxSize(font)
                - 4;
        for (int index = 0; index < menu.getAppearanceIds().size(); index++) {
            int appearanceIndex = index;
            Component appearanceName = menu.getAppearanceName(index);
            Checkbox checkbox = addRenderableWidget(
                    Checkbox.builder(Component.empty(), font)
                            .pos(leftPos + 177, topPos + y)
                            .selected(menu.isAppearanceEnabled(index))
                            .onValueChange((changedCheckbox, selected) -> {
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
            appearanceLabels.add(MultiLineLabel.create(
                    font,
                    appearanceName,
                    appearanceTextWidth
            ));
            y += 20;
        }
    }

    @Override
    protected void containerTick() {
        modeButton.setValue(menu.getSpawnerMode());
        String synchronizedMaxCustomers =
                Integer.toString(menu.getMaxCustomers());
        if (
                !maxCustomers.isFocused()
                        && !maxCustomers.getValue()
                                .equals(synchronizedMaxCustomers)
        ) {
            maxCustomers.setValue(synchronizedMaxCustomers);
        }

        synchronizingAppearanceCheckboxes = true;
        for (
                int index = 0;
                index < appearanceCheckboxes.size();
                index++
        ) {
            Checkbox checkbox = appearanceCheckboxes.get(index);
            if (
                    checkbox.selected()
                            != menu.isAppearanceEnabled(index)
            ) {
                checkbox.onPress();
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
        graphics.blit(
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
                        "screen.customers.customer_spawner.cost"
                ),
                156,
                6,
                0x404040,
                false
        );
        graphics.drawString(
                font,
                Component.translatable(
                        "screen.customers.customer_spawner.mode"
                ),
                177,
                17,
                0x404040,
                false
        );
        graphics.drawString(
                font,
                Component.translatable(
                        "screen.customers.customer_spawner.max"
                ),
                177,
                53,
                0x404040,
                false
        );
        graphics.drawString(
                font,
                Component.translatable(
                        "screen.customers.customer_spawner.appearance"
                ),
                177,
                89,
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
        renderAppearanceLabels(graphics);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderAppearanceLabels(GuiGraphics graphics) {
        for (int index = 0; index < appearanceCheckboxes.size(); index++) {
            Checkbox checkbox = appearanceCheckboxes.get(index);
            MultiLineLabel label = appearanceLabels.get(index);
            int labelHeight = label.getLineCount() * font.lineHeight;
            int labelY = checkbox.getY()
                    + Checkbox.getBoxSize(font) / 2
                    - labelHeight / 2;
            label.renderLeftAlignedNoShadow(
                    graphics,
                    checkbox.getX() + Checkbox.getBoxSize(font) + 4,
                    labelY,
                    font.lineHeight,
                    APPEARANCE_TEXT_COLOR
            );
        }
    }

    private void send(int id) {
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }
}
