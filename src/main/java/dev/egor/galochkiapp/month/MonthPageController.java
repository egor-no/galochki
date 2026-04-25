package dev.egor.galochkiapp.month;

import dev.egor.galochkiapp.page.GalochkiPage;
import dev.egor.galochkiapp.page.GalochkiPageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.DayOfWeek;
import java.time.YearMonth;

@Controller
public class MonthPageController {

    private final MonthPageService monthPageService;
    private final GalochkiPageService pageService;

    public MonthPageController(MonthPageService monthPageService,
                               GalochkiPageService pageService) {
        this.monthPageService = monthPageService;
        this.pageService = pageService;
    }

    @GetMapping({"/", "/month"})
    public String month(@RequestParam(required = false) Long pageId,
                        @RequestParam(required = false) Integer year,
                        @RequestParam(required = false) Integer month,
                        Model model) {

        if (!pageService.hasPagesForCurrentOwner()) {
            model.addAttribute("weekDays", DayOfWeek.values());
            return "create-page";
        }

        GalochkiPage selectedPage = pageId != null
                ? pageService.getByIdForCurrentOwner(pageId)
                : pageService.getFirstPageForCurrentOwner();

        YearMonth yearMonth = year != null && month != null
                ? YearMonth.of(year, month)
                : YearMonth.now();

        model.addAttribute("page", monthPageService.build(selectedPage.getId(), yearMonth));
        return "month";
    }
}