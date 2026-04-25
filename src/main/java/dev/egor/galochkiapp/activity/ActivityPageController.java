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
    public String create(@RequestParam String title) {
        activityService.create(title);
        return "redirect:/month";
    }
}