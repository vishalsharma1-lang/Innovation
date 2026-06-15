package com.cms.service.page;

import com.cms.entity.page.*;
import com.cms.repository.page.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PageService {

    @Autowired private DynamicPageRepository pageRepo;
    @Autowired private PageSectionRepository sectionRepo;
    @Autowired private PageVersionRepository versionRepo;
    @Autowired private ObjectMapper objectMapper;

    // ─── Page CRUD ────────────────────────────────────────

    public Page<DynamicPage> searchPages(String search, Pageable pageable) {
        if (search == null || search.isBlank()) return pageRepo.findByIsDeletedFalse(pageable);
        return pageRepo.searchPages(search.trim(), pageable);
    }

    public Optional<DynamicPage> getById(Long id) { return pageRepo.findById(id); }
    public Optional<DynamicPage> getBySlug(String slug) { return pageRepo.findBySlugAndIsDeletedFalse(slug); }
    public Optional<DynamicPage> getPublishedBySlug(String slug) { return pageRepo.findFirstBySlugAndStatusAndIsDeletedFalse(slug, "published"); }

    public DynamicPage savePage(DynamicPage page) {
        if (page.getSlug() == null || page.getSlug().isBlank()) {
            page.setSlug(page.getTitle().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("-+$", ""));
        }
        if (page.getIsDeleted() == null) page.setIsDeleted(false);
        if (page.getStatus() == null) page.setStatus("draft");
        return pageRepo.save(page);
    }

    public void deletePage(Long id) {
        pageRepo.findById(id).ifPresent(p -> { p.setIsDeleted(true); pageRepo.save(p); });
    }

    public DynamicPage publishPage(Long id) {
        return pageRepo.findById(id).map(p -> {
            p.setStatus("published");
            return pageRepo.save(p);
        }).orElse(null);
    }

    public DynamicPage unpublishPage(Long id) {
        return pageRepo.findById(id).map(p -> {
            p.setStatus("draft");
            return pageRepo.save(p);
        }).orElse(null);
    }

    // ─── Clone Page ───────────────────────────────────────

    public DynamicPage clonePage(Long sourcePageId, String newTitle, String newSlug) {
        DynamicPage source = pageRepo.findById(sourcePageId).orElse(null);
        if (source == null) return null;

        DynamicPage clone = new DynamicPage();
        clone.setTitle(newTitle != null ? newTitle : source.getTitle() + " (Copy)");
        clone.setSlug(newSlug != null ? newSlug : source.getSlug() + "-copy");
        clone.setTemplateName(source.getTemplateName());
        clone.setSourcePageId(sourcePageId);
        clone.setStatus("draft");
        clone.setLanguage(source.getLanguage());
        clone.setSeoTitle(source.getSeoTitle());
        clone.setSeoDescription(source.getSeoDescription());
        clone.setSeoKeywords(source.getSeoKeywords());
        clone.setOgTitle(source.getOgTitle());
        clone.setOgDescription(source.getOgDescription());
        clone.setOgImage(source.getOgImage());
        clone.setHeadScripts(source.getHeadScripts());
        clone.setBodyScripts(source.getBodyScripts());
        clone = pageRepo.save(clone);

        // Clone sections
        List<PageSection> sourceSections = sectionRepo.findByPageIdAndIsDeletedFalseOrderByDisplayOrderAsc(sourcePageId);
        for (PageSection s : sourceSections) {
            PageSection cloneSection = new PageSection();
            cloneSection.setPageId(clone.getId());
            cloneSection.setSectionType(s.getSectionType());
            cloneSection.setSectionKey(s.getSectionKey());
            cloneSection.setDisplayOrder(s.getDisplayOrder());
            cloneSection.setIsVisible(s.getIsVisible());
            cloneSection.setTitle(s.getTitle());
            cloneSection.setSubtitle(s.getSubtitle());
            cloneSection.setContent(s.getContent());
            cloneSection.setPlainText(s.getPlainText());
            cloneSection.setImageUrl(s.getImageUrl());
            cloneSection.setImageAlt(s.getImageAlt());
            cloneSection.setVideoUrl(s.getVideoUrl());
            cloneSection.setButtonText(s.getButtonText());
            cloneSection.setButtonUrl(s.getButtonUrl());
            cloneSection.setButtonStyle(s.getButtonStyle());
            cloneSection.setBackgroundColor(s.getBackgroundColor());
            cloneSection.setTextColor(s.getTextColor());
            cloneSection.setCssClass(s.getCssClass());
            cloneSection.setJsonData(s.getJsonData());
            sectionRepo.save(cloneSection);
        }

        return clone;
    }

    // ─── Section CRUD ──────────────────────────────────────

    public List<PageSection> getSections(Long pageId) {
        return sectionRepo.findByPageIdAndIsDeletedFalseOrderByDisplayOrderAsc(pageId);
    }

    public List<PageSection> getVisibleSections(Long pageId) {
        return sectionRepo.findByPageIdAndIsVisibleTrueAndIsDeletedFalseOrderByDisplayOrderAsc(pageId);
    }

    public Optional<PageSection> getSectionById(Long id) { return sectionRepo.findById(id); }

    public PageSection saveSection(PageSection section) {
        if (section.getIsDeleted() == null) section.setIsDeleted(false);
        if (section.getIsVisible() == null) section.setIsVisible(true);
        return sectionRepo.save(section);
    }

    public void deleteSection(Long id) {
        sectionRepo.findById(id).ifPresent(s -> { s.setIsDeleted(true); sectionRepo.save(s); });
    }

    public void reorderSections(Long pageId, List<Long> sectionIds) {
        for (int i = 0; i < sectionIds.size(); i++) {
            sectionRepo.findById(sectionIds.get(i)).ifPresent(s -> {
                s.setDisplayOrder(sectionIds.indexOf(s.getId()));
                sectionRepo.save(s);
            });
        }
    }

    public void toggleSectionVisibility(Long id) {
        sectionRepo.findById(id).ifPresent(s -> {
            s.setIsVisible(!s.getIsVisible());
            sectionRepo.save(s);
        });
    }

    // ─── Versioning ───────────────────────────────────────

    public PageVersion createVersion(Long pageId, String changeNote, String createdBy) {
        List<PageSection> sections = getSections(pageId);
        DynamicPage page = pageRepo.findById(pageId).orElse(null);
        if (page == null) return null;

        // Get latest version number
        List<PageVersion> existing = versionRepo.findByPageIdOrderByVersionNumberDesc(pageId);
        int nextVersion = existing.isEmpty() ? 1 : existing.get(0).getVersionNumber() + 1;

        PageVersion version = new PageVersion();
        version.setPageId(pageId);
        version.setVersionNumber(nextVersion);
        version.setChangeNote(changeNote);
        version.setCreatedBy(createdBy);

        try {
            version.setSnapshotData(objectMapper.writeValueAsString(sections));
        } catch (Exception e) {
            version.setSnapshotData("[]");
        }

        // Update page version number
        page.setVersionNumber(nextVersion);
        pageRepo.save(page);

        return versionRepo.save(version);
    }

    public List<PageVersion> getVersionHistory(Long pageId) {
        return versionRepo.findByPageIdOrderByVersionNumberDesc(pageId);
    }

    public boolean rollbackToVersion(Long pageId, Long versionId) {
        PageVersion version = versionRepo.findById(versionId).orElse(null);
        if (version == null || !version.getPageId().equals(pageId)) return false;

        try {
            // Soft-delete all current sections
            List<PageSection> current = getSections(pageId);
            for (PageSection s : current) {
                s.setIsDeleted(true);
                sectionRepo.save(s);
            }

            // Restore from snapshot
            PageSection[] snapSections = objectMapper.readValue(version.getSnapshotData(), PageSection[].class);
            for (PageSection s : snapSections) {
                PageSection restored = new PageSection();
                restored.setPageId(pageId);
                restored.setSectionType(s.getSectionType());
                restored.setSectionKey(s.getSectionKey());
                restored.setDisplayOrder(s.getDisplayOrder());
                restored.setIsVisible(s.getIsVisible());
                restored.setTitle(s.getTitle());
                restored.setSubtitle(s.getSubtitle());
                restored.setContent(s.getContent());
                restored.setPlainText(s.getPlainText());
                restored.setImageUrl(s.getImageUrl());
                restored.setImageAlt(s.getImageAlt());
                restored.setVideoUrl(s.getVideoUrl());
                restored.setButtonText(s.getButtonText());
                restored.setButtonUrl(s.getButtonUrl());
                restored.setButtonStyle(s.getButtonStyle());
                restored.setBackgroundColor(s.getBackgroundColor());
                restored.setTextColor(s.getTextColor());
                restored.setCssClass(s.getCssClass());
                restored.setJsonData(s.getJsonData());
                restored.setIsDeleted(false);
                sectionRepo.save(restored);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
