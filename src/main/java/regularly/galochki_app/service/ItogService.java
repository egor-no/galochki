package regularly.galochki_app.service;

import org.springframework.stereotype.Service;
import regularly.galochki_app.model.*;

import java.util.ArrayList;
import java.util.List;

@Service
public class ItogService {

    private static final double PARTIAL_GALOCHKA_THRESHOLD = 0.5;

    public void calculateDailyItog(GalochkiPage page) {

        page.getGalochki().forEach(week -> {
            if (page.getPage().getType() == PageType.MIXED) {
                if (!isMixedPageNormalized(page.getActivities())) {
                    page.getPage().setDailyItog(false);
                    week.setDailyItog(null);
                    return;
                }
            }

            List<Double> results = new ArrayList<>();

            for (GalochkiDay day : week.getGalochkiDays()) {
                double sum = 0.0;

                List<Galochka> values = day.getGalochki();
                List<Activity> activities = page.getActivities();

                if (page.getPage().getType() == PageType.MIXED) {
                    for (int i = 0; i < values.size(); i++) {
                        if (activities.get(i).getType() == ActivityType.NUMERIC) {
                            sum += galochkizeNumeric(values.get(i).getValue(), activities.get(i).getDailyNorm());
                        } else {
                            sum += values.get(i).getValue();
                        }
                    }
                } else {
                    for (int i = 0; i < values.size(); i++) {
                        sum += values.get(i).getValue();
                    }
                }

                results.add(sum);
            }

            week.setDailyItog(results);
        });
    }

    private boolean isMixedPageNormalized(List<Activity> activities) {
            return activities.stream()
                    .filter(a -> a.getType() == ActivityType.NUMERIC)
                    .noneMatch(a -> a.getDailyNorm() == null || a.getDailyNorm() == 0.0);
    }

    private double galochkizeNumeric(double value, double norm) {
        double galochki = 0.0;
        double galochkaKoef = value / norm;
        double galochkaInt = Math.floor(galochkaKoef);
        double galochkaFrac = galochkaKoef - galochkaInt;
        if (galochkaFrac > PARTIAL_GALOCHKA_THRESHOLD) {
            galochki += PARTIAL_GALOCHKA_THRESHOLD;
        }
        galochki += galochkaInt;
        return galochki;
    }
}