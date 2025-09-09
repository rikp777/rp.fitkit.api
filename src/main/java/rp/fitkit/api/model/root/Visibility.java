package rp.fitkit.api.model.root;

/**
 * Defines the visibility levels for entities.
 * This ensures type-safety and prevents typos.
 */
public enum Visibility {
    /**
     * Only visible to the owner.
     */
    PRIVATE,

    /**
     * Visible to the owner and their friends.
     */
    FRIENDS,

    /**
     * Visible to all users of the application.
     */
    GLOBAL
}

