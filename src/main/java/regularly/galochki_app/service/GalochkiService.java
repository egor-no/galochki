package regularly.galochki_app.service;

import org.springframework.beans.factory.annotation.Autowired;
import regularly.galochki_app.model.GalochkiPage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GalochkiService {
    private static final String BASE_PATH = "data/sections/";

    @Autowired
    private XmlMapper xmlMapper;

    @Autowired
    private ItogService itogService;

    public void createSection(String sectionName) throws IOException {
        Path sectionPath = Paths.get(BASE_PATH, sectionName);
        Files.createDirectories(sectionPath);
    }

    public GalochkiPage loadPage(String sectionName, String yearMonth) throws IOException {
        Path filePath = Paths.get(BASE_PATH, sectionName, yearMonth + ".xml");

        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("Page not found: " + filePath);
        }

        GalochkiPage page = xmlMapper.readValue(filePath.toFile(), GalochkiPage.class);

        if (page.getPage().isDailyItog()) {
            itogService.calculateDailyItog(page);
        }

        return page;
    }

    public void savePage(String sectionName, String yearMonth, GalochkiPage page) throws IOException {
        Path filePath = Paths.get(BASE_PATH, sectionName, yearMonth + ".xml");

        Files.createDirectories(filePath.getParent());

        xmlMapper.writeValue(filePath.toFile(), page);
    }
}
