package dev.egor.galochkiapp.galochka;

import dev.egor.galochkiapp.activity.Activity;
import dev.egor.galochkiapp.activity.ActivityRepository;
import dev.egor.galochkiapp.page.PageType;
import dev.egor.galochkiapp.week.PageWeekOverheadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class GalochkaService {

    private static final BigDecimal HALF = new BigDecimal("0.5");
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final GalochkaRepository galochkaRepository;
    private final ActivityRepository activityRepository;
    private final PageWeekOverheadService overheadService;

    public GalochkaService(GalochkaRepository galochkaRepository,
                           ActivityRepository activityRepository,
                           PageWeekOverheadService overheadService) {
        this.galochkaRepository = galochkaRepository;
        this.activityRepository = activityRepository;
        this.overheadService = overheadService;
    }

    @Transactional
    public Galochka handleLeftClick(Long activityId, LocalDate date) {
        Galochka galochka = findOrCreate(activityId, date);

        PageType pageType = galochka.getActivity()
                .getPage()
                .getPageType();

        BigDecimal currentValue = normalizeValue(galochka.getValue());

        switch (pageType) {
            case BINARY -> galochka.setValue(toggleBinary(currentValue));
            case HALF_STEP -> galochka.setValue(currentValue.add(HALF));
            case NUMBER -> throw new IllegalStateException(
                    "Для числовой страницы значение вводится вручную"
            );
        }

        Galochka saved = galochkaRepository.save(galochka);
        recalculateNormIfSupported(saved, date);
        return saved;
    }

    @Transactional
    public Galochka setNumericValue(Long activityId, LocalDate date, BigDecimal value) {
        Galochka galochka = findOrCreate(activityId, date);
        PageType pageType = galochka.getActivity()
                .getPage()
                .getPageType();

        if (pageType != PageType.NUMBER) {
            throw new IllegalStateException(
                    "Ручной ввод доступен только для числовой страницы"
            );
        }

        BigDecimal normalizedValue = value == null ? ZERO : value;
        galochka.setValue(normalizedValue);
        return galochkaRepository.save(galochka);
    }

    @Transactional
    public Galochka reset(Long activityId, LocalDate date) {
        Galochka galochka = findOrCreate(activityId, date);
        galochka.setValue(ZERO);
        Galochka saved = galochkaRepository.save(galochka);
        recalculateNormIfSupported(saved, date);
        return saved;
    }

    private BigDecimal toggleBinary(BigDecimal currentValue) {
        return currentValue.compareTo(ZERO) == 0 ? ONE : ZERO;
    }

    private BigDecimal normalizeValue(BigDecimal value) {
        return value == null ? ZERO : value;
    }

    private Galochka findOrCreate(Long activityId, LocalDate date) {
        return galochkaRepository.findByActivityIdAndDate(activityId, date)
                .orElseGet(() -> createEmptyGalochka(activityId, date));
    }

    private void recalculateNormIfSupported(Galochka galochka, LocalDate date) {

        if (!galochka.getActivity().getPage().hasWeeklyNorm()) {
            return;
        }

        Long pageId = galochka.getActivity().getPage().getId();
        overheadService.recalculateFrom(pageId, date);
    }

    private Galochka createEmptyGalochka(Long activityId, LocalDate date) {

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Activity not found: " + activityId
                        )
                );

        Galochka galochka = new Galochka();
        galochka.setActivity(activity);
        galochka.setDate(date);
        galochka.setValue(ZERO);
        return galochka;
    }
}