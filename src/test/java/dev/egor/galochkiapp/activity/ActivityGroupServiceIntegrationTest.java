package dev.egor.galochkiapp.activity;

import dev.egor.galochkiapp.page.GalochkiPage;
import dev.egor.galochkiapp.page.GalochkiPageService;
import dev.egor.galochkiapp.page.PageType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ActivityGroupServiceIntegrationTest {

    @Autowired
    private ActivityGroupService groupService;

    @Autowired
    private ActivityGroupRepository groupRepository;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private GalochkiPageService pageService;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Создание группы связывает её со страницей и помещает после существующих групп")
    void createsGroupAtTheEndOfPage() {
        GalochkiPage page = createPage();
        ActivityGroup first = groupService.create(page.getId(), "Работа");
        ActivityGroup second = groupService.create(page.getId(), "Здоровье");

        assertThat(first.getPage().getId()).isEqualTo(page.getId());
        assertThat(first.getSortOrder()).isZero();
        assertThat(second.getSortOrder()).isEqualTo(1);
        assertThat(groupRepository.findByPageIdOrderBySortOrderAscIdAsc(page.getId()))
                .extracting(ActivityGroup::getTitle)
                .containsExactly("Работа", "Здоровье");
    }

    @Test
    @DisplayName("Переименование сохраняет новое название группы")
    void renamesGroup() {
        ActivityGroup group = groupService.create(createPage().getId(), "Старое название");

        groupService.renameForCurrentOwner(group.getId(), "Новое название");
        entityManager.flush();
        entityManager.clear();

        assertThat(groupRepository.findById(group.getId()).orElseThrow().getTitle())
                .isEqualTo("Новое название");
    }

    @Test
    @DisplayName("Полная перестановка групп сохраняет переданный порядок")
    void reordersAllGroups() {
        GalochkiPage page = createPage();
        ActivityGroup first = groupService.create(page.getId(), "Первая");
        ActivityGroup second = groupService.create(page.getId(), "Вторая");
        ActivityGroup third = groupService.create(page.getId(), "Третья");

        groupService.reorderGroupsForCurrentOwner(
                page.getId(), List.of(third.getId(), first.getId(), second.getId()));
        entityManager.flush();
        entityManager.clear();

        assertThat(groupRepository.findByPageIdOrderBySortOrderAscIdAsc(page.getId()))
                .extracting(ActivityGroup::getId)
                .containsExactly(third.getId(), first.getId(), second.getId());
    }

    @Test
    @DisplayName("Удаление группы переносит её дела в конец блока «Без группы»")
    void deletesGroupAndMovesActivitiesToUngroupedEnd() {
        GalochkiPage page = createPage();
        ActivityGroup group = groupService.create(page.getId(), "Удаляемая");
        Activity existingUngrouped = activityService.create(page.getId(), "Уже без группы", null);
        Activity firstInGroup = activityService.create(page.getId(), "Первое в группе", group.getId());
        Activity secondInGroup = activityService.create(page.getId(), "Второе в группе", group.getId());

        groupService.deleteForCurrentOwner(group.getId());
        entityManager.flush();
        entityManager.clear();

        assertThat(groupRepository.findById(group.getId())).isEmpty();
        assertThat(activityRepository.findByPageIdAndGroupIsNullAndActiveTrueOrderBySortOrderAscIdAsc(page.getId()))
                .extracting(Activity::getId)
                .containsExactly(existingUngrouped.getId(), firstInGroup.getId(), secondInGroup.getId());
    }

    @Test
    @DisplayName("Удаление группы переносит в «Без группы» также неактивные дела")
    void deletesGroupAndMovesInactiveActivitiesToo() {
        GalochkiPage page = createPage();
        ActivityGroup group = groupService.create(page.getId(), "Удаляемая");
        Activity inactive = activityService.create(page.getId(), "Архивное дело", group.getId());
        inactive.setActive(false);
        entityManager.flush();

        groupService.deleteForCurrentOwner(group.getId());
        entityManager.flush();
        entityManager.clear();

        Activity reloaded = activityRepository.findById(inactive.getId()).orElseThrow();
        assertThat(reloaded.getGroup()).isNull();
        assertThat(groupRepository.findById(group.getId())).isEmpty();
    }

    @Test
    @DisplayName("Неполный список перестановки групп отклоняется без частичного изменения порядка")
    void rejectsIncompleteGroupReorder() {
        GalochkiPage page = createPage();
        ActivityGroup first = groupService.create(page.getId(), "Первая");
        groupService.create(page.getId(), "Вторая");

        assertThatThrownBy(() -> groupService.reorderGroupsForCurrentOwner(page.getId(), List.of(first.getId())))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private GalochkiPage createPage() {
        return pageService.create("Тестовая страница", DayOfWeek.MONDAY, PageType.HALF_STEP, new BigDecimal("10"),
                false, true, false);
    }
}
