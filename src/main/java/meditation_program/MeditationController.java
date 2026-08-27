package meditation_program;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

@RestController
@RequestMapping("/meditation")
public class MeditationController {

    //checking meditation programs
    @GetMapping("/program")
    public List<MeditationProgram> program_list(){
        List<MeditationProgram> test = new ArrayList<>();

        //For changing Programs' status, List may be bad choice
        //Time Complexity for searching...
        test.add(new MeditationProgram("별 보기",
                "사진 1",
                20,
                15,
                "모집중"));

        return test;
    }

    //reserving meditation programs
    @PostMapping("/program/{program_name}/{number}")
    public String program_reservation(@PathVariable String program_name,
                                      @PathVariable int number){
        return "";
    }

    //checking users' reviews
    @GetMapping("/review")
    public List<UserReview> review_list(){

        //For changing Programs' status, List may be bad choice
        //Time Complexity for searching...
        List<UserReview> test = new ArrayList<>();
        test.add(new UserReview("userid", LocalDate.now(), "test review"));

        return test;
    }

    //Create one's review
    @PostMapping("/review/{userid}/create/{review}")
    public String review_add(@PathVariable String userid,
                             @PathVariable String review){
        return review;
    }

    //Delete one's review
    @DeleteMapping("/review/{userid}/delete/{number}")
    public void review_delete(@PathVariable String userid,
                              @PathVariable int number){
        //number is idx of one's reviews
    }

    //-----------------------------------------

    @GetMapping("/admin")
    public List<MeditationProgram> program_admin_list(){

        //For changing Programs' status, List may be bad choice
        //Time Complexity for searching...
        List<MeditationProgram> test = new ArrayList<>();
        test.add(new MeditationProgram("별 보기",
                "사진 1",
                20,
                15,
                "모집중"));

        return test;
    }

    @PostMapping("/admin/delete/{program_name}/{picture}/{capacity}/{remain}")
    public void program_admin_create(@PathVariable String program_name,
                                     @PathVariable String picture,
                                     @PathVariable int capacity,
                                     @PathVariable int remain){

    }


    @DeleteMapping("/admin/delete/{program_name}")
    public void program_admin_delete(@PathVariable String program_name){

    }

    @PatchMapping("/admin/adjust/{program_name}/{picture}/{capacity}/{remain}")
    public void program_admin_adjust(@PathVariable String program_name,
                                     @PathVariable String picture,
                                     @PathVariable int capacity,
                                     @PathVariable int remain){

    }
}
