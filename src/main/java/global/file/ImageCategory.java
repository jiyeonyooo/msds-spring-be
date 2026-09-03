package global.file;

public enum ImageCategory {
    ROOM("rooms"),
    FACILITY("facilities");

    private final String directory;

    ImageCategory(String directory) {
        this.directory = directory;
    }

    public String directory() {
        return directory;
    }
}
