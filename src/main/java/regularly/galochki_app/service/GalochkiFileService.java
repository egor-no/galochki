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
import java.time.YearMonth;

@Service
public class GalochkiFileService {

    private static final String BASE_PATH = "data/sections/";

    private final GalochkiXmlHandler xmlHandler;
    private final GalochkiPageBuilder pageBuilder;
    private final GalochkiPageSerializer pageSerializer;

    public GalochkiFileService(
            GalochkiXmlHandlerFactory handlerFactory,
            GalochkiPageBuilder pageBuilder,
            GalochkiPageSerializer pageSerializer
    ) {
        this.xmlHandler = handlerFactory.getXmlHandler();
        this.pageBuilder = pageBuilder;
        this.pageSerializer = pageSerializer;
    }

    public void createSection(String sectionName) throws IOException {
        Path sectionPath = Paths.get(BASE_PATH, sectionName);
        Files.createDirectories(sectionPath);
    }

    public GalochkiPage loadPage(String sectionName, String yearMonthStr) throws IOException {
        Path filePath = Paths.get(BASE_PATH, sectionName, yearMonthStr + ".xml");

        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("Page not found: " + filePath);
        }

        GalochkiXmlFile rawFile = xmlHandler.read(filePath);
        YearMonth yearMonth = YearMonth.parse(yearMonthStr);

        GalochkiPage page = pageBuilder.build(
                rawFile.getPage(),
                rawFile.getActivites(),
                rawFile.getGalochki(),
                yearMonth
        );

        return page;
    }

    public void savePage(String sectionName, String yearMonth, GalochkiPage page) throws IOException {
        Path filePath = Paths.get(BASE_PATH, sectionName, yearMonth + ".xml");
        Files.createDirectories(filePath.getParent());

        GalochkiXmlFile file = pageSerializer.serialize(page);

        xmlHandler.write(filePath, file);
    }
}
