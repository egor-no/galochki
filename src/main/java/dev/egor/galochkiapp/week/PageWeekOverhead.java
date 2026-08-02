package dev.egor.galochkiapp.week;

import dev.egor.galochkiapp.page.GalochkiPage;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "page_week_overhead",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_page_week_overhead",
                        columnNames = {"page_id", "week_start_date"}
                )
        }
)
public class PageWeekOverhead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "page_id", nullable = false)
    private GalochkiPage page;

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    @Column(name = "overhead_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal value = BigDecimal.ZERO;

    public Long getId() {
        return id;
    }

    public GalochkiPage getPage() {
        return page;
    }

    public void setPage(GalochkiPage page) {
        this.page = page;
    }

    public LocalDate getWeekStartDate() {
        return weekStartDate;
    }

    public void setWeekStartDate(LocalDate weekStartDate) {
        this.weekStartDate = weekStartDate;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}