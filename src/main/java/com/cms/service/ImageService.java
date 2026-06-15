package com.cms.service;

import com.cms.entity.ImageSettings;
import com.cms.repository.ImageSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ImageService {

    @Autowired
    private ImageSettingsRepository imageSettingsRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public List<ImageSettings> getAllImages() {
        return imageSettingsRepository.findAll();
    }

    public List<ImageSettings> getActiveImages() {
        return imageSettingsRepository.findByIsActiveTrueAndIsDeletedFalse();
    }

    public Page<ImageSettings> searchImages(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return imageSettingsRepository.findByIsDeletedFalse(pageable);
        }
        return imageSettingsRepository.searchImages(search.trim(), pageable);
    }

    public Optional<ImageSettings> getImageById(Long id) {
        return imageSettingsRepository.findById(id);
    }

    public ImageSettings uploadImage(MultipartFile file, String altText, String category) throws IOException {
        // Create upload directory if not exists
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFileName = UUID.randomUUID().toString() + extension;

        // Save file
        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Save to database
        ImageSettings imageSettings = new ImageSettings();
        imageSettings.setImageName(uniqueFileName);
        imageSettings.setOriginalName(originalFilename);
        imageSettings.setImagePath(filePath.toString());
        imageSettings.setImageUrl("/uploads/" + uniqueFileName);
        imageSettings.setAltText(altText != null ? altText : originalFilename);
        imageSettings.setFileSize(file.getSize());
        imageSettings.setFileType(file.getContentType());
        imageSettings.setCategory(category != null ? category : "general");
        imageSettings.setIsActive(true);

        return imageSettingsRepository.save(imageSettings);
    }

    public ImageSettings replaceImage(Long id, MultipartFile newFile, String altText, String category) throws IOException {
        ImageSettings existing = imageSettingsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + id));

        // Delete old file
        try {
            Path oldFilePath = Paths.get(uploadDir, existing.getImageName());
            Files.deleteIfExists(oldFilePath);
        } catch (IOException e) {
            // Log but continue
        }

        // Upload new file
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = newFile.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFileName = UUID.randomUUID().toString() + extension;

        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(newFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Update database record
        existing.setImageName(uniqueFileName);
        existing.setOriginalName(originalFilename);
        existing.setImagePath(filePath.toString());
        existing.setImageUrl("/uploads/" + uniqueFileName);
        existing.setFileSize(newFile.getSize());
        existing.setFileType(newFile.getContentType());
        if (altText != null) existing.setAltText(altText);
        if (category != null) existing.setCategory(category);

        return imageSettingsRepository.save(existing);
    }

    public void deleteImage(Long id) throws IOException {
        ImageSettings image = imageSettingsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + id));

        // Soft delete - keep file on disk
        image.setIsDeleted(true);
        imageSettingsRepository.save(image);
    }

    public ImageSettings updateImageDetails(Long id, String altText, String category) {
        ImageSettings image = imageSettingsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + id));
        if (altText != null) image.setAltText(altText);
        if (category != null) image.setCategory(category);
        return imageSettingsRepository.save(image);
    }
}
