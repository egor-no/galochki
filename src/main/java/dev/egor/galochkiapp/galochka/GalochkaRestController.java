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

    @PostMapping("/toggle")
    public GalochkaValueDto toggle(@RequestParam Long activityId,
                                   @RequestParam LocalDate date) {
        Galochka galochka = galochkaService.toggle(activityId, date);
        return new GalochkaValueDto(galochka.getValue().toPlainString());
    }
}