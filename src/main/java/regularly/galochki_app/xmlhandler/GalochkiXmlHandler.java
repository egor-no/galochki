package regularly.galochki_app.xmlhandler;

import regularly.galochki_app.model.GalochkiPage;

import java.io.IOException;
import java.nio.file.Path;

public interface GalochkiXmlHandler {
    GalochkiPage read(Path path) throws IOException;

    void write(Path path, GalochkiPage page) throws IOException;
}