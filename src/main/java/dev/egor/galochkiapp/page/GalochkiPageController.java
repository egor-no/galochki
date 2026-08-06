package dev.egor.galochkiapp.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.DayOfWeek;

@Controller
public class GalochkiPageController {

    private final GalochkiPageService pageService;

    public GalochkiPageController(GalochkiPageService pageService) {
        this.pageService = pageService;
    }

    @PostMapping("/pages")
    public String create(@RequestParam String title, @RequestParam DayOfWeek weekStartDay, @RequestParam PageType pageType, @RequestParam(required = false) BigDecimal weeklyNorm) {
        GalochkiPage page = pageService.create(title, weekStartDay, pageType, weeklyNorm);
        return "redirect:/month?pageId=" + page.getId();
    }

    @PostMapping("/pages/update")
    public String update(@RequestParam Long pageId, @RequestParam String title, @RequestParam Integer year, @RequestParam Integer month) {
        pageService.updateTitleForCurrentOwner(pageId, title);
        return "redirect:/month?pageId=" + pageId + "&year=" + year + "&month=" + month + "&edit=true";
    }

    @PostMapping("/pages/delete")
    public String delete(@RequestParam Long pageId) {
        pageService.deleteForCurrentOwner(pageId);
        return "redirect:/month";
    }
}