package main.model;

public enum DiscountType {
    FIXED("Fixed"),
    PERCENT("Percent");

    private final String displayName;

    public String getDisplayName() {
        return displayName;
    }

    DiscountType(String displayName) {
        this.displayName = displayName;
    }
}
