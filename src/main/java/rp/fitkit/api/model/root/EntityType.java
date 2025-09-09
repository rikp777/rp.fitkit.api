package rp.fitkit.api.model.root;

public enum EntityType {
    /**
     * A section within a daily log (e.g., morning, afternoon).
     */
    LOG_SECTION,

    /**
     * An entire daily log for a specific date.
     */
    DAILY_LOG,

    // Future entity types can be added here, for example:
     PERSON,
    // LOCATION,
    // EXERCISE,
    // FOOD_ITEM
}
