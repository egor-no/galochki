package dev.egor.galochkiapp.galochka;

import dev.egor.galochkiapp.activity.Activity;
import dev.egor.galochkiapp.activity.ActivityRepository;
import org.springframework.stereotype.Service;
import dev.egor.galochkiapp.week.PageWeekOverheadService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class GalochkaService {

    private final GalochkaRepository galochkaRepository;
    private final ActivityRepository activityRepository;
    private final PageWeekOverheadService overheadService;

    public GalochkaService(GalochkaRepository galochkaRepository, ActivityRepository activityRepository, PageWeekOverheadService overheadService) {
        this.galochkaRepository = galochkaRepository;
        this.activityRepository = activityRepository;
        this.overheadService = overheadService;
    }

    @Transactional
    public Galochka toggle(Long activityId, LocalDate date) {
        Galochka galochka = galochkaRepository.findByActivityIdAndDate(activityId, date)
                .orElseGet(() -> createEmptyGalochka(activityId, date));

        if (BigDecimal.ZERO.compareTo(galochka.getValue()) == 0) {
            galochka.setValue(BigDecimal.ONE);
        } else {
            galochka.setValue(BigDecimal.ZERO);
        }

        Galochka saved = galochkaRepository.save(galochka);
        Long pageId = saved.getActivity().getPage().getId();

        overheadService.recalculateFrom(pageId, date);

        return saved;
    }

    private Galochka createEmptyGalochka(Long activityId, LocalDate date) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found: " + activityId));

        Galochka galochka = new Galochka();
        galochka.setActivity(activity);
        galochka.setDate(date);
        galochka.setValue(BigDecimal.ZERO);

        return galochka;
    }
}