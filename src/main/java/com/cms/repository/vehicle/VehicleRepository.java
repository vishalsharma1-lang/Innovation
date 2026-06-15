package com.cms.repository.vehicle;

import com.cms.entity.vehicle.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Page<Vehicle> findByIsDeletedFalse(Pageable pageable);
    List<Vehicle> findByIsActiveTrueAndIsDeletedFalse();
    List<Vehicle> findByIsFeaturedTrueAndIsActiveTrueAndIsDeletedFalse();
    Optional<Vehicle> findBySlugAndIsDeletedFalse(String slug);
    Optional<Vehicle> findFirstBySlugAndIsDeletedFalse(String slug);
    Optional<Vehicle> findByIdAndIsDeletedFalse(Long id);

    @Query("SELECT v FROM Vehicle v WHERE " +
           "(LOWER(v.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.brand) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.model) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.category) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND v.isDeleted = false")
    Page<Vehicle> searchVehicles(@Param("search") String search, Pageable pageable);

    @Query("SELECT v FROM Vehicle v WHERE v.category = :category AND v.isDeleted = false AND v.isActive = true")
    List<Vehicle> findByCategory(@Param("category") String category);

    @Query("SELECT v FROM Vehicle v WHERE v.brand = :brand AND v.isDeleted = false AND v.isActive = true")
    List<Vehicle> findByBrand(@Param("brand") String brand);

    @Query("SELECT v FROM Vehicle v WHERE LOWER(v.brand) = LOWER(:brand) AND v.isDeleted = false AND v.isActive = true ORDER BY v.name ASC")
    List<Vehicle> findByBrandIgnoreCase(@Param("brand") String brand);

    @Query("SELECT DISTINCT v.brand FROM Vehicle v WHERE v.isDeleted = false AND v.isActive = true AND v.brand IS NOT NULL ORDER BY v.brand ASC")
    List<String> findDistinctBrands();

    @Query("SELECT v FROM Vehicle v WHERE " +
           "(LOWER(v.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.brand) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.model) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.category) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND v.isDeleted = false AND v.isActive = true ORDER BY v.name ASC")
    List<Vehicle> searchActiveVehicles(@Param("search") String search);

    // ── Used Car Queries ──────────────────────────────────────
    List<Vehicle> findByVehicleTypeAndIsActiveTrueAndIsDeletedFalse(String vehicleType);

    @Query("SELECT v FROM Vehicle v WHERE v.vehicleType = :type AND v.isActive = true AND v.isDeleted = false " +
           "AND (LOWER(v.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.brand) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.model) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY v.createdAt DESC")
    List<Vehicle> searchByTypeAndKeyword(@Param("type") String type, @Param("search") String search);

    @Query("SELECT v FROM Vehicle v WHERE v.vehicleType = :type AND v.isActive = true AND v.isDeleted = false ORDER BY v.createdAt DESC")
    Page<Vehicle> findByTypePageable(@Param("type") String type, Pageable pageable);

    @Query("SELECT v FROM Vehicle v WHERE v.vehicleType = :type AND v.isActive = true AND v.isDeleted = false " +
           "AND (LOWER(v.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.brand) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.model) LIKE LOWER(CONCAT('%', :search, '%'))) ORDER BY v.createdAt DESC")
    Page<Vehicle> searchByTypePageable(@Param("type") String type, @Param("search") String search, Pageable pageable);

    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.vehicleType = :type AND v.isDeleted = false")
    long countByType(@Param("type") String type);

    long countByIsDeletedFalse();

    @Query("SELECT v FROM Vehicle v WHERE v.vehicleType = :type AND v.isDeleted = false ORDER BY v.createdAt DESC")
    Page<Vehicle> findByTypeAndIsDeletedFalsePageable(@Param("type") String type, Pageable pageable);

    @Query("SELECT v FROM Vehicle v WHERE v.vehicleType = :type AND v.isDeleted = false " +
           "AND (LOWER(v.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.brand) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.model) LIKE LOWER(CONCAT('%', :search, '%'))) ORDER BY v.createdAt DESC")
    Page<Vehicle> searchByTypeAndIsDeletedFalsePageable(@Param("type") String type, @Param("search") String search, Pageable pageable);
}
