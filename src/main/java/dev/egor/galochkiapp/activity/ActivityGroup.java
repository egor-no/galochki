package dev.egor.galochkiapp.activity;

import dev.egor.galochkiapp.page.GalochkiPage;
import jakarta.persistence.*;

@Entity
@Table(name = "activity_group")
public class ActivityGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "page_id", nullable = false)
    private GalochkiPage page;

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public GalochkiPage getPage() {
        return page;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setPage(GalochkiPage page) {
        this.page = page;
    }
}