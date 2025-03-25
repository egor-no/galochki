package regularly.galochki_app.xmlhandler;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import regularly.galochki_app.model.GalochkiPage;

import java.io.IOException;
import java.nio.file.Path;

@Service
public class JacksonXmlHandler implements GalochkiXmlHandler {

    @Autowired
    private final XmlMapper xmlMapper;

    public JacksonXmlHandler(XmlMapper xmlMapper) {
        this.xmlMapper = xmlMapper;
    }

    @Override
    public GalochkiPage read(Path path) throws IOException {
        return xmlMapper.readValue(path.toFile(), GalochkiPage.class);
    }

    @Override
    public void write(Path path, GalochkiPage page) throws IOException {
        xmlMapper.writeValue(path.toFile(), page);
    }
}