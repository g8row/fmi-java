package bg.sofia.uni.fmi.mjt.cookingcompass.recipe;

public enum MealType {
    BREAKFAST("breakfast"),
    BRUNCH("brunch"),
    LUNCH_DINNER("lunch"),
    SNACK("snack"),
    TEATIME("teatime");

    final String parameter;

    MealType(String parameter) {
        this.parameter = parameter;
    }

    public String getParameter() {
        return parameter;
    }

    public static MealType fromParameter(String parameter) {
        if (parameter.equals("lunch/dinner")) {
            return MealType.LUNCH_DINNER;
        }
        for (MealType type : MealType.values()) {
            if (type.parameter.equals(parameter.toLowerCase())) {
                return type;
            }
        }
        return null;
    }
}
