package com.doan.backend.modules.upload.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.doan.backend.common.exception.BadRequestException;
import com.doan.backend.config.CloudinaryProperties;
import com.doan.backend.modules.upload.dto.response.UploadResponse;
import com.doan.backend.modules.upload.service.UploadService;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final Cloudinary cloudinary;
    private final CloudinaryProperties cloudinaryProperties;

    @Override
    public UploadResponse uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File upload khong duoc de trong");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new BadRequestException("Chi chap nhan anh JPG, PNG hoac WEBP");
        }

        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", cloudinaryProperties.getFolder(),
                    "resource_type", "image",
                    "use_filename", false,
                    "unique_filename", true,
                    "overwrite", false
            ));
            String publicId = String.valueOf(result.get("public_id"));
            String secureUrl = String.valueOf(result.get("secure_url"));
            return UploadResponse.builder()
                    .fileName(publicId)
                    .url(secureUrl)
                    .publicId(publicId)
                    .build();
        } catch (IOException ex) {
            throw new BadRequestException("Khong the upload file");
        }
    }
}
