package com.vikingkittens.mc.customers.client.advancements.ftb;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.config.gui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.client.gui.widget.ContextMenuItem;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.task.Task;

import net.minecraft.nbt.CompoundTag;

import com.vikingkittens.mc.customers.advancements.ftb.CustomersFTBTasks;

public final class CustomersFTBTaskGuiProvider {
    private CustomersFTBTaskGuiProvider() {}

    public static void initialize() {
        CustomersFTBTasks.CUSTOMERS_TASK.setGuiProvider(CustomersFTBTaskGuiProvider::openTaskMenu);
    }

    private static void openTaskMenu(
            Panel panel,
            Quest quest,
            BiConsumer<Task, CompoundTag> callback
    ) {
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
            BiConsumer<Task, CompoundTag> callback,
            CustomersFTBTasks.CustomersTaskKind taskKind
    ) {
        CustomersFTBTasks.CustomersTask task = CustomersFTBTasks.createTask(quest, taskKind);
        EditableConfigGroup config = new EditableConfigGroup("customers", accepted -> {
            if (accepted) {
                callback.accept(task, task.getType().makeExtraNBT());
            }
            panel.run();
        }).setNameKey(taskKind.displayNameKey());
        task.fillConfigGroup(task.createSubGroup(config));
        new EditConfigScreen(config).openGui();
    }
}
