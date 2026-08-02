package com.vikingkittens.mc.customers.client.customer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.BossEvent;
import net.minecraft.world.item.ItemStack;

import com.vikingkittens.mc.customers.client.compatability.BossBarCUtils;
import com.vikingkittens.mc.customers.client.compatability.GuiGraphicsCUtils;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerSnapshot;

/**
 * Renders customer request details with the customer spawner boss bar.
 */
public final class CustomerBossBarRenderer {
    private static final double PLAYER_VIEW_RANGE = 64.0D;
    private static final int BAR_WIDTH = 182;
    private static final int BAR_GAP = 3;
    private static final int SCREEN_HORIZONTAL_MARGIN = 20;
    private static final int ICON_PADDING = 2;
    private static final int ICON_SIZE = 12;
    private static final float ICON_SCALE = 0.75F;

    private CustomerBossBarRenderer() {
    }

    /**
     * Determines whether a player is within the customer spawner's display range.
     *
     * @param spawnerPos customer spawner position
     * @param playerX player x coordinate
     * @param playerY player y coordinate
     * @param playerZ player z coordinate
     * @return whether the player is within the display range
     */
    public static boolean isInRange(
            BlockPos spawnerPos,
            double playerX,
            double playerY,
            double playerZ
    ) {
        return spawnerPos.distToCenterSqr(playerX, playerY, playerZ)
                <= PLAYER_VIEW_RANGE * PLAYER_VIEW_RANGE;
    }

    /**
     * Calculates the vertical increment needed before rendering another boss bar.
     *
     * @param vanillaIncrement normal boss bar increment
     * @param layoutHeight height of the customer item rows
     * @return adjusted vertical increment
     */
    public static int calculateIncrement(int vanillaIncrement, int layoutHeight) {
        return layoutHeight == 0
                ? vanillaIncrement
                : vanillaIncrement + layoutHeight + BAR_GAP;
    }

    /**
     * Calculates the available width for customer item groups.
     *
     * @param guiWidth current scaled GUI width
     * @return available layout width with screen-edge margins
     */
    public static int calculateLayoutWidth(int guiWidth) {
        return Math.max(1, guiWidth - SCREEN_HORIZONTAL_MARGIN);
    }

    /**
     * Renders the title, grouped customer requests, and relocated boss bar.
     *
     * @param graphics GUI rendering context
     * @param bossEvent boss bar state
     * @param snapshot synchronized customer spawner state
     * @param x vanilla boss bar left coordinate
     * @param y vanilla boss bar top coordinate
     * @param vanillaIncrement normal boss bar vertical increment
     * @return adjusted vertical increment for the next boss bar
     */
    public static int render(
            GuiGraphics graphics,
            BossEvent bossEvent,
            CustomerSpawnerSnapshot snapshot,
            int x,
            int y,
            int vanillaIncrement
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        CustomerBossBarLayout.Layout layout = CustomerBossBarLayout.create(
                snapshot.customers(),
                x + BAR_WIDTH / 2,
                y,
                calculateLayoutWidth(graphics.guiWidth())
        );

        int titleX = x + (BAR_WIDTH - minecraft.font.width(bossEvent.getName())) / 2;
        graphics.drawString(
                minecraft.font,
                bossEvent.getName(),
                titleX,
                y - 9,
                0xFFFFFF
        );

        for (CustomerBossBarLayout.Group group : layout.groups()) {
            CustomerBossBarLayout.Bounds bounds = group.bounds();
            graphics.fill(
                    bounds.x(),
                    bounds.y(),
                    bounds.x() + bounds.width(),
                    bounds.y() + bounds.height(),
                    CustomerBossBarLayout.backgroundColor(group.customer().type())
            );

            int itemX = bounds.x() + ICON_PADDING;
            int itemY = bounds.y() + ICON_PADDING;
            for (ItemStack stack : group.customer().offerCostItems()) {
                GuiGraphicsCUtils.renderItem(
                        graphics,
                        stack,
                        itemX,
                        itemY,
                        ICON_SCALE
                );
                itemX += ICON_SIZE;
            }
        }

        int barY = layout.height() == 0
                ? y
                : y + layout.height() + BAR_GAP;
        BossBarCUtils.render(graphics, x, barY, bossEvent);
        return calculateIncrement(vanillaIncrement, layout.height());
    }

    /**
     * Determines whether the local player can see a spawner's boss bar.
     *
     * @param player local player
     * @param spawnerPos customer spawner position
     * @return whether the player is within the display range
     */
    public static boolean isInRange(LocalPlayer player, BlockPos spawnerPos) {
        return isInRange(
                spawnerPos,
                player.getX(),
                player.getY(),
                player.getZ()
        );
    }
}
