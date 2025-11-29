package online.sevika.tm.entity.enums;

/**
 * Enum representing different roles a user can have in a team
 */
public enum TeamRole {
    OWNER,    // Full control over team, can delete team
    ADMIN,    // Can manage team members and settings
    MEMBER,   // Can view and contribute to team projects
    VIEWER    // Read-only access to team resources
}
