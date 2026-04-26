package dev.egor.galochkiapp.activity;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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

    @PostMapping("/activities/update")
    @ResponseBody
    public String update(@RequestParam Long activityId,
                         @RequestParam String title) {

        activityService.renameForCurrentOwner(activityId, title);

        return "OK";
    }

    @PostMapping("/activities/delete")
    public String delete(@RequestParam Long activityId,
                         @RequestParam Long pageId,
                         @RequestParam Integer year,
                         @RequestParam Integer month) {

        activityService.deleteForCurrentOwner(activityId);

        return "redirect:/month?pageId=" + pageId + "&year=" + year + "&month=" + month;
    }
}