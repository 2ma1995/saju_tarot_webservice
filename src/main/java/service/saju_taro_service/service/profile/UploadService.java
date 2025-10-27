package service.saju_taro_service.service.profile;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@Service
public class UploadService {
    @Value("${file.upload-dir}")
    private String uploadDir;

    public String uploadProfileImage(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;

        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path target = dir.resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + fileName; // URL 반환
        } catch (IOException e) {
            log.error("File upload failed", e);
            throw new RuntimeException("파일 업로드 실패");
        }
    }
}
