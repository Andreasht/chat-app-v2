public enum Status {
    ON("Online"),
    OFF("Offline");

    private final String text;

    Status(String t) {
        text = t;
    }

    public String getText() {
        return text;
    }

}
