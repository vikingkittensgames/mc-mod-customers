package com.vikingkittens.mc.customers.compatability;

import java.util.Iterator;
import java.util.ServiceLoader;

public final class CustomersServices {
    private CustomersServices() {}

    public static IPlatformHelper platform() {
        return PlatformHolder.INSTANCE;
    }

    public static IItemStackHelper itemStacks() {
        return ItemStackHolder.INSTANCE;
    }

    public static IConfigHelper config() {
        return ConfigHolder.INSTANCE;
    }

    public static INetworkHelper network() {
        return NetworkHolder.INSTANCE;
    }

    public static IRegistrationHelper registration() {
        return RegistrationHolder.INSTANCE;
    }

    static <T> T requireSingle(Class<T> serviceType, Iterable<T> services) {
        Iterator<T> iterator = services.iterator();
        if (!iterator.hasNext()) {
            throw new IllegalStateException(
                    "No " + serviceType.getName() + " implementation was found"
            );
        }

        T service = iterator.next();
        if (iterator.hasNext()) {
            throw new IllegalStateException(
                    "Multiple " + serviceType.getName() + " implementations were found"
            );
        }
        return service;
    }

    private static <T> T load(Class<T> serviceType) {
        return requireSingle(serviceType, ServiceLoader.load(serviceType));
    }

    private static final class PlatformHolder {
        private static final IPlatformHelper INSTANCE = load(IPlatformHelper.class);
    }

    private static final class ItemStackHolder {
        private static final IItemStackHelper INSTANCE = load(IItemStackHelper.class);
    }

    private static final class ConfigHolder {
        private static final IConfigHelper INSTANCE = load(IConfigHelper.class);
    }

    private static final class NetworkHolder {
        private static final INetworkHelper INSTANCE = load(INetworkHelper.class);
    }

    private static final class RegistrationHolder {
        private static final IRegistrationHelper INSTANCE = load(IRegistrationHelper.class);
    }
}
