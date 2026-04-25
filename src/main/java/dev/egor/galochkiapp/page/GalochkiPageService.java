package dev.egor.galochkiapp.page;

import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.List;

@Service
public class GalochkiPageService {

    private final GalochkiPageRepository pageRepository;

    public GalochkiPageService(GalochkiPageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    public List<GalochkiPage> getAllPages() {
        return pageRepository.findAll();
    }

    public GalochkiPage getById(Long id) {
        return pageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Страница не найдена: " + id));
    }

    public GalochkiPage create(String title, DayOfWeek weekStartDay) {
        GalochkiPage page = new GalochkiPage();
        page.setTitle(title);
        page.setWeekStartDay(weekStartDay);

        return pageRepository.save(page);
    }

    public boolean hasPages() {
        return pageRepository.count() > 0;
    }

    public GalochkiPage getFirstPage() {
        return pageRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Страниц пока нет"));
    }
}