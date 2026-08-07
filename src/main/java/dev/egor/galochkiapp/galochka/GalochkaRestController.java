package dev.egor.galochkiapp.galochka;

import dev.egor.galochkiapp.month.MonthPageService;
import dev.egor.galochkiapp.month.WeekSummaryDto;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/galochki")
public class GalochkaRestController {

    private final GalochkaService galochkaService;
    private final MonthPageService monthPageService;

    public GalochkaRestController(GalochkaService galochkaService, MonthPageService monthPageService) {
        this.galochkaService = galochkaService;
        this.monthPageService = monthPageService;
    }

    @PostMapping("/click")
    public GalochkaUpdateDto click(@RequestParam Long activityId, @RequestParam LocalDate date, @RequestParam int year, @RequestParam int month) {
        Galochka galochka = galochkaService.handleLeftClick(activityId, date);
        return toUpdateDto(galochka, year, month);
    }

    @PostMapping("/value")
    public GalochkaValueDto setValue(@RequestParam Long activityId, @RequestParam LocalDate date, @RequestParam(required = false) BigDecimal value) {
        return toDto(galochkaService.setNumericValue(activityId, date, value));
    }

    @PostMapping("/reset")
    public GalochkaUpdateDto reset(@RequestParam Long activityId, @RequestParam LocalDate date, @RequestParam int year, @RequestParam int month) {
        Galochka galochka = galochkaService.reset(activityId, date);
        return toUpdateDto(galochka, year, month);
    }

    private GalochkaValueDto toDto(Galochka galochka) {
        return new GalochkaValueDto(galochka.getValue().stripTrailingZeros().toPlainString());
    }

    private GalochkaUpdateDto toUpdateDto(Galochka galochka, int year, int month) {
        Long pageId = galochka.getActivity().getPage().getId();
        List<WeekSummaryDto> summaries = monthPageService.buildWeekSummaries(pageId, YearMonth.of(year, month));
        return new GalochkaUpdateDto(galochka.getValue().stripTrailingZeros().toPlainString(), summaries);
    }
}