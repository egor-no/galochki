package regularly.galochki_app.model;

import lombok.Data;

import java.util.List;

@Data
public class GalochkiWeek {

    private List<GalochkiDay> galochkiDays;
    private List<Double> dailyItog;
    private Double overflow;

}
