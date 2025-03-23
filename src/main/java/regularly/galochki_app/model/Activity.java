package regularly.galochki_app.model;

import lombok.Data;

@Data
public class Activity {
    private String name;
    private ActivityType type; // enum: BOOLEAN, GALOCHKI, NUMERIC
    private Double dailyNorm;
    private Double weeklyNorm;
    private Integer periodicity;
}
