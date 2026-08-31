package com.example.meditation.wellness.entity;

public enum WellnessLevel {
    VERY_RELAXED("매우 편안", "지금은 몸과 마음이 비교적 편안해 보여요. 이 여유를 천천히 즐겨보세요."),
    RELAXED("편안", "전반적으로 안정적인 상태예요. 짧은 산책이나 명상으로 여유를 이어가 보세요."),
    NORMAL("보통", "조금 지쳐 있을 수 있어요. 잠시 일상에서 벗어나 쉬어가는 시간을 가져보세요."),
    TIRED("지침", "몸과 마음에 휴식이 필요한 것 같아요. 오늘은 조금 천천히 보내보는 건 어떨까요?"),
    VERY_TIRED("매우 지침", "현재 피로와 부담을 크게 느끼고 있는 것 같아요. 무리하지 말고 충분한 휴식을 가져보세요.");

    private final String label;
    private final String message;

    WellnessLevel(String label, String message) {
        this.label = label;
        this.message = message;
    }

    public String getLabel() {
        return label;
    }

    public String getMessage() {
        return message;
    }

    public static WellnessLevel fromScore(int score) {
        if (score <= 20) return VERY_RELAXED;
        if (score <= 40) return RELAXED;
        if (score <= 60) return NORMAL;
        if (score <= 80) return TIRED;
        return VERY_TIRED;
    }
}
