package regularly.galochki_app.model;

import lombok.Data;

import java.util.List;

@Data
public class GalochkiPage {
    private Page page;
    private List<Activity> activites;
    private List<GalochkiWeek> galochki;
}
