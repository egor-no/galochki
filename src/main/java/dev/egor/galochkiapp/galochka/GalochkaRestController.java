package dev.egor.galochkiapp.galochka;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/galochki")
public class GalochkaRestController {

    private final GalochkaService galochkaService;

    public GalochkaRestController(GalochkaService galochkaService) {
        this.galochkaService = galochkaService;
    }

    @PostMapping("/click")
    public GalochkaValueDto click(@RequestParam Long activityId,
                                  @RequestParam LocalDate date) {

        Galochka galochka = galochkaService.handleLeftClick(activityId, date);

        return toDto(galochka);
    }

    @PostMapping("/value")
    public GalochkaValueDto setValue(@RequestParam Long activityId, @RequestParam LocalDate date, @RequestParam(required = false) BigDecimal value) {
        return toDto(galochkaService.setNumericValue(activityId, date, value));
    }

    @PostMapping("/reset")
    public GalochkaValueDto reset(@RequestParam Long activityId,
                                  @RequestParam LocalDate date) {
        Galochka galochka = galochkaService.reset(activityId, date);
        return toDto(galochka);
    }

    private GalochkaValueDto toDto(Galochka galochka) {
        return new GalochkaValueDto(galochka.getValue().stripTrailingZeros().toPlainString());
    }
}