package com.vikingkittens.mc.customers.common.events;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class InternalEvents {
    private static final List<Handler> HANDLERS = new ArrayList<>();
    private static final Set<Class<?>> REGISTERED_TYPES = new HashSet<>();

    private InternalEvents() {}

    public static synchronized void register(Class<?> handlerType) {
        if (REGISTERED_TYPES.contains(handlerType)) {
            return;
        }
        List<Handler> discoveredHandlers = new ArrayList<>();
        for (Method method : handlerType.getDeclaredMethods()) {
            if (method.isAnnotationPresent(InternalEventHandler.class)) {
                validate(method);
                method.setAccessible(true);
                discoveredHandlers.add(new Handler(method.getParameterTypes()[0], method));
            }
        }
        HANDLERS.addAll(discoveredHandlers);
        REGISTERED_TYPES.add(handlerType);
    }

    public static void emit(InternalEvent event) {
        List<Handler> handlers;
        synchronized (InternalEvents.class) {
            handlers = List.copyOf(HANDLERS);
        }
        for (Handler handler : handlers) {
            if (handler.eventType().isInstance(event)) {
                handler.invoke(event);
            }
        }
    }

    private static void validate(Method method) {
        if (!Modifier.isStatic(method.getModifiers())
                || method.getParameterCount() != 1
                || !InternalEvent.class.isAssignableFrom(method.getParameterTypes()[0])) {
            throw new IllegalArgumentException(
                    "Internal event handlers must be static methods with one InternalEvent parameter: " + method
            );
        }
    }

    private record Handler(Class<?> eventType, Method method) {
        private void invoke(InternalEvent event) {
            try {
                method.invoke(null, event);
            } catch (IllegalAccessException | InvocationTargetException exception) {
                throw new IllegalStateException("Unable to invoke internal event handler " + method, exception);
            }
        }
    }
}
