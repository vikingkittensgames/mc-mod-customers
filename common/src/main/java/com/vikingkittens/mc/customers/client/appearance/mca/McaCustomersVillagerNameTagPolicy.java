package com.vikingkittens.mc.customers.client.appearance.mca;

final class McaCustomersVillagerNameTagPolicy {
    private McaCustomersVillagerNameTagPolicy() {}

    static boolean shouldRender(
            boolean hasCustomName,
            boolean editorOpen,
            boolean playerPresent,
            boolean showNameTags,
            double distanceSquared,
            float nameTagDistance,
            boolean invisibleToPlayer
    ) {
        return hasCustomName
                && !editorOpen
                && playerPresent
                && showNameTags
                && distanceSquared
                        < nameTagDistance * nameTagDistance
                && !invisibleToPlayer;
    }
}
