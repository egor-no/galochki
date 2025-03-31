package regularly.galochki_app.xmlhandler;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import regularly.galochki_app.model.GalochkiXmlFile;

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
    public GalochkiXmlFile read(Path path) throws IOException {
        return xmlMapper.readValue(path.toFile(), GalochkiXmlFile.class);
    }

    @Override
    public void write(Path path, GalochkiXmlFile file) throws IOException {
        xmlMapper.writeValue(path.toFile(), file);
    }
}