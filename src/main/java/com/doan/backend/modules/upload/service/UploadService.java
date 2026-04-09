package com.doan.backend.modules.upload.service;

import com.doan.backend.modules.upload.dto.response.UploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    UploadResponse uploadImage(MultipartFile file);
}
