package dev.egor.galochkiapp.activity;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class ActivityGroupController {

    private final ActivityGroupService groupService;

    public ActivityGroupController(ActivityGroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping("/activity-groups")
    public String create(@RequestParam Long pageId,
                         @RequestParam String title,
                         @RequestParam Integer year,
                         @RequestParam Integer month) {

        groupService.create(pageId, title);

        return "redirect:/month?pageId=" + pageId + "&year=" + year + "&month=" + month + "&edit=true";
    }

    @PostMapping("/activity-groups/update")
    @ResponseBody
    public String update(@RequestParam Long groupId,
                         @RequestParam String title) {

        groupService.renameForCurrentOwner(groupId, title);
        return "OK";
    }

    @PostMapping("/activity-groups/reorder")
    @ResponseBody
    public String reorder(@RequestParam Long pageId,
                          @RequestParam List<Long> groupIds) {

        groupService.reorderGroupsForCurrentOwner(pageId, groupIds);
        return "OK";
    }

    @PostMapping("/activity-groups/delete")
    public String delete(@RequestParam Long groupId,
                         @RequestParam Long pageId,
                         @RequestParam Integer year,
                         @RequestParam Integer month) {

        groupService.deleteForCurrentOwner(groupId);

        return "redirect:/month?pageId=" + pageId + "&year=" + year + "&month=" + month + "&edit=true";
    }
}