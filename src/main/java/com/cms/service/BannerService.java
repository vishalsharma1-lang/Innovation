package com.cms.service;

import com.cms.entity.BannerSettings;
import com.cms.repository.BannerSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BannerService {

    @Autowired
    private BannerSettingsRepository bannerSettingsRepository;

    public List<BannerSettings> getAllBanners() {
        return bannerSettingsRepository.findAll();
    }

    public Page<BannerSettings> getBannersPaginated(Pageable pageable) {
        return bannerSettingsRepository.findByIsDeletedFalse(pageable);
    }

    public Page<BannerSettings> searchBanners(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return bannerSettingsRepository.findByIsDeletedFalse(pageable);
        }
        return bannerSettingsRepository.searchBanners(search.trim(), pageable);
    }

    public List<BannerSettings> getBannersByPage(String pageName) {
        return bannerSettingsRepository.findByPageNameIgnoreCaseAndIsActiveTrueAndIsDeletedFalseOrderByDisplayOrderAsc(pageName);
    }

    public Optional<BannerSettings> getBannerById(Long id) {
        return bannerSettingsRepository.findById(id);
    }

    public BannerSettings saveBanner(BannerSettings bannerSettings) {
        return bannerSettingsRepository.save(bannerSettings);
    }

    public void deleteBanner(Long id) {
        Optional<BannerSettings> optional = bannerSettingsRepository.findById(id);
        if (optional.isPresent()) {
            BannerSettings entity = optional.get();
            entity.setIsDeleted(true);
            bannerSettingsRepository.save(entity);
        }
    }
}
