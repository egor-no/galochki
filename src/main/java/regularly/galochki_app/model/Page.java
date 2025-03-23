package regularly.galochki_app.model;

import lombok.Data;

@Data
public class Page {
    private String name;
    private PageType type; // enum: BOOLEAN, GALOCHKI, NUMERIC, MIXED
    private boolean dailyItog;
    private boolean weeklyItog;
    private Double dailyNorm;
    private Double weeklyNorm;
}
