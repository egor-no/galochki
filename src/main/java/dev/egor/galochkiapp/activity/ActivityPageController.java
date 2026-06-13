package dev.egor.galochkiapp.activity;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class ActivityPageController {

    private final ActivityService activityService;

    public ActivityPageController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping("/activities")
    public String create(@RequestParam Long pageId,
                         @RequestParam String title,
                         @RequestParam(required = false) Long groupId,
                         @RequestParam(required = false) Integer year,
                         @RequestParam(required = false) Integer month) {

        activityService.create(pageId, title, groupId);

        if (year != null && month != null) {
            return "redirect:/month?pageId=" + pageId + "&year=" + year + "&month=" + month + "&edit=true";
        }

        return "redirect:/month?pageId=" + pageId + "&edit=true";
    }

    @PostMapping("/activities/update")
    @ResponseBody
    public String update(@RequestParam Long activityId,
                         @RequestParam String title) {

        activityService.renameForCurrentOwner(activityId, title);

        return "OK";
    }

    @PostMapping("/activities/reorder")
    @ResponseBody
    public String reorder(@RequestParam Long pageId,
                          @RequestBody List<ActivityReorderGroupDto> groups) {

        activityService.reorderForCurrentOwner(pageId, groups);
        return "OK";
    }

    @PostMapping("/activities/delete")
    public String delete(@RequestParam Long activityId,
                         @RequestParam Long pageId,
                         @RequestParam Integer year,
                         @RequestParam Integer month) {

        activityService.deleteForCurrentOwner(activityId);

        return "redirect:/month?pageId=" + pageId + "&year=" + year + "&month=" + month + "&edit=true";
    }
}