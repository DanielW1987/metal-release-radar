package rocks.metaldetector.persistence.domain.user;

import java.util.HashSet;
import java.util.Set;

/**
 * The enum names must be prefixed with ROLE_.
 */
public enum UserRole {

  ROLE_USER {
    @Override
    public String getName() {
      return getDisplayName().toUpperCase();
    }
    @Override
    public String getDisplayName() {
      return "User";
    }
  },
  ROLE_ADMINISTRATOR {
    @Override
    public String getName() {
      return getDisplayName().toUpperCase();
    }
    @Override
    public String getDisplayName() {
      return "Administrator";
    }
  };

  public abstract String getName();

  public abstract String getDisplayName();

  public static Set<UserRole> createUserRole() {
    Set<UserRole> userRoleSet = new HashSet<>();
    userRoleSet.add(ROLE_USER);

    return userRoleSet;
  }

  public static Set<UserRole> createAdministratorRole() {
    Set<UserRole> userRoleSet = new HashSet<>();
    userRoleSet.add(ROLE_ADMINISTRATOR);

    return userRoleSet;
  }

  public static Set<UserRole> getRoleFromString(String role) {
    if (role.equalsIgnoreCase(ROLE_ADMINISTRATOR.getDisplayName())) {
      return createAdministratorRole();
    }
    else if (role.equalsIgnoreCase(ROLE_USER.getDisplayName())) {
      return createUserRole();
    }
    throw new IllegalArgumentException("Role '" + role + "' not found!");
  }
}
