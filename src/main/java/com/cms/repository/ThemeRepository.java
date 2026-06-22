package com.cms.repository;

import com.cms.entity.Theme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
    Optional<Theme> findByIsActiveTrue();

    @Modifying
    @Query("UPDATE Theme t SET t.isActive = false")
    void deactivateAll();
}
