package global.file;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Service
public class LocalImageStorageService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );

    private final Path uploadRoot;

    public LocalImageStorageService(@Value("${app.upload.dir:./uploads}") String uploadDirectory) {
        this.uploadRoot = Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    @PostConstruct
    void initialize() {
        try {
            for (ImageCategory category : ImageCategory.values()) {
                Files.createDirectories(uploadRoot.resolve(category.directory()));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not initialize the image upload directory.", exception);
        }
    }

    public StoredImage store(MultipartFile file, ImageCategory category) {
        String contentType = validateAndDetectContentType(file);
        String storedName = UUID.randomUUID() + EXTENSIONS.get(contentType);
        Path categoryDirectory = uploadRoot.resolve(category.directory()).normalize();
        Path target = categoryDirectory.resolve(storedName).normalize();

        if (!target.startsWith(categoryDirectory)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image path.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to store the image.",
                    exception
            );
        }

        return new StoredImage(
                sanitizeOriginalName(file.getOriginalFilename()),
                storedName,
                "/uploads/" + category.directory() + "/" + storedName
        );
    }

    private String validateAndDetectContentType(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file is empty.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.CONTENT_TOO_LARGE, "Image must be 10MB or smaller.");
        }

        String detectedType;
        try (InputStream inputStream = file.getInputStream()) {
            byte[] header = inputStream.readNBytes(12);
            detectedType = detectImageType(header);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not read the image.", exception);
        }

        if (detectedType == null || !detectedType.equals(file.getContentType())) {
            throw new ResponseStatusException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Only valid JPG, PNG, and WEBP images are allowed."
            );
        }
        return detectedType;
    }

    private String detectImageType(byte[] header) {
        if (header.length >= 3
                && unsigned(header[0]) == 0xff
                && unsigned(header[1]) == 0xd8
                && unsigned(header[2]) == 0xff) {
            return "image/jpeg";
        }
        if (header.length >= 8
                && unsigned(header[0]) == 0x89
                && header[1] == 'P' && header[2] == 'N' && header[3] == 'G'
                && unsigned(header[4]) == 0x0d && unsigned(header[5]) == 0x0a
                && unsigned(header[6]) == 0x1a && unsigned(header[7]) == 0x0a) {
            return "image/png";
        }
        if (header.length >= 12
                && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') {
            return "image/webp";
        }
        return null;
    }

    private int unsigned(byte value) {
        return value & 0xff;
    }

    private String sanitizeOriginalName(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            return "image";
        }
        String normalizedName = originalName.replace('\\', '/');
        String fileName = normalizedName.substring(normalizedName.lastIndexOf('/') + 1)
                .replaceAll("[\\p{Cntrl}]", "_");
        if (fileName.isBlank()) {
            return "image";
        }
        return fileName.length() <= 255 ? fileName : fileName.substring(fileName.length() - 255);
    }
}
