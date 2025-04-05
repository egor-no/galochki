package regularly.galochki_app.service;

import org.springframework.stereotype.Service;
import regularly.galochki_app.model.*;

import java.time.YearMonth;
import java.util.List;

@Service
public class GalochkiSectionService {

    private final GalochkiPageBuilder pageBuilder;

    public GalochkiSectionService(GalochkiPageBuilder pageBuilder) {
        this.pageBuilder = pageBuilder;
    }

    public GalochkiPage createNewPage() {
        YearMonth currentMonth = YearMonth.now();

        Page pageStub = new Page();
        pageStub.setName("");
        pageStub.setType(PageType.GALOCHKI);
        pageStub.setDailyItog(false);
        pageStub.setWeeklyItog(false);
        pageStub.setDailyNorm(null);
        pageStub.setWeeklyNorm(null);

        List<Activity> emptyActivities = List.of();
        List<GalochkiWeek> emptyWeeks = List.of();

        return pageBuilder.build(pageStub, emptyActivities, emptyWeeks, currentMonth);
    }

}
