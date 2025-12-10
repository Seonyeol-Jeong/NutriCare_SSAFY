package com.nutricare.model.service;

import com.nutricare.model.dto.HealthProfile;

/**
 * BMR/TDEE/목표 칼로리 계산 유틸.
 * - 기본 BMR: Mifflin-St Jeor (성별 없으면 남성 기준, 나이는 기본 30세)
 * - 활동계수: LOW=1.2, MEDIUM=1.55, HIGH=1.725 (기본 1.2)
 * - 목표 보정: LOSE=-500kcal, MAINTAIN=0kcal, GAIN=+300kcal
 * - 목표 칼로리 하한선: 1200kcal
 */
public final class CalorieCalculator {

    private CalorieCalculator() {}

    public static class CaloriePlan {
        private final int bmr;
        private final int tdee;
        private final int targetCalories;
        private final double activityFactor;
        private final int ageUsed;
        private final String genderUsed;
        private final String goalType;

        public CaloriePlan(int bmr, int tdee, int targetCalories, double activityFactor,
                           int ageUsed, String genderUsed, String goalType) {
            this.bmr = bmr;
            this.tdee = tdee;
            this.targetCalories = targetCalories;
            this.activityFactor = activityFactor;
            this.ageUsed = ageUsed;
            this.genderUsed = genderUsed;
            this.goalType = goalType;
        }

        public int getBmr() { return bmr; }
        public int getTdee() { return tdee; }
        public int getTargetCalories() { return targetCalories; }
        public double getActivityFactor() { return activityFactor; }
        public int getAgeUsed() { return ageUsed; }
        public String getGenderUsed() { return genderUsed; }
        public String getGoalType() { return goalType; }
    }

    public static CaloriePlan calculate(HealthProfile profile, Integer ageYears, String gender) {
        if (profile == null) {
            throw new IllegalArgumentException("healthProfile이 없습니다.");
        }
        Double weightKg = profile.getWeightKg();
        Double heightCm = profile.getHeightCm();
        if (weightKg == null || heightCm == null) {
            throw new IllegalArgumentException("몸무게 또는 키가 없습니다.");
        }

        int age = (ageYears != null && ageYears > 0) ? ageYears : 30; // 기본 30세
        String genderNorm = (gender != null) ? gender.toUpperCase() : "MALE";
        double genderOffset = "FEMALE".equals(genderNorm) ? -161 : 5;

        // Mifflin-St Jeor
        double bmrRaw = 10 * weightKg + 6.25 * heightCm - 5 * age + genderOffset;
        int bmr = (int) Math.round(bmrRaw);

        double activityFactor = resolveActivityFactor(profile.getActivityLevel());
        int tdee = (int) Math.round(bmr * activityFactor);

        int adjustment = resolveGoalAdjustment(profile.getGoalType());
        int targetCalories = Math.max(1200, tdee + adjustment);

        return new CaloriePlan(bmr, tdee, targetCalories, activityFactor, age, genderNorm, profile.getGoalType());
    }

    private static double resolveActivityFactor(String activityLevel) {
        if (activityLevel == null) return 1.2;
        switch (activityLevel.toUpperCase()) {
            case "MEDIUM":
                return 1.55;
            case "HIGH":
                return 1.725;
            case "LOW":
            default:
                return 1.2;
        }
    }

    private static int resolveGoalAdjustment(String goalType) {
        if (goalType == null) return 0;
        switch (goalType.toUpperCase()) {
            case "LOSE":
                return -500;
            case "GAIN":
                return 300;
            case "MAINTAIN":
            default:
                return 0;
        }
    }
}
