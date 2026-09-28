package com.vikingkittens.mc.customers.client.advancements.ftb;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.task.Task;

import com.vikingkittens.mc.customers.advancements.ftb.CustomersFTBTasks;

public final class CustomersFTBTaskGuiProvider {
    private CustomersFTBTaskGuiProvider() {}

    public static void initialize() {
        CustomersFTBTasks.CUSTOMERS_TASK.setGuiProvider(CustomersFTBTaskGuiProvider::openTaskMenu);
    }

    private static void openTaskMenu(Panel panel, Quest quest, Consumer<Task> callback) {
        List<ContextMenuItem> menuItems = Arrays.stream(CustomersFTBTasks.CustomersTaskKind.values())
                .map(taskKind -> new ContextMenuItem(
                        taskKind.displayName(),
                        taskKind.icon(),
                        button -> openTaskEditor(panel, quest, callback, taskKind)
                ))
                .toList();
        panel.getGui().openContextMenu(menuItems);
    }

    private static void openTaskEditor(
            Panel panel,
            Quest quest,
            Consumer<Task> callback,
            CustomersFTBTasks.CustomersTaskKind taskKind
    ) {
        CustomersFTBTasks.CustomersTask task = CustomersFTBTasks.createTask(quest, taskKind);
        ConfigGroup config = new ConfigGroup("customers", accepted -> {
            if (accepted) {
                callback.accept(task);
            }
            panel.run();
        }).setNameKey(taskKind.displayNameKey());
        task.fillConfigGroup(task.createSubGroup(config));
        new EditConfigScreen(config).openGui();
    }
}
