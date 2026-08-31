package com.example.meditation.wellness.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "wellness_answers", uniqueConstraints = {
        @UniqueConstraint(name = "uk_wellness_answer_check_question",
                columnNames = {"wellness_check_id", "wellness_question_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WellnessAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wellness_check_id", nullable = false)
    private WellnessCheck wellnessCheck;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wellness_question_id", nullable = false)
    private WellnessQuestion wellnessQuestion;

    @Column(name = "answer_value", nullable = false)
    private Integer answerValue;

    @Column(name = "converted_value", nullable = false)
    private Integer convertedValue;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public WellnessAnswer(WellnessCheck wellnessCheck, WellnessQuestion wellnessQuestion,
                          Integer answerValue, Integer convertedValue) {
        this.wellnessCheck = wellnessCheck;
        this.wellnessQuestion = wellnessQuestion;
        this.answerValue = answerValue;
        this.convertedValue = convertedValue;
    }

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
