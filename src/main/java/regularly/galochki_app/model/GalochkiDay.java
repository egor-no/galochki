package regularly.galochki_app.model;

import lombok.Data;

import java.util.List;

@Data
public class GalochkiDay {
    private int day; // day of the month
    private List<Galochka> galochki; // one per activity
}
