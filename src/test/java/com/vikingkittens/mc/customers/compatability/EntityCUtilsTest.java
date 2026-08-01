package com.vikingkittens.mc.customers.compatability;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
class EntityCUtilsTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }
    @Test
    void createsEntity() {
        EntityType<Entity> entityType = mock(EntityType.class);
        Level level = mock(Level.class);
        Entity entity = mock(Entity.class);
        when(entityType.create(level))
                .thenReturn(entity);

        assertSame(entity, EntityCUtils.create(entityType, level));
    }
    @Test
    void snapsEntityToPrecisePosition() {
        Entity entity = mock(Entity.class);
        Vec3 position = new Vec3(1.25D, 64.5D, -3.75D);

        EntityCUtils.snapTo(entity, position, 90.0F, 15.0F);

        verify(entity).moveTo(
                position.x,
                position.y,
                position.z,
                90.0F,
                15.0F
        );
    }
    @Test
    void snapsEntityToBlockPosition() {
        Entity entity = mock(Entity.class);
        BlockPos position = new BlockPos(1, 64, -3);

        EntityCUtils.snapTo(entity, position, 90.0F, 15.0F);

        verify(entity).moveTo(position, 90.0F, 15.0F);
    }
    @Test
    void mountsEntityAndSendsGameEvent() {
        Entity passenger = mock(Entity.class);
        Entity vehicle = mock(Entity.class);
        when(passenger.startRiding(vehicle, true)).thenReturn(true);

        assertTrue(EntityCUtils.startRiding(passenger, vehicle, true));
        verify(passenger).startRiding(vehicle, true);
    }
}
