package com._2.hungryworker.global.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 업로드된 파일을 로컬 디스크(app.upload.dir)에 저장하고,
 * WebConfig의 "/uploads/**" 정적 리소스 매핑으로 접근 가능한 URL을 돌려준다.
 * (운영 환경에서는 S3 등 오브젝트 스토리지로 교체하는 것을 권장)
 */
@Component
public class FileStorageService {

    private static final String PUBLIC_PATH_PREFIX = "/uploads/";

    private final String uploadDir;

    public FileStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String store(MultipartFile file, String subDir) {
        try {
            String originalFilename = StringUtils.cleanPath(
                    file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
            String extension = extractExtension(originalFilename);
            String storedFilename = UUID.randomUUID() + extension;

            Path targetDir = Paths.get(uploadDir, subDir).toAbsolutePath().normalize();
            Files.createDirectories(targetDir);

            Path targetPath = targetDir.resolve(storedFilename);
            file.transferTo(targetPath);

            return PUBLIC_PATH_PREFIX + subDir + "/" + storedFilename;
        } catch (IOException e) {
            throw new IllegalStateException("파일 저장에 실패했습니다.", e);
        }
    }

    public void delete(String publicUrl) {
        if (publicUrl == null || !publicUrl.startsWith(PUBLIC_PATH_PREFIX)) {
            return;
        }
        try {
            Path path = Paths.get(uploadDir, publicUrl.substring(PUBLIC_PATH_PREFIX.length()))
                    .toAbsolutePath().normalize();
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // 파일 삭제 실패는 리뷰 삭제 자체를 막을 이유가 없으므로 무시한다.
        }
    }

    private String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex >= 0 ? filename.substring(dotIndex) : "";
    }
}