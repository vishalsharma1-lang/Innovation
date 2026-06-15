package com.cms.service;

import com.cms.entity.SeoSettings;
import com.cms.repository.SeoSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SeoService {

    @Autowired
    private SeoSettingsRepository seoSettingsRepository;

    public List<SeoSettings> getAllSeoSettings() {
        return seoSettingsRepository.findAll();
    }

    public Page<SeoSettings> getSeoSettingsPaginated(Pageable pageable) {
        return seoSettingsRepository.findByIsDeletedFalse(pageable);
    }

    public Page<SeoSettings> searchSeoSettings(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return seoSettingsRepository.findByIsDeletedFalse(pageable);
        }
        return seoSettingsRepository.searchSeoSettings(search.trim(), pageable);
    }

    public Optional<SeoSettings> getSeoByPage(String pageName) {
        return seoSettingsRepository.findByPageNameIgnoreCaseAndIsDeletedFalse(pageName);
    }

    public Optional<SeoSettings> getSeoById(Long id) {
        return seoSettingsRepository.findById(id);
    }

    public SeoSettings saveSeoSettings(SeoSettings seoSettings) {
        return seoSettingsRepository.save(seoSettings);
    }

    public SeoSettings createOrUpdateSeo(SeoSettings seoSettings) {
        Optional<SeoSettings> existing = seoSettingsRepository.findByPageName(seoSettings.getPageName());
        if (existing.isPresent() && (seoSettings.getId() == null || !seoSettings.getId().equals(existing.get().getId()))) {
            // Update existing
            SeoSettings existingSettings = existing.get();
            existingSettings.setSeoTitle(seoSettings.getSeoTitle());
            existingSettings.setMetaDescription(seoSettings.getMetaDescription());
            existingSettings.setKeywords(seoSettings.getKeywords());
            existingSettings.setCanonicalUrl(seoSettings.getCanonicalUrl());
            existingSettings.setRobots(seoSettings.getRobots());
            existingSettings.setOgTitle(seoSettings.getOgTitle());
            existingSettings.setOgDescription(seoSettings.getOgDescription());
            return seoSettingsRepository.save(existingSettings);
        }
        return seoSettingsRepository.save(seoSettings);
    }

    public void deleteSeoSettings(Long id) {
        Optional<SeoSettings> optional = seoSettingsRepository.findById(id);
        if (optional.isPresent()) {
            SeoSettings entity = optional.get();
            entity.setIsDeleted(true);
            seoSettingsRepository.save(entity);
        }
    }

    public boolean existsByPageName(String pageName) {
        return seoSettingsRepository.existsByPageName(pageName);
    }
}
