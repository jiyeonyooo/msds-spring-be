package com.example.meditation.wellness.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "wellness_questions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_wellness_question_order", columnNames = {"survey_id", "display_order"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WellnessQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "survey_id", nullable = false)
    private WellnessSurvey survey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WellnessCategory category;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "is_reverse_scored", nullable = false)
    private boolean reverseScored;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public WellnessQuestion(WellnessSurvey survey, WellnessCategory category, String content,
                            Integer displayOrder, boolean reverseScored, QuestionStatus status) {
        this.survey = survey;
        this.category = category;
        this.content = content;
        this.displayOrder = displayOrder;
        this.reverseScored = reverseScored;
        this.status = status;
    }

    public int convert(int answerValue) {
        return reverseScored ? 4 - answerValue : answerValue;
    }

    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
