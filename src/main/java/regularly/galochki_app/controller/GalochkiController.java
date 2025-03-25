package regularly.galochki_app.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sections")
public class GalochkiController {

    @Autowired
    private GalochkiService galochkiService;

    // 1. Create a new section
    @PostMapping("/{sectionName}")
    public void createSection(@PathVariable String sectionName) {
        galochkiService.createSection(sectionName);
    }

    // 2. Load a page (month XML)
    @GetMapping("/{sectionName}/pages/{yearMonth}")
    public GalochkiPage loadPage(@PathVariable String sectionName,
                                 @PathVariable String yearMonth) {
        return galochkiService.loadPage(sectionName, yearMonth);
    }

    // 3. Save the entire page after editing
    @PostMapping("/{sectionName}/pages/{yearMonth}")
    public void savePage(@PathVariable String sectionName,
                         @PathVariable String yearMonth,
                         @RequestBody GalochkiPage page) {
        galochkiService.savePage(sectionName, yearMonth, page);
    }
}
