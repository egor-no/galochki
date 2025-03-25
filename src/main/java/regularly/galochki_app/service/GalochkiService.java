package regularly.galochki_app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import regularly.galochki_app.model.GalochkiPage;
import regularly.galochki_app.xmlhandler.GalochkiXmlHandler;
import regularly.galochki_app.xmlhandler.GalochkiXmlHandlerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class GalochkiService {

    private static final String BASE_PATH = "data/sections/";

    @Autowired
    private ItogService itogService;

    @Autowired
    private GalochkiXmlHandler xmlHandler;

    public void createSection(String sectionName) throws IOException {
        Path sectionPath = Paths.get(BASE_PATH, sectionName);
        Files.createDirectories(sectionPath);
    }

    public GalochkiPage loadPage(String sectionName, String yearMonth) throws IOException {
        Path filePath = Paths.get(BASE_PATH, sectionName, yearMonth + ".xml");

        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("Page not found: " + filePath);
        }

        GalochkiPage page = xmlHandler.read(filePath);

        if (page.getPage().isDailyItog()) {
            itogService.calculateDailyItog(page);
        }

        return page;
    }

    public void savePage(String sectionName, String yearMonth, GalochkiPage page) throws IOException {
        Path filePath = Paths.get(BASE_PATH, sectionName, yearMonth + ".xml");

        Files.createDirectories(filePath.getParent());

        xmlHandler.write(filePath, page);
    }
}
