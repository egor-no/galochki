package dev.egor.galochkiapp.activity;

import dev.egor.galochkiapp.galochka.Galochka;
import dev.egor.galochkiapp.galochka.GalochkaRepository;
import dev.egor.galochkiapp.page.GalochkiPage;
import dev.egor.galochkiapp.page.GalochkiPageService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ActivityServiceIntegrationTest {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private ActivityGroupService groupService;

    @Autowired
    private GalochkiPageService pageService;

    @Autowired
    private GalochkaRepository galochkaRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Создание дела в группе связывает его с группой и страницей")
    void createsActivityInsideGroup() {
        GalochkiPage page = createPage();
        ActivityGroup group = groupService.create(page.getId(), "Группа");

        Activity activity = activityService.create(page.getId(), "Дело", group.getId());

        assertThat(activity.getPage().getId()).isEqualTo(page.getId());
        assertThat(activity.getGroup().getId()).isEqualTo(group.getId());
        assertThat(activity.getSortOrder()).isZero();
        assertThat(activity.isActive()).isTrue();
    }

    @Test
    @DisplayName("Создание дела без группы помещает его в конец блока «Без группы»")
    void createsUngroupedActivityAtTheEnd() {
        GalochkiPage page = createPage();
        Activity first = activityService.create(page.getId(), "Первое", null);
        Activity second = activityService.create(page.getId(), "Второе", null);

        assertThat(first.getGroup()).isNull();
        assertThat(second.getGroup()).isNull();
        assertThat(second.getSortOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("Перестановка дел внутри группы сохраняет новый порядок")
    void reordersActivitiesInsideGroup() {
        GalochkiPage page = createPage();
        ActivityGroup group = groupService.create(page.getId(), "Группа");
        Activity first = activityService.create(page.getId(), "Первое", group.getId());
        Activity second = activityService.create(page.getId(), "Второе", group.getId());

        activityService.reorderForCurrentOwner(page.getId(), List.of(
                new ActivityReorderGroupDto(group.getId(), List.of(second.getId(), first.getId()))));
        entityManager.flush();
        entityManager.clear();

        assertThat(activityRepository.findByPageIdAndGroupIdAndActiveTrueOrderBySortOrderAscIdAsc(
                page.getId(), group.getId()))
                .extracting(Activity::getId)
                .containsExactly(second.getId(), first.getId());
    }

    @Test
    @DisplayName("Перестановка может перенести дело из одной группы в другую")
    void movesActivityBetweenGroups() {
        GalochkiPage page = createPage();
        ActivityGroup source = groupService.create(page.getId(), "Исходная");
        ActivityGroup target = groupService.create(page.getId(), "Целевая");
        Activity moved = activityService.create(page.getId(), "Переносимое", source.getId());

        activityService.reorderForCurrentOwner(page.getId(), List.of(
                new ActivityReorderGroupDto(source.getId(), List.of()),
                new ActivityReorderGroupDto(target.getId(), List.of(moved.getId()))));
        entityManager.flush();
        entityManager.clear();

        Activity reloaded = activityRepository.findById(moved.getId()).orElseThrow();
        assertThat(reloaded.getGroup().getId()).isEqualTo(target.getId());
        assertThat(reloaded.getSortOrder()).isZero();
    }

    @Test
    @DisplayName("Перестановка может перенести дело в блок «Без группы»")
    void movesActivityToUngroupedBlock() {
        GalochkiPage page = createPage();
        ActivityGroup group = groupService.create(page.getId(), "Группа");
        Activity moved = activityService.create(page.getId(), "Переносимое", group.getId());

        activityService.reorderForCurrentOwner(page.getId(), List.of(
                new ActivityReorderGroupDto(group.getId(), List.of()),
                new ActivityReorderGroupDto(null, List.of(moved.getId()))));
        entityManager.flush();
        entityManager.clear();

        Activity reloaded = activityRepository.findById(moved.getId()).orElseThrow();
        assertThat(reloaded.getGroup()).isNull();
        assertThat(reloaded.getSortOrder()).isZero();
    }

    @Test
    @DisplayName("Удаление дела удаляет также связанные с ним галочки")
    void deletesActivityTogetherWithItsMarks() {
        GalochkiPage page = createPage();
        Activity activity = activityService.create(page.getId(), "Удаляемое", null);
        Galochka mark = new Galochka();
        mark.setActivity(activity);
        mark.setDate(LocalDate.of(2026, 4, 10));
        mark.setValue(BigDecimal.ONE);
        galochkaRepository.saveAndFlush(mark);

        activityService.deleteForCurrentOwner(activity.getId());
        entityManager.flush();

        assertThat(activityRepository.findById(activity.getId())).isEmpty();
        assertThat(galochkaRepository.findByActivityIdAndDate(activity.getId(), mark.getDate())).isEmpty();
    }

    @Test
    @DisplayName("Неполная перестановка дел отклоняется без сохранения смешанного порядка")
    void rejectsIncompleteActivityReorder() {
        GalochkiPage page = createPage();
        ActivityGroup group = groupService.create(page.getId(), "Группа");
        Activity first = activityService.create(page.getId(), "Первое", group.getId());
        activityService.create(page.getId(), "Второе", group.getId());

        assertThatThrownBy(() -> activityService.reorderForCurrentOwner(page.getId(), List.of(
                new ActivityReorderGroupDto(group.getId(), List.of(first.getId())))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private GalochkiPage createPage() {
        return pageService.create("Тестовая страница", DayOfWeek.MONDAY, new BigDecimal("10"));
    }
}
