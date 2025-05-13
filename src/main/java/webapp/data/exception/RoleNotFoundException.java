package webapp.data.exception;


public class RoleNotFoundException extends NotFoundInDatabaseException {
    public RoleNotFoundException(String message) {
        super("Role not found: " + message);
    }
}
