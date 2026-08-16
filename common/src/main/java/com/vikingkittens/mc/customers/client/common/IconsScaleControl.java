package com.vikingkittens.mc.customers.client.common;

import java.util.function.Consumer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class IconsScaleControl extends AbstractWidget {
    private final ResourceLocation fullIcon;
    private final ResourceLocation halfIcon;
    private final ResourceLocation emptyIcon;
    private final int iconWidth;
    private final int iconHeight;
    private final int iconCount;
    private final float minimum;
    private final float maximum;
    private final float increment;
    private final Consumer<Float> valueChangeListener;
    private float value;

    public IconsScaleControl(
            int x,
            int y,
            ResourceLocation fullIcon,
            ResourceLocation halfIcon,
            ResourceLocation emptyIcon,
            int iconWidth,
            int iconHeight,
            int iconCount,
            float minimum,
            float maximum,
            float increment,
            float initialValue,
            Consumer<Float> valueChangeListener
    ) {
        super(x, y, iconWidth * iconCount, iconHeight, Component.empty());
        this.fullIcon = fullIcon;
        this.halfIcon = halfIcon;
        this.emptyIcon = emptyIcon;
        this.iconWidth = iconWidth;
        this.iconHeight = iconHeight;
        this.iconCount = iconCount;
        this.minimum = minimum;
        this.maximum = maximum;
        this.increment = increment;
        this.valueChangeListener = valueChangeListener;
        value = clamp(initialValue);
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = clamp(value);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        int iconIndex = Mth.clamp(
                ((int)mouseX - getX()) / iconWidth,
                0,
                iconCount - 1
        );
        int xWithinIcon = ((int)mouseX - getX()) % iconWidth;
        setValue(iconIndex + (xWithinIcon < iconWidth / 2 ? increment : 1.0F));
        valueChangeListener.accept(value);
    }

    @Override
    protected void renderWidget(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        for (int iconIndex = 0; iconIndex < iconCount; iconIndex++) {
            float remaining = value - iconIndex;
            ResourceLocation icon = remaining >= 1.0F
                    ? fullIcon
                    : remaining >= increment ? halfIcon : emptyIcon;
            graphics.blit(
                    icon,
                    getX() + iconIndex * iconWidth,
                    getY(),
                    0,
                    0,
                    iconWidth,
                    iconHeight,
                    iconWidth,
                    iconHeight
            );
        }
    }

    private float clamp(float value) {
        return Mth.clamp(
                Math.round(value / increment) * increment,
                minimum,
                maximum
        );
    }

    @Override
    protected void updateWidgetNarration(
            NarrationElementOutput narration
    ) {}
}
