package com.nutricare.model.dao;

import java.util.List;
import com.nutricare.model.dto.DietResult;

public interface DietResultDao {

    // 1. 특정 추천(rec_id)에 포함된 모든 식단 결과 조회 (List로 변경 권장)
    List<DietResult> selectByRecId(Long recId);

    // 2. 식단 결과 상세 조회 (meal_id로 단건 조회 - 필요시 사용)
    DietResult selectByMealId(Long mealId);

    // 3. 새 diet_result 저장
    int insertDietResult(DietResult dietResult);

    // 4. 식단 정보 수정
    int updateDietResult(DietResult dietResult);

    // 5. 식단 삭제 (meal_id 기준)
    int deleteDietResult(Long mealId);
    
    // 6. 추천 ID로 전체 식단 삭제 (rec_id 기준 - 재설계 시 유용)
    int deleteByRecId(Long recId);
}