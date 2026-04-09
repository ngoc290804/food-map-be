package com.doan.backend.modules.upload.service.impl;

import com.doan.backend.common.exception.BadRequestException;
import com.doan.backend.config.UploadProperties;
import com.doan.backend.modules.upload.dto.response.UploadResponse;
import com.doan.backend.modules.upload.service.UploadService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private final UploadProperties uploadProperties;

    @Override
    public UploadResponse uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File upload không được để trống");
        }

        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "image" : file.getOriginalFilename());
        String fileName = UUID.randomUUID() + "-" + originalName;
        Path uploadDir = Path.of(uploadProperties.getDir());

        try {
            Files.createDirectories(uploadDir);
            Files.copy(file.getInputStream(), uploadDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new BadRequestException("Không thể upload file");
        }

        return UploadResponse.builder()
                .fileName(fileName)
                .url("/api/uploads/image/" + fileName)
                .build();
    }
}
