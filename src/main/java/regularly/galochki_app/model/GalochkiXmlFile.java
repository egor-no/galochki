package regularly.galochki_app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GalochkiXmlFile {
    private Page page;
    private List<GalochkiWeek> galochki;
}