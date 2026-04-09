package com.doan.backend.modules.upload.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UploadResponse {
    private final String fileName;
    private final String url;
}
