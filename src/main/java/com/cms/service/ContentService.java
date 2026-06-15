package com.cms.service;

import com.cms.entity.ContentSettings;
import com.cms.repository.ContentSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ContentService {

    @Autowired
    private ContentSettingsRepository contentSettingsRepository;

    public List<ContentSettings> getAllContent() {
        return contentSettingsRepository.findAll();
    }

    public Page<ContentSettings> getContentPaginated(Pageable pageable) {
        return contentSettingsRepository.findByIsDeletedFalse(pageable);
    }

    public Page<ContentSettings> searchContent(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return contentSettingsRepository.findByIsDeletedFalse(pageable);
        }
        return contentSettingsRepository.searchContentSettings(search.trim(), pageable);
    }

    public List<ContentSettings> getContentByPage(String pageName) {
        return contentSettingsRepository.findByPageNameIgnoreCaseAndIsActiveTrueAndIsDeletedFalse(pageName);
    }

    public Optional<ContentSettings> getContentByPageAndSection(String pageName, String sectionName) {
        return contentSettingsRepository.findByPageNameAndSectionName(pageName, sectionName);
    }

    public Optional<ContentSettings> getContentById(Long id) {
        return contentSettingsRepository.findById(id);
    }

    public ContentSettings saveContent(ContentSettings contentSettings) {
        return contentSettingsRepository.save(contentSettings);
    }

    public ContentSettings createOrUpdateContent(ContentSettings content) {
        Optional<ContentSettings> existing = contentSettingsRepository
                .findByPageNameAndSectionName(content.getPageName(), content.getSectionName());

        if (existing.isPresent() && (content.getId() == null || !content.getId().equals(existing.get().getId()))) {
            ContentSettings existingContent = existing.get();
            existingContent.setHeading(content.getHeading());
            existingContent.setSubheading(content.getSubheading());
            existingContent.setDescription(content.getDescription());
            existingContent.setButtonText(content.getButtonText());
            existingContent.setButtonLink(content.getButtonLink());
            existingContent.setImageUrl(content.getImageUrl());
            existingContent.setIsActive(content.getIsActive());
            return contentSettingsRepository.save(existingContent);
        }
        return contentSettingsRepository.save(content);
    }

    public void deleteContent(Long id) {
        Optional<ContentSettings> optional = contentSettingsRepository.findById(id);
        if (optional.isPresent()) {
            ContentSettings entity = optional.get();
            entity.setIsDeleted(true);
            contentSettingsRepository.save(entity);
        }
    }
}
