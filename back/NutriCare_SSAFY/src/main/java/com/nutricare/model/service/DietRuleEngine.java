package com.nutricare.model.service;

import org.springframework.stereotype.Component;

import com.nutricare.model.dto.DietContext;
import com.nutricare.model.service.CalorieCalculator.CaloriePlan;

/**
 * 질환/건강정보 기반 허용/제한/조건 텍스트를 결정하는 간단한 룰 엔진.
 * LLM은 이 텍스트를 그대로 프롬프트로 소비하며, 정책은 서버에서 결정한다.
 */
@Component
public class DietRuleEngine {

    public static class RuleText {
        private final String allow;
        private final String avoid;
        private final String conditions;

        public RuleText(String allow, String avoid, String conditions) {
            this.allow = allow;
            this.avoid = avoid;
            this.conditions = conditions;
        }

        public String getAllow() { return allow; }
        public String getAvoid() { return avoid; }
        public String getConditions() { return conditions; }

        @Override
        public String toString() {
            return String.format("허용: %s / 제한: %s / 조건: %s", allow, avoid, conditions);
        }
    }

    /**
     * diagnosisName, activity/goal 등의 정보를 기반으로 룰 텍스트를 만든다.
     * 필요 시 정책을 확장할 때 이 메서드만 수정하면 된다.
     */
    public RuleText buildRules(DietContext ctx, CaloriePlan plan) {
        String diagnosis = safeUpper(ctx.getDiagnosisName());
        String goal = ctx.getHealthGoalType();

        String allow = "채소, 살코기 단백질, 통곡물, 수분 충분";
        String avoid = "고당도 디저트, 튀김, 과다 나트륨";

        if ("ACNE".equals(diagnosis)) {
            avoid = "고당 지수 식품, 유제품 과다, 튀김/포화지방";
            allow = "오메가3(등푸른생선), 채소, 저당 통곡물, 물 많이";
        }
        if ("ATOPIC".equals(diagnosis)) {
            avoid = "가공식품, 인스턴트, 지나친 설탕/소금";
            allow = "항염 음식(연어·아보카도·견과), 채소, 물";
        }

        String conditions = "1식 열량은 계획에 맞추고(±50kcal) 단백질/채소를 충분히 포함";
        if (plan != null) {
            conditions = String.format("1식 열량 %.0f~%.0f kcal, 단백질 우선, 채소 충분",
                    plan.getTargetCalories() - 50.0,
                    plan.getTargetCalories() + 50.0);
        }
        if ("LOSE".equalsIgnoreCase(String.valueOf(goal))) {
            conditions += ", 가공당/설탕 음료 금지";
        } else if ("GAIN".equalsIgnoreCase(String.valueOf(goal))) {
            allow += ", 견과·올리브유로 열량 보충";
        }

        return new RuleText(allow, avoid, conditions);
    }

    private String safeUpper(String v) {
        return v == null ? "" : v.toUpperCase();
    }
}
