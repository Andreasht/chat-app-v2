class NoUserException extends RuntimeException {
    public NoUserException() {
        super("No user with the entered name was found in the database");
    }
}
