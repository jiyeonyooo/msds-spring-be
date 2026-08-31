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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "wellness_checks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WellnessCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "resv_id")
    private Long reservationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "survey_id", nullable = false)
    private WellnessSurvey survey;

    @Enumerated(EnumType.STRING)
    @Column(name = "stay_stage", nullable = false, length = 20)
    private StayStage stayStage;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "result_level", nullable = false, length = 20)
    private WellnessLevel resultLevel;

    @Column(name = "checked_at", nullable = false)
    private LocalDateTime checkedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public WellnessCheck(Long memberId, Long reservationId, WellnessSurvey survey,
                         StayStage stayStage, Integer totalScore, WellnessLevel resultLevel) {
        this.memberId = memberId;
        this.reservationId = reservationId;
        this.survey = survey;
        this.stayStage = stayStage;
        this.totalScore = totalScore;
        this.resultLevel = resultLevel;
        this.checkedAt = LocalDateTime.now();
    }

    @PrePersist
    private void onCreate() {
        if (checkedAt == null) checkedAt = LocalDateTime.now();
        createdAt = LocalDateTime.now();
    }
}
