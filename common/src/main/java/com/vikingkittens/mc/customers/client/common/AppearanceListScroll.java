package com.vikingkittens.mc.customers.client.common;

final class AppearanceListScroll {
    private AppearanceListScroll() {}

    static int maximumOffset(int entryCount, int rowHeight, int viewportHeight) {
        return Math.max(0, entryCount * rowHeight - viewportHeight);
    }

    static int afterWheel(
            int currentOffset,
            double scrollAmount,
            int entryCount,
            int rowHeight,
            int viewportHeight
    ) {
        int maximumOffset = maximumOffset(entryCount, rowHeight, viewportHeight);
        int offset = currentOffset - (int)Math.signum(scrollAmount) * rowHeight;
        return Math.max(0, Math.min(offset, maximumOffset));
    }
}
