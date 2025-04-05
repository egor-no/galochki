package regularly.galochki_app.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import regularly.galochki_app.model.GalochkiPage;
import regularly.galochki_app.service.GalochkiFileService;
import regularly.galochki_app.service.GalochkiSectionService;

import java.io.IOException;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/page")
public class GalochkiController {

    private final GalochkiFileService fileService;
    private final GalochkiSectionService sectionService;

    @Autowired
    public GalochkiController(GalochkiFileService fileService, GalochkiSectionService sectionService) {
        this.fileService = fileService;
        this.sectionService = sectionService;
    }

    @GetMapping(value = "/{section}/{month}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GalochkiPage> loadPage(
            @PathVariable String section,
            @PathVariable String month
    ) {
        try {
            GalochkiPage page = fileService.loadPage(section, month);
            return ResponseEntity.ok(page);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/api/page/new/")
    public ResponseEntity<GalochkiPage> createNewPage() {
        return ResponseEntity.ok(sectionService.createNewPage());
    }

    @PostMapping("/{section}/{month}")
    public ResponseEntity<Void> savePage(
            @PathVariable String section,
            @PathVariable String month,
            @RequestBody GalochkiPage page
    ) {
        try {
            fileService.savePage(section, month, page);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
