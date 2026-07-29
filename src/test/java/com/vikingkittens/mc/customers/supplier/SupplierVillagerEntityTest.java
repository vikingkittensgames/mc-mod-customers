package com.vikingkittens.mc.customers.supplier;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.common.MobUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.MockedStatic;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SupplierVillagerEntityTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void ignoresFallDamage() {
        SupplierVillagerEntity supplier = mock(
                SupplierVillagerEntity.class,
                CALLS_REAL_METHODS
        );

        assertFalse(supplier.causeFallDamage(
                100.0F,
                1.0F,
                mock(DamageSource.class)
        ));
    }

    @Test
    void acceptsCandidateWithReachablePathToSpawner() {
        Level level = mock(Level.class);
        SupplierVillagerEntity supplier = mock(SupplierVillagerEntity.class);
        PathNavigation navigation = mock(PathNavigation.class);
        Path path = mock(Path.class);
        BlockPos spawnerPos = new BlockPos(10, 54, 20);
        BlockPos navigationTarget = new BlockPos(10, 64, 20);
        BlockPos candidatePos = new BlockPos(50, 65, 40);

        when(supplier.getNavigation()).thenReturn(navigation);
        when(navigation.createPath(navigationTarget, 0)).thenReturn(path);
        when(path.canReach()).thenReturn(true);

        try (MockedStatic<MobUtils> mobUtils = mockStatic(MobUtils.class)) {
            mockCandidateValidation(
                    mobUtils,
                    level,
                    spawnerPos,
                    64,
                    candidatePos
            );

            assertEquals(
                    candidatePos,
                    SupplierVillagerEntity.findReachableSpawnPos(
                            level,
                            supplier,
                            spawnerPos,
                            navigationTarget
                    )
            );
        }

        InOrder validationOrder = inOrder(supplier, navigation);
        validationOrder.verify(supplier).moveTo(candidatePos, 0, 0);
        validationOrder.verify(supplier).setOnGround(true);
        validationOrder.verify(navigation).createPath(navigationTarget, 0);
    }

    @Test
    void rejectsCandidateWhenPathCannotReachSpawner() {
        Level level = mock(Level.class);
        SupplierVillagerEntity supplier = mock(SupplierVillagerEntity.class);
        PathNavigation navigation = mock(PathNavigation.class);
        Path path = mock(Path.class);
        BlockPos spawnerPos = new BlockPos(10, 54, 20);
        BlockPos navigationTarget = new BlockPos(10, 64, 20);
        BlockPos candidatePos = new BlockPos(50, 65, 40);

        when(supplier.getNavigation()).thenReturn(navigation);
        when(navigation.createPath(navigationTarget, 0)).thenReturn(path);
        when(path.canReach()).thenReturn(false);

        try (MockedStatic<MobUtils> mobUtils = mockStatic(MobUtils.class)) {
            mockCandidateValidation(
                    mobUtils,
                    level,
                    spawnerPos,
                    64,
                    candidatePos
            );

            assertNull(SupplierVillagerEntity.findReachableSpawnPos(
                    level,
                    supplier,
                    spawnerPos,
                    navigationTarget
            ));
        }
    }

    @Test
    void rejectsCandidateWhenNavigationCannotCreatePath() {
        Level level = mock(Level.class);
        SupplierVillagerEntity supplier = mock(SupplierVillagerEntity.class);
        PathNavigation navigation = mock(PathNavigation.class);
        BlockPos spawnerPos = new BlockPos(10, 64, 20);
        BlockPos navigationTarget = new BlockPos(10, 70, 20);
        BlockPos candidatePos = new BlockPos(50, 65, 40);

        when(supplier.getNavigation()).thenReturn(navigation);
        when(navigation.createPath(navigationTarget, 0)).thenReturn(null);

        try (MockedStatic<MobUtils> mobUtils = mockStatic(MobUtils.class)) {
            mockCandidateValidation(
                    mobUtils,
                    level,
                    spawnerPos,
                    64,
                    candidatePos
            );

            assertNull(SupplierVillagerEntity.findReachableSpawnPos(
                    level,
                    supplier,
                    spawnerPos,
                    navigationTarget
            ));
        }
    }

    @Test
    void reducesRadiusByFourUntilPositionIsFound() {
        Level level = mock(Level.class);
        SupplierVillagerEntity supplier = mock(SupplierVillagerEntity.class);
        BlockPos spawnerPos = new BlockPos(10, 64, 20);
        BlockPos navigationTarget = new BlockPos(10, 70, 20);
        BlockPos expected = new BlockPos(55, 65, 40);

        try (MockedStatic<MobUtils> mobUtils = mockStatic(MobUtils.class)) {
            mobUtils.when(() -> MobUtils.getRandomSpawnPos(
                    eq(level),
                    eq(spawnerPos),
                    eq(64),
                    eq(3),
                    eq(64),
                    any()
            )).thenReturn(null);
            mobUtils.when(() -> MobUtils.getRandomSpawnPos(
                    eq(level),
                    eq(spawnerPos),
                    eq(60),
                    eq(3),
                    eq(64),
                    any()
            )).thenReturn(null);
            mobUtils.when(() -> MobUtils.getRandomSpawnPos(
                    eq(level),
                    eq(spawnerPos),
                    eq(56),
                    eq(3),
                    eq(64),
                    any()
            )).thenReturn(expected);

            assertEquals(
                    expected,
                    SupplierVillagerEntity.findReachableSpawnPos(
                            level,
                            supplier,
                            spawnerPos,
                            navigationTarget
                    )
            );

            mobUtils.verify(() -> MobUtils.getRandomSpawnPos(
                    eq(level),
                    eq(spawnerPos),
                    eq(52),
                    eq(3),
                    eq(64),
                    any()
            ), never());
        }
    }

    @SuppressWarnings("unchecked")
    private static void mockCandidateValidation(
            MockedStatic<MobUtils> mobUtils,
            Level level,
            BlockPos spawnerPos,
            int radius,
            BlockPos candidatePos
    ) {
        mobUtils.when(() -> MobUtils.getRandomSpawnPos(
                eq(level),
                eq(spawnerPos),
                eq(radius),
                eq(3),
                eq(64),
                any()
        )).thenAnswer(invocation -> {
            Predicate<BlockPos> isValid = invocation.getArgument(5);
            return isValid.test(candidatePos) ? candidatePos : null;
        });
    }
}
