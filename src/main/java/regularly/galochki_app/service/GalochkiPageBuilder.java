package regularly.galochki_app.service;

import org.springframework.stereotype.Service;
import regularly.galochki_app.model.Activity;
import regularly.galochki_app.model.GalochkiWeek;
import regularly.galochki_app.model.GalochkiPage;
import regularly.galochki_app.model.Page;

import java.util.List;

@Service
public class GalochkiPageBuilder {
    private final ItogService itogService;

    public GalochkiPageBuilder(ItogService itogService) {
        this.itogService = itogService;
    }

    public GalochkiPage build(Page page, List<Activity> activites, List<GalochkiWeek> weeks) {
        GalochkiPage result = new GalochkiPage();
        result.setPage(page);
        result.setActivites(activites);
        result.setGalochki(weeks);

        itogService.calculateDailyItog(result);

        return result;
    }

}
