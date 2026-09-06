package com.vikingkittens.mc.customers.client.customer;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.client.common.IconsScaleControl;
import com.vikingkittens.mc.customers.client.compatability.GuiGraphicsCUtils;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerBlockEntity;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerBlockMenu;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerLevelSettings;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerMode;

public class CustomerSpawnerBlockScreen extends AbstractContainerScreen<CustomerSpawnerBlockMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            Customers.MODID,
            "textures/gui/customer_spawner_ui.png"
    );
    private static final int TEXTURE_WIDTH = 288;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int APPEARANCE_WIDGET_WIDTH = 99;
    private static final int APPEARANCE_TEXT_COLOR = 0xFF000000;
    private static final int MAX_CUSTOMERS_X = 177;
    private static final int PET_PERCENTAGE_X = 200;
    private static final int PET_PERCENT_SIGN_X = 229;
    private static final int PET_PANEL_SHIFT = 90;
    private static final int PET_PANEL_GAP = 2;
    private static final int PET_PANEL_WIDTH = 140;
    private static final int PET_PANEL_HEIGHT = 222;
    private static final int PET_PANEL_TITLE_X = 8;
    private static final int PET_PANEL_TITLE_Y = 8;
    private static final int PET_PANEL_LIST_X = 8;
    private static final int PET_PANEL_ALL_Y = 24;
    private static final int PET_PANEL_LIST_Y = 42;
    private static final int PET_PANEL_LIST_WIDTH = 124;
    private static final int PET_PANEL_LIST_HEIGHT = 172;
    private static final int PET_PANEL_ROW_HEIGHT = 18;
    private static final int PET_PANEL_CHECKBOX_SIZE = 10;
    private static final int PET_PANEL_SCROLLBAR_WIDTH = 4;
    private static final int PET_PANEL_SCROLL_AMOUNT = 18;
    private static final int AVOID_LABEL_X = 241;
    private static final int AVOID_ICON_X = 252;
    private static final int AVOID_ICON_Y = 65;
    private static final Identifier ARROW_LEFT = texture("arrow_left");
    private static final Identifier ARROW_LEFT_PRESSED = texture("arrow_left_pressed");
    private static final Identifier ARROW_RIGHT = texture("arrow_right");
    private static final Identifier ARROW_RIGHT_PRESSED = texture("arrow_right_pressed");
    private static final Identifier HALFSTAR_SMALL = texture("halfstar_small");
    private static final Identifier NOSTAR_SMALL = texture("nostar_small");
    private static final Identifier STAR_SMALL = texture("star_small");
    private static final Identifier PET_PANEL = texture("pet-panel");
    private final List<AppearanceCheckbox> appearanceCheckboxes =
            new ArrayList<>();
    private final List<Component> appearanceLabels =
            new ArrayList<>();
    private ModeButton modeButton;
    private TextureButton decrementLevelButton;
    private TextureButton incrementLevelButton;
    private IconsScaleControl requiredStars;
    private EditBox maxCustomers;
    private EditBox petPercentage;
    private PetsButton petsButton;
    private boolean petPanelOpen;
    private int petScrollOffset;
    private int lastSelectedLevel;

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
        leftPos = (width - imageWidth) / 2 - (petPanelOpen ? PET_PANEL_SHIFT : 0);
        menu.setPetFoodCostSlotVisible(petPanelOpen);
        lastSelectedLevel = menu.getSelectedLevel();
        appearanceCheckboxes.clear();
        appearanceLabels.clear();
        modeButton = addRenderableWidget(new ModeButton(
                leftPos + imageWidth - 22,
                topPos + 6
        ));
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
                leftPos + MAX_CUSTOMERS_X,
                topPos + 65,
                20,
                18,
                Component.translatable(
                        "screen.customers.customer_spawner.max_customers"
                )
        );
        maxCustomers.setMaxLength(2);
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
        petPercentage = new EditBox(
                font,
                leftPos + PET_PERCENTAGE_X,
                topPos + 65,
                26,
                18,
                Component.translatable(
                        "screen.customers.customer_spawner.pets"
                )
        );
        petPercentage.setMaxLength(3);
        petPercentage.setFilter(
                CustomerSpawnerBlockMenu::isValidPetPercentageText
        );
        petPercentage.setValue(Integer.toString(menu.getPetPercentagePercent()));
        petPercentage.setResponder(value -> {
            if (!value.isEmpty()) {
                send(menu.petPercentageButtonId(Integer.parseInt(value)));
            }
        });
        addRenderableWidget(petPercentage);
        petsButton = addRenderableWidget(new PetsButton(
                leftPos + PET_PERCENTAGE_X,
                topPos + 52
        ));

        int y = 101;
        int appearanceTextWidth = APPEARANCE_WIDGET_WIDTH
                - PET_PANEL_CHECKBOX_SIZE
                - 4;
        for (int index = 0; index < menu.getAppearanceIds().size(); index++) {
            Component appearanceName = menu.getAppearanceName(index);
            AppearanceCheckbox checkbox = addRenderableWidget(
                    new AppearanceCheckbox(
                            leftPos + 177,
                            topPos + y,
                            index
                    )
            );
            appearanceCheckboxes.add(checkbox);
            appearanceLabels.add(appearanceName);
            y += 20;
        }
    }

    @Override
    protected void containerTick() {
        modeButton.setMode(menu.getSpawnerMode());
        decrementLevelButton.visible = menu.getSelectedLevel() > 0;
        incrementLevelButton.visible =
                menu.getSelectedLevel() < CustomerSpawnerBlockEntity.MAX_LEVELS - 1;
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
        String synchronizedPetPercentage =
                Integer.toString(menu.getPetPercentagePercent());
        if (
                !petPercentage.isFocused()
                        && !petPercentage.getValue()
                                .equals(synchronizedPetPercentage)
        ) {
            petPercentage.setValue(synchronizedPetPercentage);
        }

        if (lastSelectedLevel != menu.getSelectedLevel()) {
            lastSelectedLevel = menu.getSelectedLevel();
            petScrollOffset = 0;
        }
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
        if (petPanelOpen) {
            GuiGraphicsCUtils.blit(
                    graphics,
                    PET_PANEL,
                    getPetPanelX(),
                    topPos,
                    0,
                    0,
                    PET_PANEL_WIDTH,
                    PET_PANEL_HEIGHT,
                    PET_PANEL_WIDTH,
                    PET_PANEL_HEIGHT
            );
        }
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
                0xFF404040,
                false
        );
        if (menu.hasAvoidBlock()) {
            graphics.drawString(
                    font,
                    Component.translatable("screen.customers.customer_spawner.avoid"),
                    AVOID_LABEL_X,
                    53,
                    0xFF404040,
                    false
            );
            graphics.renderItem(menu.getAvoidBlockItem(), AVOID_ICON_X, AVOID_ICON_Y);
        }
        graphics.drawString(
                font,
                Component.translatable("container.inventory"),
                8,
                inventoryLabelY,
                0xFF404040,
                false
        );
        graphics.drawString(
                font,
                Component.translatable(
                        "screen.customers.customer_spawner.stars_required"
                ),
                177,
                17,
                0xFF404040,
                false
        );
        graphics.drawString(
                font,
                Component.translatable(
                        "screen.customers.customer_spawner.max"
                ),
                MAX_CUSTOMERS_X,
                53,
                0xFF404040,
                false
        );
        graphics.drawString(
                font,
                "%",
                PET_PERCENT_SIGN_X,
                70,
                0xFF404040,
                false
        );
        graphics.drawString(
                font,
                Component.translatable(
                        "screen.customers.customer_spawner.appearance"
                ),
                177,
                89,
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
        renderPetPanel(graphics);
        renderTooltip(graphics, mouseX, mouseY);
        renderAvoidBlockTooltip(graphics, mouseX, mouseY);
        renderPetFoodTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isEscape() && petPanelOpen) {
            petPanelOpen = false;
            petScrollOffset = 0;
            rebuildWidgets();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (event.button() == 0 && petPanelOpen && clickPetPanel(mouseX, mouseY)) {
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (petPanelOpen && isInPetPanelList(mouseX, mouseY)) {
            petScrollOffset = Mth.clamp(
                    petScrollOffset - (int)Math.signum(scrollY) * PET_PANEL_SCROLL_AMOUNT,
                    0,
                    getMaxPetScrollOffset()
            );
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void renderAvoidBlockTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!menu.hasAvoidBlock()
                || mouseX < leftPos + AVOID_ICON_X
                || mouseX >= leftPos + AVOID_ICON_X + 16
                || mouseY < topPos + AVOID_ICON_Y
                || mouseY >= topPos + AVOID_ICON_Y + 16) {
            return;
        }
        ItemStack avoidBlockItem = menu.getAvoidBlockItem();
        graphics.setTooltipForNextFrame(font, avoidBlockItem, mouseX, mouseY);
    }

    private void renderPetFoodTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!petPanelOpen || !isInPetPanelList(mouseX, mouseY)) {
            return;
        }
        int row = ((int)mouseY - topPos - PET_PANEL_LIST_Y + petScrollOffset) / PET_PANEL_ROW_HEIGHT;
        if (row < 0 || row >= menu.getPetTypes().size()) {
            return;
        }
        int foodIconX = getPetPanelX() + PET_PANEL_LIST_X + PET_PANEL_CHECKBOX_SIZE + 4;
        int foodIconY = topPos + PET_PANEL_LIST_Y + row * PET_PANEL_ROW_HEIGHT - petScrollOffset + 1;
        if (mouseX >= foodIconX && mouseX < foodIconX + 16 && mouseY >= foodIconY && mouseY < foodIconY + 16) {
            graphics.setTooltipForNextFrame(font, menu.getPetTypeFood(row), mouseX, mouseY);
        }
    }

    private void renderAppearanceLabels(GuiGraphics graphics) {
        for (int index = 0; index < appearanceCheckboxes.size(); index++) {
            AppearanceCheckbox checkbox = appearanceCheckboxes.get(index);
            Component label = appearanceLabels.get(index);
            List<net.minecraft.util.FormattedCharSequence> lines = font.split(
                    label,
                    APPEARANCE_WIDGET_WIDTH - PET_PANEL_CHECKBOX_SIZE - 4
            );
            int labelHeight = lines.size() * font.lineHeight;
            int labelY = checkbox.getY()
                    + PET_PANEL_CHECKBOX_SIZE / 2
                    - labelHeight / 2;
            for (int line = 0; line < lines.size(); line++) {
                graphics.drawString(
                        font,
                        lines.get(line),
                        checkbox.getX() + PET_PANEL_CHECKBOX_SIZE + 4,
                        labelY + line * font.lineHeight,
                        APPEARANCE_TEXT_COLOR,
                        false
                );
            }
        }
    }

    private void renderPetPanel(GuiGraphics graphics) {
        if (!petPanelOpen) {
            return;
        }
        int panelX = getPetPanelX();
        graphics.drawString(
                font,
                Component.translatable("screen.customers.customer_spawner.pets"),
                panelX + PET_PANEL_TITLE_X,
                topPos + PET_PANEL_TITLE_Y,
                0xFF404040,
                false
        );
        renderPetCheckbox(
                graphics,
                menu.areAllPetTypesEnabled(),
                panelX + PET_PANEL_LIST_X,
                topPos + PET_PANEL_ALL_Y
        );
        graphics.drawString(
                font,
                Component.translatable("screen.customers.customer_spawner.all"),
                panelX + PET_PANEL_LIST_X + PET_PANEL_CHECKBOX_SIZE + 4,
                topPos + PET_PANEL_ALL_Y + (PET_PANEL_CHECKBOX_SIZE - font.lineHeight) / 2,
                0xFF000000,
                false
        );
        int listX = panelX + PET_PANEL_LIST_X;
        int listY = topPos + PET_PANEL_LIST_Y;
        graphics.enableScissor(
                listX,
                listY,
                listX + PET_PANEL_LIST_WIDTH,
                listY + PET_PANEL_LIST_HEIGHT
        );
        for (int index = 0; index < menu.getPetTypes().size(); index++) {
            int rowY = listY + index * PET_PANEL_ROW_HEIGHT - petScrollOffset;
            renderPetTypeRow(graphics, index, listX, rowY);
        }
        graphics.disableScissor();
        renderPetPanelScrollbar(graphics, listX, listY);
    }

    private void renderPetTypeRow(GuiGraphics graphics, int index, int x, int y) {
        int checkboxY = y + (PET_PANEL_ROW_HEIGHT - PET_PANEL_CHECKBOX_SIZE) / 2;
        renderPetCheckbox(graphics, menu.isPetTypeEnabled(index), x, checkboxY);
        graphics.renderItem(menu.getPetTypeFood(index), x + PET_PANEL_CHECKBOX_SIZE + 4, y + 1);
        graphics.drawString(
                font,
                menu.getPetTypeName(index),
                x + PET_PANEL_CHECKBOX_SIZE + 24,
                y + (PET_PANEL_ROW_HEIGHT - font.lineHeight) / 2,
                0xFF000000,
                false
        );
    }

    private void renderPetCheckbox(GuiGraphics graphics, boolean selected, int x, int y) {
        graphics.fill(x, y, x + PET_PANEL_CHECKBOX_SIZE, y + PET_PANEL_CHECKBOX_SIZE, 0xFF000000);
        graphics.fill(x + 1, y + 1, x + PET_PANEL_CHECKBOX_SIZE - 1, y + PET_PANEL_CHECKBOX_SIZE - 1, 0xFFFFFFFF);
        if (selected) {
            graphics.fill(x + 3, y + 3, x + PET_PANEL_CHECKBOX_SIZE - 3, y + PET_PANEL_CHECKBOX_SIZE - 3, 0xFF000000);
        }
    }

    private void renderPetPanelScrollbar(GuiGraphics graphics, int listX, int listY) {
        int maxScrollOffset = getMaxPetScrollOffset();
        if (maxScrollOffset <= 0) {
            return;
        }
        int scrollbarX = listX + PET_PANEL_LIST_WIDTH - PET_PANEL_SCROLLBAR_WIDTH;
        graphics.fill(
                scrollbarX,
                listY,
                scrollbarX + PET_PANEL_SCROLLBAR_WIDTH,
                listY + PET_PANEL_LIST_HEIGHT,
                0x66000000
        );
        int thumbHeight = Math.max(12, PET_PANEL_LIST_HEIGHT * PET_PANEL_LIST_HEIGHT / getPetPanelContentHeight());
        int thumbY = listY + (PET_PANEL_LIST_HEIGHT - thumbHeight) * petScrollOffset / maxScrollOffset;
        graphics.fill(
                scrollbarX,
                thumbY,
                scrollbarX + PET_PANEL_SCROLLBAR_WIDTH,
                thumbY + thumbHeight,
                0xFF404040
        );
    }

    private boolean clickPetPanel(double mouseX, double mouseY) {
        if (isInPetPanelAll(mouseX, mouseY)) {
            send(menu.petTypesAllButtonId());
            return true;
        }
        if (!isInPetPanelList(mouseX, mouseY)) {
            return false;
        }
        int row = ((int)mouseY - topPos - PET_PANEL_LIST_Y + petScrollOffset) / PET_PANEL_ROW_HEIGHT;
        if (row >= 0 && row < menu.getPetTypes().size()) {
            int foodX = getPetPanelX() + PET_PANEL_LIST_X + PET_PANEL_CHECKBOX_SIZE + 4;
            int rowY = topPos + PET_PANEL_LIST_Y + row * PET_PANEL_ROW_HEIGHT - petScrollOffset;
            send(mouseX >= foodX && mouseX < foodX + 16 && mouseY >= rowY + 1 && mouseY < rowY + 17
                    ? menu.petTypeFoodButtonId(row)
                    : menu.petTypeButtonId(row));
            return true;
        }
        return false;
    }

    private boolean isInPetPanelList(double mouseX, double mouseY) {
        int panelX = getPetPanelX();
        return mouseX >= panelX + PET_PANEL_LIST_X &&
                mouseX < panelX + PET_PANEL_LIST_X + PET_PANEL_LIST_WIDTH &&
                mouseY >= topPos + PET_PANEL_LIST_Y &&
                mouseY < topPos + PET_PANEL_LIST_Y + PET_PANEL_LIST_HEIGHT;
    }

    private boolean isInPetPanelAll(double mouseX, double mouseY) {
        int panelX = getPetPanelX();
        return mouseX >= panelX + PET_PANEL_LIST_X &&
                mouseX < panelX + PET_PANEL_LIST_X + PET_PANEL_LIST_WIDTH &&
                mouseY >= topPos + PET_PANEL_ALL_Y &&
                mouseY < topPos + PET_PANEL_ALL_Y + PET_PANEL_ROW_HEIGHT;
    }

    private int getPetPanelX() {
        return leftPos + imageWidth + PET_PANEL_GAP;
    }

    private int getMaxPetScrollOffset() {
        return Math.max(0, getPetPanelContentHeight() - PET_PANEL_LIST_HEIGHT);
    }

    private int getPetPanelContentHeight() {
        return menu.getPetTypes().size() * PET_PANEL_ROW_HEIGHT;
    }

    private void send(int id) {
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }

    private static Identifier texture(String name) {
        return Identifier.fromNamespaceAndPath(
                Customers.MODID,
                "textures/gui/" + name + ".png"
        );
    }

    private static Identifier modeTexture(CustomerSpawnerMode mode) {
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
        public void onPress(InputWithModifiers input) {
            CustomerSpawnerMode[] modes = CustomerSpawnerMode.values();
            setMode(modes[(mode.ordinal() + 1) % modes.length]);
            send(menu.modeButtonId(mode));
        }

        @Override
        protected void renderContents(
                GuiGraphics graphics,
                int mouseX,
                int mouseY,
                float partialTick
        ) {
            GuiGraphicsCUtils.blit(
                    graphics,
                    modeTexture(mode),
                    getX(),
                    getY(),
                    0,
                    0,
                    16,
                    16,
                    16,
                    16
            );
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narration) {}
    }

    private class AppearanceCheckbox extends AbstractButton {
        private final int appearanceIndex;

        private AppearanceCheckbox(int x, int y, int appearanceIndex) {
            super(x, y, PET_PANEL_CHECKBOX_SIZE, PET_PANEL_CHECKBOX_SIZE, Component.empty());
            this.appearanceIndex = appearanceIndex;
        }

        @Override
        public void onPress(InputWithModifiers input) {
            send(menu.appearanceButtonId(appearanceIndex));
        }

        @Override
        protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            renderPetCheckbox(graphics, menu.isAppearanceEnabled(appearanceIndex), getX(), getY());
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narration) {}
    }

    private class TextureButton extends AbstractButton {
        private final Identifier texture;
        private final Identifier pressedTexture;
        private final int buttonId;

        private TextureButton(
                int x,
                int y,
                Identifier texture,
                Identifier pressedTexture,
                int buttonId
        ) {
            super(x, y, 12, 12, Component.empty());
            this.texture = texture;
            this.pressedTexture = pressedTexture;
            this.buttonId = buttonId;
        }

        @Override
        public void onPress(InputWithModifiers input) {
            send(buttonId);
        }

        @Override
        protected void renderContents(
                GuiGraphics graphics,
                int mouseX,
                int mouseY,
                float partialTick
        ) {
            Identifier buttonTexture = isHoveredOrFocused()
                    ? pressedTexture
                    : texture;
            GuiGraphicsCUtils.blit(
                    graphics,
                    buttonTexture,
                    getX(),
                    getY(),
                    0,
                    0,
                    12,
                    12,
                    12,
                    12
            );
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narration) {}
    }

    private class PetsButton extends AbstractButton {
        private PetsButton(int x, int y) {
            super(
                    x,
                    y,
                    font.width(Component.translatable("screen.customers.customer_spawner.pets")),
                    font.lineHeight + 2,
                    Component.translatable("screen.customers.customer_spawner.pets")
            );
        }

        @Override
        public void onPress(InputWithModifiers input) {
            petPanelOpen = !petPanelOpen;
            petScrollOffset = 0;
            rebuildWidgets();
        }

        @Override
        protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.drawString(font, getMessage(), getX(), getY() + 1, 0xFF404040, false);
            graphics.fill(getX(), getY() + font.lineHeight + 1, getX() + getWidth(), getY() + font.lineHeight + 2, 0xFF404040);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }
}
