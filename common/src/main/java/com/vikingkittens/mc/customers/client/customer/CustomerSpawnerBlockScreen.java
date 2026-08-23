package com.vikingkittens.mc.customers.client.customer;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.client.common.IconsScaleControl;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerBlockEntity;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerBlockMenu;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerLevelSettings;
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
    private static final ResourceLocation ARROW_LEFT = texture("arrow_left");
    private static final ResourceLocation ARROW_LEFT_PRESSED = texture("arrow_left_pressed");
    private static final ResourceLocation ARROW_RIGHT = texture("arrow_right");
    private static final ResourceLocation ARROW_RIGHT_PRESSED = texture("arrow_right_pressed");
    private static final ResourceLocation HALFSTAR_SMALL = texture("halfstar_small");
    private static final ResourceLocation NOSTAR_SMALL = texture("nostar_small");
    private static final ResourceLocation STAR_SMALL = texture("star_small");
    private final List<Checkbox> appearanceCheckboxes =
            new ArrayList<>();
    private final List<MultiLineLabel> appearanceLabels =
            new ArrayList<>();
    private ModeButton modeButton;
    private TextureButton decrementLevelButton;
    private TextureButton incrementLevelButton;
    private IconsScaleControl requiredStars;
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
        modeButton = addRenderableWidget(new ModeButton(leftPos + imageWidth - 22, topPos + 6));
        decrementLevelButton = addRenderableWidget(new TextureButton(
                leftPos + 4,
                topPos + 4,
                ARROW_LEFT,
                ARROW_LEFT_PRESSED,
                menu.decrementLevelButtonId()
        ));
        incrementLevelButton = addRenderableWidget(new TextureButton(
                leftPos + 245,
                topPos + 4,
                ARROW_RIGHT,
                ARROW_RIGHT_PRESSED,
                menu.incrementLevelButtonId()
        ));
        requiredStars = addRenderableWidget(new IconsScaleControl(
                leftPos + 177,
                topPos + 29,
                STAR_SMALL,
                HALFSTAR_SMALL,
                NOSTAR_SMALL,
                16,
                16,
                5,
                CustomerSpawnerLevelSettings.MINIMUM_REQUIRED_STARS,
                CustomerSpawnerLevelSettings.MAXIMUM_REQUIRED_STARS,
                0.5F,
                menu.getRequiredStars(),
                value -> send(menu.requiredStarsButtonId(value))
        ));
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
        int appearanceTextWidth = APPEARANCE_WIDGET_WIDTH - 20;
        for (int index = 0; index < menu.getAppearanceIds().size(); index++) {
            int appearanceIndex = index;
            Component appearanceName = menu.getAppearanceName(index);
            Checkbox checkbox = addRenderableWidget(
                    new Checkbox(
                            leftPos + 177,
                            topPos + y,
                            20,
                            20,
                            Component.empty(),
                            menu.isAppearanceEnabled(index),
                            false
                    ) {
                        @Override
                        public void onPress() {
                            super.onPress();
                            if (!synchronizingAppearanceCheckboxes) {
                                send(menu.appearanceButtonId(appearanceIndex));
                            }
                        }
                    }
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
        modeButton.setMode(menu.getSpawnerMode());
        decrementLevelButton.visible = menu.getSelectedLevel() > 0;
        incrementLevelButton.visible = menu.getSelectedLevel() < CustomerSpawnerBlockEntity.MAX_LEVELS - 1;
        requiredStars.setValue(menu.getRequiredStars());
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
        graphics.drawString(
                font,
                Component.translatable(
                        "screen.customers.customer_spawner.title",
                        menu.getSpawnerMode().getTitle(),
                        menu.getSelectedLevel() + 1
                ),
                24,
                6,
                0x404040,
                false
        );
        graphics.drawString(
                font,
                Component.translatable(
                        "container.inventory"
                ),
                8,
                inventoryLabelY,
                0x404040,
                false
        );
        graphics.drawString(
                font,
                Component.translatable(
                        "screen.customers.customer_spawner.stars_required"
                ),
                177,
                17,
                0x404040,
                false
        );
        graphics.drawString(
                font,
                Component.translatable("screen.customers.customer_spawner.max"),
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
        renderBackground(graphics);
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
                    + 10
                    - labelHeight / 2;
            label.renderLeftAlignedNoShadow(
                    graphics,
                    checkbox.getX() + 24,
                    labelY,
                    font.lineHeight,
                    APPEARANCE_TEXT_COLOR
            );
        }
    }

    private void send(int id) {
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(Customers.MODID, "textures/gui/" + name + ".png");
    }

    private static ResourceLocation modeTexture(CustomerSpawnerMode mode) {
        return texture("mode_" + mode.getSerializedName());
    }

    private class ModeButton extends AbstractButton {
        private CustomerSpawnerMode mode = menu.getSpawnerMode();

        private ModeButton(int x, int y) {
            super(x, y, 16, 16, menu.getSpawnerMode().getTitle());
        }

        private void setMode(CustomerSpawnerMode mode) {
            this.mode = mode;
            setMessage(mode.getTitle());
        }

        @Override
        public void onPress() {
            CustomerSpawnerMode[] modes = CustomerSpawnerMode.values();
            setMode(modes[(mode.ordinal() + 1) % modes.length]);
            send(menu.modeButtonId(mode));
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.blit(modeTexture(mode), getX(), getY(), 0, 0, 16, 16, 16, 16);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narration) {}
    }

    private class TextureButton extends AbstractButton {
        private final ResourceLocation texture;
        private final ResourceLocation pressedTexture;
        private final int buttonId;

        private TextureButton(int x, int y, ResourceLocation texture, ResourceLocation pressedTexture, int buttonId) {
            super(x, y, 12, 12, Component.empty());
            this.texture = texture;
            this.pressedTexture = pressedTexture;
            this.buttonId = buttonId;
        }

        @Override
        public void onPress() {
            send(buttonId);
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
