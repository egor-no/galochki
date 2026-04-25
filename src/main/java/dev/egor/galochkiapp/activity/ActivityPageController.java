package dev.egor.galochkiapp.activity;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ActivityPageController {

    private final ActivityService activityService;

    public ActivityPageController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping("/activities")
    public String create(@RequestParam Long pageId,
                         @RequestParam String title,
                         @RequestParam(required = false) Integer year,
                         @RequestParam(required = false) Integer month) {

        activityService.create(pageId, title);

        if (year != null && month != null) {
            return "redirect:/month?pageId=" + pageId + "&year=" + year + "&month=" + month;
        }

        return "redirect:/month?pageId=" + pageId;
    }
}