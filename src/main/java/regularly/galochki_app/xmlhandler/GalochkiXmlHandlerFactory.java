package regularly.galochki_app.xmlhandler;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Component
public class GalochkiXmlHandlerFactory {

    private final GalochkiXmlHandler selectedHandler;

    public GalochkiXmlHandlerFactory(
            @Value("${galochki.xml.handler:jackson}") String type,
            JacksonXmlHandler jacksonHandler,
            DomXmlHandler domHandler
            // Add DOM, SAX, etc. when needed
    ) {
        this.selectedHandler = switch (type.toLowerCase()) {
            case "dom" -> domHandler;
            case "sax" -> throw new UnsupportedOperationException("SAX not implemented yet");
            case "jaxb" -> throw new UnsupportedOperationException("JAXB not implemented yet");
            case "jackson", "default" -> jacksonHandler;
            default -> jacksonHandler;
        };
    }

    @Bean
    public GalochkiXmlHandler getXmlHandler() {
        return selectedHandler;
    }
}