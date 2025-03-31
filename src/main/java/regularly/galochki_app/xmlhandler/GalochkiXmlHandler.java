package regularly.galochki_app.xmlhandler;

import regularly.galochki_app.model.GalochkiXmlFile;

import java.io.IOException;
import java.nio.file.Path;

public interface GalochkiXmlHandler {
    GalochkiXmlFile read(Path path) throws IOException;

    void write(Path path, GalochkiXmlFile page) throws IOException;
}