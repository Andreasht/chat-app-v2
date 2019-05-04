class ExistingUserException extends  RuntimeException {
    public ExistingUserException() {
        super("A user with this name already exists in the database");
    }
}
