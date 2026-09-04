package meditation_program.controller;

import global.dto.response.ApiResponse;
import global.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@Tag(name = "관리자 - 명상 프로그램", description = "명상 프로그램에 사용할 이미지를 업로드합니다.")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class ProgramImageController {

    private static final String UPLOAD_DIR = "uploads/program/";

    @PostMapping(value = "/program/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "명상 프로그램 이미지 업로드")
    public ResponseEntity<ApiResponse<String>> uploadImage(
            @Parameter(description = "업로드할 이미지 파일", required = true)
            @RequestParam("file") MultipartFile file) throws IOException {
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
