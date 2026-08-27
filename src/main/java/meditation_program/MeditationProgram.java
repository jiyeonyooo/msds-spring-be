package meditation_program;

public class MeditationProgram {

    public MeditationProgram(String program_name, String picture, int capacity, int remain, String status) {
        this.program_name = program_name;
        this.picture = picture;
        this.capacity = capacity;
        this.remain = remain;
        this.status = status;
    }

    String program_name;
    //It's v.0...
    //Have to Add for picture
    String picture;
    int capacity;
    int remain;
    // ["모집중", "마감"]
    String status;
}