package dev.egor.galochkiapp.galochka;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/galochki")
public class GalochkaRestController {

    private final GalochkaService galochkaService;

    public GalochkaRestController(GalochkaService galochkaService) {
        this.galochkaService = galochkaService;
    }

    @PostMapping("/increment")
    public GalochkaValueDto increment(@RequestParam Long activityId,
                                      @RequestParam LocalDate date) {
        Galochka galochka = galochkaService.increment(activityId, date);

        return new GalochkaValueDto(
                galochka.getValue().stripTrailingZeros().toPlainString()
        );
    }

    @PostMapping("/reset")
    public GalochkaValueDto reset(@RequestParam Long activityId,
                                  @RequestParam LocalDate date) {
        Galochka galochka = galochkaService.reset(activityId, date);

        return new GalochkaValueDto(
                galochka.getValue().stripTrailingZeros().toPlainString()
        );
    }
}