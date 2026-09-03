package global.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalImageStorageServiceTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void storesAValidPngWithGeneratedName() throws Exception {
        LocalImageStorageService service = service();
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "room.png",
                "image/png",
                pngBytes()
        );

        StoredImage storedImage = service.store(file, ImageCategory.ROOM);

        assertThat(storedImage.originalName()).isEqualTo("room.png");
        assertThat(storedImage.storedName()).endsWith(".png");
        assertThat(storedImage.url()).isEqualTo("/uploads/rooms/" + storedImage.storedName());
        assertThat(Files.exists(temporaryDirectory.resolve("rooms").resolve(storedImage.storedName())))
                .isTrue();
    }

    @Test
    void rejectsContentTypeThatDoesNotMatchFileSignature() {
        LocalImageStorageService service = service();
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "fake.png",
                "image/png",
                new byte[]{1, 2, 3, 4}
        );

        assertThatThrownBy(() -> service.store(file, ImageCategory.FACILITY))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode().value())
                .isEqualTo(415);
    }

    private LocalImageStorageService service() {
        LocalImageStorageService service = new LocalImageStorageService(temporaryDirectory.toString());
        service.initialize();
        return service;
    }

    private byte[] pngBytes() {
        return new byte[]{
                (byte) 0x89, 'P', 'N', 'G',
                0x0d, 0x0a, 0x1a, 0x0a,
                0, 0, 0, 0
        };
    }
}
