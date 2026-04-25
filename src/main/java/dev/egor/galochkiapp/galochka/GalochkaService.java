package dev.egor.galochkiapp.galochka;

import dev.egor.galochkiapp.activity.Activity;
import dev.egor.galochkiapp.activity.ActivityRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class GalochkaService {

    private final GalochkaRepository galochkaRepository;
    private final ActivityRepository activityRepository;

    public GalochkaService(GalochkaRepository galochkaRepository,
                           ActivityRepository activityRepository) {
        this.galochkaRepository = galochkaRepository;
        this.activityRepository = activityRepository;
    }

    public Galochka toggle(Long activityId, LocalDate date) {
        Galochka galochka = galochkaRepository
                .findByActivityIdAndDate(activityId, date)
                .orElseGet(() -> createEmptyGalochka(activityId, date));

        if (BigDecimal.ZERO.compareTo(galochka.getValue()) == 0) {
            galochka.setValue(BigDecimal.ONE);
        } else {
            galochka.setValue(BigDecimal.ZERO);
        }

        return galochkaRepository.save(galochka);
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