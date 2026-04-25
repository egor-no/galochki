package dev.egor.galochkiapp.galochka;

import dev.egor.galochkiapp.activity.Activity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "galochka",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_galochka_activity_date",
                        columnNames = {"activity_id", "mark_date"}
                )
        }
)
public class Galochka {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @Column(name = "mark_date", nullable = false)
    private LocalDate date;

    @Column(name = "mark_value", nullable = false, precision = 5, scale = 2)
    private BigDecimal value = BigDecimal.ZERO;

    public Long getId() {
        return id;
    }

    public Activity getActivity() {
        return activity;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}