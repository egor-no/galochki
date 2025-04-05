package regularly.galochki_app.service;

import org.springframework.stereotype.Service;
import regularly.galochki_app.model.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class GalochkiPageSerializer {

    public GalochkiXmlFile serialize(GalochkiPage page) {
        List<GalochkiWeek> nonEmptyGalochki = page.getGalochki().stream()
                .map(this::filterWeek)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        GalochkiXmlFile file = new GalochkiXmlFile();

        file.setPage(page.getPage());
        file.setActivites(page.getActivities());
        file.setGalochki(nonEmptyGalochki);

        return file;
    }

    private GalochkiWeek filterWeek(GalochkiWeek week) {
        List<GalochkiDay> filteredDays = week.getGalochkiDays().stream()
                .filter(day -> day.getGalochki().stream().anyMatch(g -> g.getValue() != 0.0))
                .toList();

        if (filteredDays.isEmpty()) {
            return null;
        }

        GalochkiWeek filtered = new GalochkiWeek();
        filtered.setGalochkiDays(filteredDays);
        filtered.setDailyItog(week.getDailyItog());
        filtered.setOverflow(week.getOverflow());
        return filtered;
    }

}
