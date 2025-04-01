package regularly.galochki_app.service;

import org.springframework.stereotype.Service;
import regularly.galochki_app.model.GalochkiPage;
import regularly.galochki_app.model.GalochkiXmlFile;
import regularly.galochki_app.xmlhandler.GalochkiXmlHandler;
import regularly.galochki_app.xmlhandler.GalochkiXmlHandlerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class GalochkiFileService {

    private static final String BASE_PATH = "data/sections/";

    private final GalochkiXmlHandler xmlHandler;
    private final GalochkiPageBuilder pageBuilder;

    public GalochkiFileService(
            GalochkiXmlHandlerFactory handlerFactory,
            GalochkiPageBuilder pageBuilder
    ) {
        this.xmlHandler = handlerFactory.getXmlHandler(); // get the actual implementation (e.g. Jackson)
        this.pageBuilder = pageBuilder; // Spring will autowire this one
    }

    public void createSection(String sectionName) throws IOException {
        Path sectionPath = Paths.get(BASE_PATH, sectionName);
        Files.createDirectories(sectionPath);
    }

    public GalochkiPage loadPage(String sectionName, String yearMonth) throws IOException {
        Path filePath = Paths.get(BASE_PATH, sectionName, yearMonth + ".xml");

        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("Page not found: " + filePath);
        }

        GalochkiXmlFile rawFile = xmlHandler.read(filePath);
        GalochkiPage page = pageBuilder.build(
                rawFile.getPage(),
                rawFile.getActivites(),
                rawFile.getGalochki()
        );

        return page;
    }

    public void savePage(String sectionName, String yearMonth, GalochkiPage page) throws IOException {
        Path filePath = Paths.get("data/sections", sectionName, yearMonth + ".xml");

        Files.createDirectories(filePath.getParent());

        GalochkiXmlFile file = new GalochkiXmlFile(
                page.getPage(),
                page.getActivites(),
                page.getGalochki()
        );

        xmlHandler.write(filePath, file);
    }
}
