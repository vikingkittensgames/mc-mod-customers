package com.vikingkittens.mc.customers.client.appearance.mca;

import java.lang.ref.WeakReference;

import net.conczin.mca.Config;
import net.conczin.mca.client.gui.VillagerEditorScreen;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.Genetics;
import net.conczin.mca.entity.ai.relationship.Gender;
import net.conczin.mca.registry.EntitiesMCA;
import net.conczin.mca.resources.ClothingList;
import net.conczin.mca.resources.HairList;
import net.conczin.mca.resources.WeightedPool;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

import com.vikingkittens.mc.customers.appearance.CustomersVillager;
import com.vikingkittens.mc.customers.appearance.mca.McaCustomersVillagerVariation;
import com.vikingkittens.mc.customers.client.appearance.CustomersVillagerRenderProxy;

final class McaCustomersVillagerProxy extends VillagerEntityMCA
        implements CustomersVillagerRenderProxy {
    private WeakReference<CustomersVillager> source =
            new WeakReference<>(null);
    private WeakReference<Mob> sourceEntity =
            new WeakReference<>(null);
    private int sourceTick = Integer.MIN_VALUE;

    McaCustomersVillagerProxy(
            Level level,
            McaCustomersVillagerVariation variation
    ) {
        super(
                variation.feminine()
                        ? EntitiesMCA.FEMALE_VILLAGER
                        : EntitiesMCA.MALE_VILLAGER,
                level,
                variation.feminine()
                        ? Gender.FEMALE
                        : Gender.MALE
        );
        setAge(McaCustomersVillagerProxyState.adultAge());
        applyVariation(variation);
    }

    void syncFrom(Mob entity, CustomersVillager villager) {
        source = new WeakReference<>(villager);
        sourceEntity = new WeakReference<>(entity);
        setPos(entity.getX(), entity.getY(), entity.getZ());
        setYRot(entity.getYRot());
        setXRot(entity.getXRot());
        yRotO = entity.yRotO;
        xRotO = entity.xRotO;
        yBodyRot = entity.yBodyRot;
        yBodyRotO = entity.yBodyRotO;
        yHeadRot = entity.yHeadRot;
        yHeadRotO = entity.yHeadRotO;
        tickCount = entity.tickCount;
        hurtTime = entity.hurtTime;
        deathTime = entity.deathTime;
        swinging = entity.swinging;
        swingingArm = entity.swingingArm;
        swingTime = entity.swingTime;
        oAttackAnim = entity.oAttackAnim;
        attackAnim = entity.attackAnim;
        if (sourceTick != entity.tickCount) {
            sourceTick = entity.tickCount;
            walkAnimation.update(
                    entity.walkAnimation.speed(),
                    1.0F
            );
        }
        setPose(entity.getPose());
        setDeltaMovement(entity.getDeltaMovement());
        setCustomName(entity.getCustomName());
        setCustomNameVisible(entity.isCustomNameVisible());
        setInvisible(entity.isInvisible());
        setGlowingTag(entity.isCurrentlyGlowing());

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            setItemSlot(slot, entity.getItemBySlot(slot).copy());
        }
    }

    @Override
    public boolean isPassenger() {
        return McaCustomersVillagerProxyState.isPassenger(source);
    }

    @Override
    public @Nullable Entity getVehicle() {
        return McaCustomersVillagerProxyState.vehicle(sourceEntity);
    }

    @Override
    public @Nullable CustomersVillager getCustomersVillagerSource() {
        return source == null ? null : source.get();
    }

    @Override
    public boolean shouldRenderNameTag() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        Config config = Config.getInstance();
        return McaCustomersVillagerNameTagPolicy.shouldRender(
                getCustomName() != null,
                minecraft.screen instanceof VillagerEditorScreen,
                player != null,
                config.showNameTags,
                player == null
                        ? Double.POSITIVE_INFINITY
                        : player.distanceToSqr(this),
                config.nameTagDistance,
                player != null && isInvisibleTo(player)
        );
    }

    private void applyVariation(
            McaCustomersVillagerVariation variation
    ) {
        getRandom().setSeed(variation.randomSeed());
        getGenetics().setGender(
                variation.feminine()
                        ? Gender.FEMALE
                        : Gender.MALE
        );
        getGenetics().setGene(Genetics.SIZE, variation.size());
        getGenetics().setGene(Genetics.WIDTH, variation.width());
        getGenetics().setGene(Genetics.BREAST, variation.breast());
        getGenetics().setGene(Genetics.MELANIN, variation.melanin());
        getGenetics().setGene(
                Genetics.HEMOGLOBIN,
                variation.hemoglobin()
        );
        getGenetics().setGene(
                Genetics.EUMELANIN,
                variation.eumelanin()
        );
        getGenetics().setGene(
                Genetics.PHEOMELANIN,
                variation.pheomelanin()
        );
        getGenetics().setGene(Genetics.SKIN, variation.skin());
        getGenetics().setGene(Genetics.FACE, variation.face());
        getGenetics().setGene(Genetics.VOICE, variation.voice());
        getGenetics().setGene(
                Genetics.VOICE_TONE,
                variation.voiceTone()
        );

        ClothingList clothing = ClothingList.getInstance();
        if (clothing != null) {
            WeightedPool.Entry<String> selected =
                    McaCustomersVillagerWeightedSelector.select(
                            clothing.getPool(this).getEntries(),
                            WeightedPool.Entry::getWeight,
                            variation.clothingChoice(),
                            null
                    );
            if (selected != null) {
                setClothes(selected.getValue());
            }
        }

        HairList hair = HairList.getInstance();
        if (hair != null) {
            WeightedPool.Entry<String> selected =
                    McaCustomersVillagerWeightedSelector.select(
                            hair.getPool(getGenetics().getGender())
                                    .getEntries(),
                            WeightedPool.Entry::getWeight,
                            variation.hairChoice(),
                            null
                    );
            if (selected != null) {
                setHair(selected.getValue());
            }
        }
    }
}
