package dev.egor.galochkiapp.page;

import dev.egor.galochkiapp.activity.Activity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "galochki_page")
public class GalochkiPage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "week_start_day")
    private DayOfWeek weekStartDay = DayOfWeek.MONDAY;

    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Activity> activities = new ArrayList<>();

    @Column(name = "weekly_norm", nullable = false, precision = 10, scale = 2)
    private BigDecimal weeklyNorm = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "page_type", nullable = false)
    private PageType pageType = PageType.HALF_STEP;

    @Column(name = "show_statistics_without_norm", nullable = false)
    private boolean showStatisticsWithoutNorm = false;

    @Column(name = "show_week_completed_check", nullable = false)
    private boolean showWeekCompletedCheck = true;

    @Column(name = "show_week_percentage", nullable = false)
    private boolean showWeekPercentage = false;

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public DayOfWeek getWeekStartDay() {
        return weekStartDay;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setWeekStartDay(DayOfWeek weekStartDay) {
        this.weekStartDay = weekStartDay;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public BigDecimal getWeeklyNorm() {
        return weeklyNorm;
    }

    public void setWeeklyNorm(BigDecimal weeklyNorm) {
        this.weeklyNorm = weeklyNorm;
    }

    public PageType getPageType() {
        return pageType;
    }

    public void setPageType(PageType pageType) {
        this.pageType = pageType;
    }

    public boolean supportsWeeklyNorm() {
        return pageType != null && pageType != PageType.NUMBER;
    }

    public boolean isShowStatisticsWithoutNorm() {
        return showStatisticsWithoutNorm;
    }

    public void setShowStatisticsWithoutNorm(boolean showStatisticsWithoutNorm) {
        this.showStatisticsWithoutNorm = showStatisticsWithoutNorm;
    }

    public boolean isShowWeekCompletedCheck() {
        return showWeekCompletedCheck;
    }

    public void setShowWeekCompletedCheck(boolean showWeekCompletedCheck) {
        this.showWeekCompletedCheck = showWeekCompletedCheck;
    }

    public boolean isShowWeekPercentage() {
        return showWeekPercentage;
    }

    public void setShowWeekPercentage(boolean showWeekPercentage) {
        this.showWeekPercentage = showWeekPercentage;
    }

    public boolean hasWeeklyNorm() {
        return supportsWeeklyNorm()
                && weeklyNorm != null
                && weeklyNorm.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean shouldShowStatistics() {
        return hasWeeklyNorm() || showStatisticsWithoutNorm;
    }
    
}