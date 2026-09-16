package com.vikingkittens.mc.customers.advancements.ftb;

import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomersFTBTasksTest {
    @Test
    void recordsProgressForAnActiveTask() {
        Quest quest = mock(Quest.class);
        TeamData teamData = mock(TeamData.class);
        CustomersFTBTasks.PetItemsServedTask task =
                new CustomersFTBTasks.PetItemsServedTask(1L, quest);
        when(teamData.canStartTasks(quest)).thenReturn(true);

        task.recordProgress(teamData);

        verify(teamData).addProgress(task, 1L);
    }

    @Test
    void ignoresProgressUntilQuestDependenciesAreSatisfied() {
        Quest quest = mock(Quest.class);
        TeamData teamData = mock(TeamData.class);
        CustomersFTBTasks.PetItemsServedTask task =
                new CustomersFTBTasks.PetItemsServedTask(1L, quest);

        task.recordProgress(teamData);

        verify(teamData, never()).addProgress(task, 1L);
    }
}
