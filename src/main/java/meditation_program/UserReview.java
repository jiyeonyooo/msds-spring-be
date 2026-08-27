package meditation_program;

import java.time.LocalDate;

public class UserReview {
    String userid;
    LocalDate review_time;
    String review;

    public UserReview(String userid, LocalDate review_time, String review) {
        this.userid = userid;
        this.review_time = review_time;
        this.review = review;
    }
}
