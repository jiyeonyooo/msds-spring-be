package global.file;

public record StoredImage(
        String originalName,
        String storedName,
        String url
) {
}
