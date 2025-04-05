package regularly.galochki_app.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class GalochkiDay {
    private LocalDate day; // day of the month
    private List<Galochka> galochki; // one per activity
}
