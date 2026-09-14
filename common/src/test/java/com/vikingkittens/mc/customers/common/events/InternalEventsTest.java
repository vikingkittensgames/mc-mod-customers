package com.vikingkittens.mc.customers.common.events;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InternalEventsTest {
    @Test
    void emitsToHandlersForTheEventTypeAndItsBaseTypes() {
        TestHandlers.baseEvents = 0;
        TestHandlers.specificEvents = 0;
        InternalEvents.register(TestHandlers.class);

        InternalEvents.emit(new SpecificEvent());

        assertEquals(1, TestHandlers.baseEvents);
        assertEquals(1, TestHandlers.specificEvents);
    }

    @Test
    void registeringTheSameHandlerClassTwiceDoesNotDuplicateCalls() {
        DuplicateHandlers.events = 0;
        InternalEvents.register(DuplicateHandlers.class);
        InternalEvents.register(DuplicateHandlers.class);

        InternalEvents.emit(new SpecificEvent());

        assertEquals(1, DuplicateHandlers.events);
    }

    @Test
    void rejectsInvalidHandlerMethods() {
        assertThrows(IllegalArgumentException.class, () -> InternalEvents.register(InvalidHandlers.class));
    }

    private static class BaseEvent extends InternalEvent {}

    private static final class SpecificEvent extends BaseEvent {}

    private static final class TestHandlers {
        private static int baseEvents;
        private static int specificEvents;

        @InternalEventHandler
        public static void onBase(BaseEvent event) {
            baseEvents++;
        }

        @InternalEventHandler
        public static void onSpecific(SpecificEvent event) {
            specificEvents++;
        }
    }

    private static final class DuplicateHandlers {
        private static int events;

        @InternalEventHandler
        public static void onSpecific(SpecificEvent event) {
            events++;
        }
    }

    private static final class InvalidHandlers {
        @InternalEventHandler
        public void onSpecific(SpecificEvent event) {}
    }
}
