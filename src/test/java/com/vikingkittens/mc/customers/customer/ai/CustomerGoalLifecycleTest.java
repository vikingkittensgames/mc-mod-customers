package com.vikingkittens.mc.customers.customer.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.customer.CustomerCounter;
import com.vikingkittens.mc.customers.customer.CustomerSeatEntity;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerBlockEntity;
import com.vikingkittens.mc.customers.customer.CustomerState;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerGoalLifecycleTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void leaderMovesToCounterAndStartsBuying() {
        GoalFixture fixture = new GoalFixture(CustomerState.INITIALIZING);

        try (
                MockedStatic<CustomerCounter> counter =
                        fixture.mockCounterSearch();
                MockedStatic<CustomerSeatEntity> seat =
                        mockStatic(CustomerSeatEntity.class)
        ) {
            fixture.tick();

            assertEquals(CustomerState.MOVING_TO_COUNTER, fixture.state.get());
            verify(fixture.navigation, atLeastOnce()).moveTo(
                    10.5,
                    65.0,
                    0.5,
                    0.5
            );

            fixture.moveCustomerTo(fixture.targetPosition);
            fixture.tick();

            assertEquals(CustomerState.BUYING, fixture.state.get());
            seat.verify(() -> CustomerSeatEntity.trySit(
                    fixture.level,
                    fixture.targetPosition.below(),
                    fixture.customer
            ));
            assertEquals(
                    List.of(
                            CustomerState.MOVING_TO_COUNTER,
                            CustomerState.BUYING
                    ),
                    fixture.stateHistory
            );
        }
    }

    @Test
    void queuedCustomerLinesUpThenMovesToCounterWhenItBecomesLeader() {
        GoalFixture fixture = new GoalFixture(CustomerState.INITIALIZING);
        fixture.followingCustomerId.set(fixture.leaderId);

        try (
                MockedStatic<CustomerCounter> counter =
                        fixture.mockCounterSearch();
                MockedStatic<CustomerSeatEntity> seat =
                        mockStatic(CustomerSeatEntity.class)
        ) {
            fixture.tick();
            assertEquals(CustomerState.LINING_UP, fixture.state.get());

            fixture.tick();
            assertEquals(CustomerState.IN_LINE, fixture.state.get());
            verify(fixture.navigation, atLeastOnce()).moveTo(
                    9.5,
                    65.0,
                    0.5,
                    0.5
            );

            fixture.moveCustomerTo(new BlockPos(9, 64, 0));
            fixture.followingCustomerId.set(null);
            fixture.tick();
            assertEquals(CustomerState.WAITING_ON_LEADER, fixture.state.get());

            fixture.tick(2);

            assertEquals(CustomerState.BUYING, fixture.state.get());
            assertEquals(
                    List.of(
                            CustomerState.LINING_UP,
                            CustomerState.IN_LINE,
                            CustomerState.WAITING_ON_LEADER,
                            CustomerState.INITIALIZING,
                            CustomerState.MOVING_TO_COUNTER,
                            CustomerState.BUYING
                    ),
                    fixture.stateHistory
            );
            seat.verify(() -> CustomerSeatEntity.trySit(
                    fixture.level,
                    fixture.targetPosition.below(),
                    fixture.customer
            ));
        }
    }
    @Test
    void lineupGoalCanExecuteMultipleTimesForTheSameCustomer() {
        GoalFixture fixture = new GoalFixture(CustomerState.INITIALIZING);
        fixture.followingCustomerId.set(fixture.leaderId);

        try (
                MockedStatic<CustomerCounter> counter =
                        fixture.mockCounterSearch();
                MockedStatic<CustomerSeatEntity> seat =
                        mockStatic(CustomerSeatEntity.class)
        ) {
            fixture.tick(2);
            assertEquals(CustomerState.IN_LINE, fixture.state.get());

            fixture.moveCustomerTo(new BlockPos(9, 64, 0));
            fixture.tick();
            assertEquals(CustomerState.WAITING_ON_LEADER, fixture.state.get());

            fixture.tick(98);
            assertEquals(CustomerState.WAITING_ON_LEADER, fixture.state.get());

            fixture.tick();
            assertEquals(CustomerState.INITIALIZING, fixture.state.get());

            fixture.leaderPosition.set(new BlockPos(12, 64, 0));
            fixture.tick(2);

            assertEquals(CustomerState.IN_LINE, fixture.state.get());
            verify(fixture.navigation, atLeastOnce()).moveTo(
                    9.5,
                    65.0,
                    0.5,
                    0.5
            );
            verify(fixture.navigation, atLeastOnce()).moveTo(
                    11.5,
                    65.0,
                    0.5,
                    0.5
            );

            fixture.moveCustomerTo(new BlockPos(11, 64, 0));
            fixture.tick();
            assertEquals(CustomerState.WAITING_ON_LEADER, fixture.state.get());

            fixture.tick(99);
            assertEquals(CustomerState.INITIALIZING, fixture.state.get());

            assertEquals(2, fixture.stateCount(CustomerState.IN_LINE));
            assertEquals(2, fixture.stateCount(CustomerState.WAITING_ON_LEADER));
            assertEquals(2, fixture.stateCount(CustomerState.INITIALIZING));
            assertEquals(2, fixture.stateCount(CustomerState.LINING_UP));
            verify(fixture.navigation, atLeastOnce()).stop();
        }
    }
    @Test
    void inLineCustomerRestartsLineupAfterLosingNavigationPath() {
        GoalFixture fixture = new GoalFixture(CustomerState.IN_LINE);
        fixture.counterTargetPosition.set(fixture.targetPosition);
        fixture.followingCustomerId.set(fixture.leaderId);
        when(fixture.navigation.getPath()).thenReturn(null);

        try (
                MockedStatic<CustomerCounter> counter =
                        fixture.mockCounterSearch();
                MockedStatic<CustomerSeatEntity> seat =
                        mockStatic(CustomerSeatEntity.class)
        ) {
            fixture.tick();

            assertEquals(CustomerState.IN_LINE, fixture.state.get());
            verify(fixture.navigation, atLeastOnce()).moveTo(
                    9.5,
                    65.0,
                    0.5,
                    0.5
            );
            assertEquals(1, fixture.stateCount(CustomerState.IN_LINE));
        }
    }

    @Test
    void blockedHeadroomWaitsWithoutStartingLineNavigation() {
        GoalFixture fixture = new GoalFixture(CustomerState.INITIALIZING);
        fixture.followingCustomerId.set(fixture.leaderId);
        fixture.blockedPosition.set(new BlockPos(9, 65, 0));

        try (
                MockedStatic<CustomerCounter> counter =
                        fixture.mockCounterSearch();
                MockedStatic<CustomerSeatEntity> seat =
                        mockStatic(CustomerSeatEntity.class)
        ) {
            fixture.tick(2);

            assertEquals(CustomerState.WAITING_ON_LEADER, fixture.state.get());
            verify(fixture.navigation, never()).moveTo(
                    9.5,
                    65.0,
                    0.5,
                    0.5
            );
        }
    }

    @Test
    void waitingContinuesWhenFollowingCustomerJumps() {
        GoalFixture fixture = new GoalFixture(CustomerState.WAITING_ON_LEADER);
        fixture.counterTargetPosition.set(fixture.targetPosition);
        fixture.followingCustomerId.set(fixture.leaderId);

        try (
                MockedStatic<CustomerCounter> counter =
                        fixture.mockCounterSearch();
                MockedStatic<CustomerSeatEntity> seat =
                        mockStatic(CustomerSeatEntity.class)
        ) {
            fixture.tick();

            fixture.leaderPosition.set(
                    fixture.leaderPosition.get().above()
            );
            fixture.tick();

            assertEquals(
                    CustomerState.WAITING_ON_LEADER,
                    fixture.state.get()
            );
        }
    }
    @Test
    void waitingEndsEarlyWhenFollowingCustomerMovesOneBlock() {
        GoalFixture fixture = new GoalFixture(CustomerState.WAITING_ON_LEADER);
        fixture.counterTargetPosition.set(fixture.targetPosition);
        fixture.followingCustomerId.set(fixture.leaderId);

        try (
                MockedStatic<CustomerCounter> counter =
                        fixture.mockCounterSearch();
                MockedStatic<CustomerSeatEntity> seat =
                        mockStatic(CustomerSeatEntity.class)
        ) {
            fixture.tick();

            assertEquals(
                    CustomerState.WAITING_ON_LEADER,
                    fixture.state.get()
            );

            fixture.leaderPosition.set(new BlockPos(11, 64, 0));
            fixture.tick();

            assertEquals(CustomerState.LINING_UP, fixture.state.get());
            assertEquals(
                    List.of(
                            CustomerState.INITIALIZING,
                            CustomerState.LINING_UP
                    ),
                    fixture.stateHistory
            );
        }
    }
    @Test
    void missingFollowingCustomerReturnsToInitializingThenStartsCounterGoal() {
        GoalFixture fixture = new GoalFixture(CustomerState.LINING_UP);
        fixture.counterTargetPosition.set(fixture.targetPosition);

        try (
                MockedStatic<CustomerCounter> counter =
                        fixture.mockCounterSearch();
                MockedStatic<CustomerSeatEntity> seat =
                        mockStatic(CustomerSeatEntity.class)
        ) {
            fixture.tick();

            assertEquals(CustomerState.INITIALIZING, fixture.state.get());
            verify(fixture.navigation, never()).moveTo(
                    9.5,
                    65.0,
                    0.5,
                    0.5
            );

            fixture.tick();

            assertEquals(CustomerState.MOVING_TO_COUNTER, fixture.state.get());
            verify(fixture.navigation, atLeastOnce()).moveTo(
                    10.5,
                    65.0,
                    0.5,
                    0.5
            );
        }
    }
    @Test
    void legacyInLineCustomerWithoutSavedTargetSelectsANewCounterTarget() {
        GoalFixture fixture = new GoalFixture(CustomerState.IN_LINE);

        try (
                MockedStatic<CustomerCounter> counter =
                        fixture.mockCounterSearch();
                MockedStatic<CustomerSeatEntity> seat =
                        mockStatic(CustomerSeatEntity.class)
        ) {
            fixture.tick();

            assertEquals(
                    fixture.targetPosition,
                    fixture.counterTargetPosition.get()
            );
            assertEquals(CustomerState.MOVING_TO_COUNTER, fixture.state.get());
        }
    }

    @Test
    void legacyLiningUpCustomerWithoutSavedTargetSelectsANewCounterTarget() {
        GoalFixture fixture = new GoalFixture(CustomerState.LINING_UP);
        fixture.followingCustomerId.set(fixture.leaderId);

        try (
                MockedStatic<CustomerCounter> counter =
                        fixture.mockCounterSearch();
                MockedStatic<CustomerSeatEntity> seat =
                        mockStatic(CustomerSeatEntity.class)
        ) {
            fixture.tick();

            assertEquals(
                    fixture.targetPosition,
                    fixture.counterTargetPosition.get()
            );
            assertEquals(CustomerState.LINING_UP, fixture.state.get());
        }
    }


    @Test
    void selectsShorterReservedCounterLine() {
        GoalFixture fixture = new GoalFixture(CustomerState.INITIALIZING);
        BlockPos secondCounterPosition = new BlockPos(13, 64, 0);
        BlockPos secondTargetPosition = new BlockPos(12, 64, 0);
        UUID firstLineCustomerId = UUID.randomUUID();
        UUID secondLineCustomerId = UUID.randomUUID();
        UUID thirdLineCustomerId = UUID.randomUUID();

        when(fixture.spawner.getReservedTargetCounterPositions()).thenReturn(
                Map.of(
                        fixture.targetPosition,
                        List.of(firstLineCustomerId, secondLineCustomerId),
                        secondTargetPosition,
                        List.of(thirdLineCustomerId)
                )
        );
        when(fixture.spawner.tryReserveTargetCounterPosition(
                secondTargetPosition,
                fixture.customerId
        )).thenReturn(thirdLineCustomerId);

        try (
                MockedStatic<CustomerCounter> counter =
                        mockStatic(CustomerCounter.class);
                MockedStatic<CustomerSeatEntity> seat =
                        mockStatic(CustomerSeatEntity.class)
        ) {
            counter.when(() -> CustomerCounter.findCounterPositions(
                    fixture.level,
                    fixture.spawnerPosition,
                    fixture.counterState
            )).thenReturn(List.of(
                    fixture.counterPosition,
                    secondCounterPosition
            ));
            counter.when(() -> CustomerCounter.findValidSurroundingPositions(
                    fixture.level,
                    List.of(
                            fixture.counterPosition,
                            secondCounterPosition
                    ),
                    fixture.customer,
                    null
            )).thenReturn(new ArrayList<>(List.of(
                    new CustomerCounter.SurroundingPosition(
                            fixture.counterPosition,
                            fixture.targetPosition
                    ),
                    new CustomerCounter.SurroundingPosition(
                            secondCounterPosition,
                            secondTargetPosition
                    )
            )));

            fixture.tick();

            assertEquals(
                    secondTargetPosition,
                    fixture.counterTargetPosition.get()
            );
            assertEquals(CustomerState.LINING_UP, fixture.state.get());
        }
    }

    private static final class GoalFixture {
        private final UUID customerId = UUID.randomUUID();
        private final UUID leaderId = UUID.randomUUID();

        private final BlockPos spawnerPosition = new BlockPos(0, 64, 0);
        private final BlockPos counterPosition = new BlockPos(11, 64, 0);
        private final BlockPos targetPosition = new BlockPos(10, 64, 0);

        private final AtomicReference<CustomerState> state;
        private final AtomicReference<Vec3> customerPosition =
                new AtomicReference<>(new Vec3(0.5, 64, 0.5));
        private final AtomicReference<BlockPos> counterTargetPosition =
                new AtomicReference<>();
        private final AtomicReference<UUID> followingCustomerId =
                new AtomicReference<>();
        private final AtomicReference<BlockPos> leaderPosition =
                new AtomicReference<>(targetPosition);
        private final AtomicReference<BlockPos> blockedPosition =
                new AtomicReference<>();

        private final List<CustomerState> stateHistory = new ArrayList<>();

        private final ServerLevel level = mock(ServerLevel.class);
        private final CustomerVillagerEntity customer =
                mock(CustomerVillagerEntity.class);
        private final CustomerVillagerEntity leader =
                mock(CustomerVillagerEntity.class);
        private final CustomerSpawnerBlockEntity spawner =
                mock(CustomerSpawnerBlockEntity.class);
        private final PathNavigation navigation = mock(PathNavigation.class);
        private final RandomSource random = mock(RandomSource.class);
        private final BlockState counterState = mock(BlockState.class);
        private final BlockState airState = mock(BlockState.class);
        private final BlockState groundState = mock(BlockState.class);

        private final GoalSelector goalSelector = new GoalSelector();

        private GoalFixture(CustomerState initialState) {
            state = new AtomicReference<>(initialState);

            when(customer.isAlive()).thenReturn(true);
            when(customer.getUUID()).thenReturn(customerId);
            when(customer.getState()).thenAnswer(ignored -> state.get());
            doAnswer(invocation -> {
                CustomerState newState = invocation.getArgument(0);
                state.set(newState);
                stateHistory.add(newState);
                return null;
            }).when(customer).setState(any(CustomerState.class));

            when(customer.position()).thenAnswer(
                    ignored -> customerPosition.get()
            );
            when(customer.blockPosition()).thenAnswer(
                    ignored -> BlockPos.containing(customerPosition.get())
            );
            when(customer.level()).thenReturn(level);
            when(customer.getNavigation()).thenReturn(navigation);
            when(customer.getRandom()).thenReturn(random);
            when(customer.getSpawner()).thenReturn(spawner);
            when(customer.getSpawnerPos()).thenReturn(spawnerPosition);
            when(customer.getSpawnPos()).thenReturn(spawnerPosition);
            when(customer.getCounterBlockState()).thenReturn(counterState);
            when(customer.getAvoidBlockState()).thenReturn(null);
            when(customer.getCounterTargetBlockPos()).thenAnswer(
                    ignored -> counterTargetPosition.get()
            );
            doAnswer(invocation -> {
                counterTargetPosition.set(invocation.getArgument(0));
                return null;
            }).when(customer).setCounterTargetBlockPos(any());
            when(customer.getAttributeValue(Attributes.MOVEMENT_SPEED))
                    .thenReturn(0.5);

            when(counterState.isAir()).thenReturn(false);
            when(airState.isAir()).thenReturn(true);
            when(groundState.isAir()).thenReturn(false);
            when(level.getMinY()).thenReturn(0);
            when(level.getMaxY()).thenReturn(256);
            when(level.getBlockState(any(BlockPos.class))).thenAnswer(invocation -> {
                BlockPos position = invocation.getArgument(0);
                return position.equals(blockedPosition.get())
                        || position.getY() < 64
                        ? groundState
                        : airState;
            });
            when(level.getRandom()).thenReturn(random);
            when(random.nextInt(anyInt())).thenReturn(0);

            when(spawner.getBlockPos()).thenReturn(spawnerPosition);
            when(spawner.getReservedTargetCounterPositions())
                    .thenReturn(Map.of());
            when(spawner.getReservedTargetCounterPositionFollowingCustomerId(
                    targetPosition,
                    customerId
            )).thenAnswer(ignored -> followingCustomerId.get());
            when(spawner.tryReserveTargetCounterPosition(
                    targetPosition,
                    customerId
            )).thenAnswer(ignored ->
                    followingCustomerId.get() == null
                            ? customerId
                            : leaderId
            );

            when(level.getEntity(leaderId)).thenReturn(leader);
            when(leader.isAlive()).thenReturn(true);
            when(leader.isRemoved()).thenReturn(false);
            when(leader.getState()).thenReturn(CustomerState.BUYING);
            when(leader.blockPosition()).thenAnswer(
                    ignored -> leaderPosition.get()
            );
            when(leader.getEyePosition()).thenAnswer(
                    ignored -> leaderPosition.get().getCenter()
            );

            goalSelector.addGoal(
                    0,
                    new CustomerMoveToCounterGoal(customer, 0.5)
            );
            goalSelector.addGoal(
                    0,
                    new CustomerLineUpGoal(customer, 0.5)
            );
            goalSelector.addGoal(
                    0,
                    new CustomerWaitOnLeaderGoal(customer)
            );
        }

        private MockedStatic<CustomerCounter> mockCounterSearch() {
            MockedStatic<CustomerCounter> counter =
                    mockStatic(CustomerCounter.class);
            counter.when(() -> CustomerCounter.findCounterPositions(
                    level,
                    spawnerPosition,
                    counterState
            )).thenReturn(List.of(counterPosition));
            counter.when(() -> CustomerCounter.findValidSurroundingPositions(
                    level,
                    List.of(counterPosition),
                    customer,
                    null
            )).thenReturn(new ArrayList<>(List.of(
                    new CustomerCounter.SurroundingPosition(
                            counterPosition,
                            targetPosition
                    )
            )));
            return counter;
        }

        private void tick() {
            goalSelector.tick();
        }

        private void tick(int count) {
            for (int index = 0; index < count; index++) {
                tick();
            }
        }

        private void moveCustomerTo(BlockPos position) {
            customerPosition.set(position.getBottomCenter());
        }

        private long stateCount(CustomerState expectedState) {
            return stateHistory.stream()
                    .filter(expectedState::equals)
                    .count();
        }
    }
}
