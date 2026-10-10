package com.vikingkittens.mc.customers.client.common;

import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public final class AppearanceListWidget extends AbstractWidget {
    private static final int ROW_HEIGHT = 20;
    private static final int CHECKBOX_SIZE = 10;
    private static final int SCROLLBAR_WIDTH = 4;
    private static final int TEXT_COLOR = 0x000000;

    private final Font font;
    private final List<MultiLineLabel> labels;
    private final IntPredicate enabled;
    private final IntConsumer toggle;
    private int scrollOffset;

    public AppearanceListWidget(
            Font font,
            int x,
            int y,
            int width,
            int height,
            Component narration,
            List<Component> appearanceNames,
            IntPredicate enabled,
            IntConsumer toggle
    ) {
        super(x, y, width, height, narration);
        this.font = font;
        labels = appearanceNames.stream()
                .map(name -> MultiLineLabel.create(font, name, width - CHECKBOX_SIZE - SCROLLBAR_WIDTH - 8))
                .toList();
        this.enabled = enabled;
        this.toggle = toggle;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!isMouseOver(mouseX, mouseY) || getMaximumScrollOffset() == 0) {
            return false;
        }
        scrollOffset = AppearanceListScroll.afterWheel(
                scrollOffset,
                scrollY,
                labels.size(),
                ROW_HEIGHT,
                getHeight()
        );
        return true;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        int index = ((int)mouseY - getY() + scrollOffset) / ROW_HEIGHT;
        if (index >= 0 && index < labels.size()) {
            toggle.accept(index);
        }
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.enableScissor(getX(), getY(), getX() + getWidth(), getY() + getHeight());
        for (int index = 0; index < labels.size(); index++) {
            int rowY = getY() + index * ROW_HEIGHT - scrollOffset;
            if (rowY + ROW_HEIGHT > getY() && rowY < getY() + getHeight()) {
                renderEntry(graphics, index, rowY);
            }
        }
        graphics.disableScissor();
        renderScrollbar(graphics);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        defaultButtonNarrationText(narration);
    }

    private void renderEntry(GuiGraphics graphics, int index, int rowY) {
        int checkboxY = rowY + (ROW_HEIGHT - CHECKBOX_SIZE) / 2;
        graphics.fill(getX(), checkboxY, getX() + CHECKBOX_SIZE, checkboxY + CHECKBOX_SIZE, 0xFF000000);
        graphics.fill(getX() + 1, checkboxY + 1, getX() + CHECKBOX_SIZE - 1, checkboxY + CHECKBOX_SIZE - 1, 0xFFFFFFFF);
        if (enabled.test(index)) {
            graphics.fill(getX() + 3, checkboxY + 3, getX() + CHECKBOX_SIZE - 3, checkboxY + CHECKBOX_SIZE - 3, 0xFF000000);
        }
        MultiLineLabel label = labels.get(index);
        int labelHeight = label.getLineCount() * font.lineHeight;
        label.renderLeftAlignedNoShadow(
                graphics,
                getX() + CHECKBOX_SIZE + 4,
                rowY + (ROW_HEIGHT - labelHeight) / 2,
                font.lineHeight,
                TEXT_COLOR
        );
    }

    private void renderScrollbar(GuiGraphics graphics) {
        int maximumScrollOffset = getMaximumScrollOffset();
        if (maximumScrollOffset == 0) {
            return;
        }
        int scrollbarX = getX() + getWidth() - SCROLLBAR_WIDTH;
        graphics.fill(scrollbarX, getY(), scrollbarX + SCROLLBAR_WIDTH, getY() + getHeight(), 0x66000000);
        int contentHeight = labels.size() * ROW_HEIGHT;
        int thumbHeight = Math.max(12, getHeight() * getHeight() / contentHeight);
        int thumbY = getY() + (getHeight() - thumbHeight) * scrollOffset / maximumScrollOffset;
        graphics.fill(scrollbarX, thumbY, scrollbarX + SCROLLBAR_WIDTH, thumbY + thumbHeight, 0xFF404040);
    }

    private int getMaximumScrollOffset() {
        return AppearanceListScroll.maximumOffset(labels.size(), ROW_HEIGHT, getHeight());
    }
}
