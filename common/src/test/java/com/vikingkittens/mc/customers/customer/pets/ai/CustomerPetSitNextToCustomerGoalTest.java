package com.vikingkittens.mc.customers.customer.pets.ai;

import org.junit.jupiter.api.Test;

import net.minecraft.core.Direction;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomerPetSitNextToCustomerGoalTest {
    @Test
    void prefersPositionsBesideTheCustomer() {
        assertEquals(0, CustomerPetSitNextToCustomerGoal.getPositionScore(Direction.NORTH, Direction.WEST));
        assertEquals(0, CustomerPetSitNextToCustomerGoal.getPositionScore(Direction.NORTH, Direction.EAST));
    }

    @Test
    void prefersBehindTheCustomerBeforeInFront() {
        assertEquals(1, CustomerPetSitNextToCustomerGoal.getPositionScore(Direction.NORTH, Direction.SOUTH));
        assertEquals(2, CustomerPetSitNextToCustomerGoal.getPositionScore(Direction.NORTH, Direction.NORTH));
    }
}
