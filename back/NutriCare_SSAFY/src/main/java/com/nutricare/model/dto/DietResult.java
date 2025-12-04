package com.nutricare.model.dto;

public class DietResult {

    private Long mealId;      // PK
    private Long recId;       // FK: diet_recommendation.rec_id
    private String menuName;  // 메뉴 이름
    private String description; // 설명
    private Integer calories; // 칼로리
    private String notes;     // 기타 메모
    private String recipeUrl;   // 레시피 URL
    private String skincareUrl; // 피부 관리법 URL

    public DietResult() {
    }

    // 필요하면 생성자 추가 가능
    public DietResult(Long recId, String menuName, String description,
                      Integer calories, String notes,
                      String recipeUrl, String skincareUrl) {
        this.recId = recId;
        this.menuName = menuName;
        this.description = description;
        this.calories = calories;
        this.notes = notes;
        this.recipeUrl = recipeUrl;
        this.skincareUrl = skincareUrl;
    }

    public Long getMealId() { return mealId; }
    public void setMealId(Long mealId) { this.mealId = mealId; }

    public Long getRecId() { return recId; }
    public void setRecId(Long recId) { this.recId = recId; }

    public String getMenuName() { return menuName; }
    public void setMenuName(String menuName) { this.menuName = menuName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getCalories() { return calories; }
    public void setCalories(Integer calories) { this.calories = calories; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getRecipeUrl() { return recipeUrl; }
    public void setRecipeUrl(String recipeUrl) { this.recipeUrl = recipeUrl; }

    public String getSkincareUrl() { return skincareUrl; }
    public void setSkincareUrl(String skincareUrl) { this.skincareUrl = skincareUrl; }
}
