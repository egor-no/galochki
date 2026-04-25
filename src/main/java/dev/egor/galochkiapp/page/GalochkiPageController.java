package dev.egor.galochkiapp.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.DayOfWeek;

@Controller
public class GalochkiPageController {

    private final GalochkiPageService pageService;

    public GalochkiPageController(GalochkiPageService pageService) {
        this.pageService = pageService;
    }

    @PostMapping("/pages")
    public String create(@RequestParam String title,
                         @RequestParam DayOfWeek weekStartDay) {

        GalochkiPage page = pageService.create(title, weekStartDay);

        return "redirect:/month?pageId=" + page.getId();
    }
}