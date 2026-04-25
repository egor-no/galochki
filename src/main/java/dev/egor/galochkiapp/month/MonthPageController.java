package dev.egor.galochkiapp.month;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MonthPageController {

    private final MonthPageService monthPageService;

    public MonthPageController(MonthPageService monthPageService) {
        this.monthPageService = monthPageService;
    }

    @GetMapping({"/", "/month"})
    public String month(Model model) {
        model.addAttribute("page", monthPageService.buildCurrentMonth());
        return "month";
    }
}