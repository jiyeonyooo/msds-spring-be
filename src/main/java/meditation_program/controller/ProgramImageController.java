package meditation_program.controller;

import global.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/meditation/admin")
@RequiredArgsConstructor
public class ProgramImageController {

    private static final String UPLOAD_DIR = "uploads/program/";

    @PostMapping("/program/upload-image")
    public ResponseEntity<ApiResponse<String>> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        String extension = getExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + extension;

        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        file.transferTo(uploadPath.resolve(fileName));

        String imageUrl = "/uploads/program/" + fileName;
        return ApiResponse.success(HttpStatus.CREATED, "이미지가 업로드되었습니다.", imageUrl);
    }

    private String getExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) return "";
        return originalFilename.substring(originalFilename.lastIndexOf("."));
    }
}