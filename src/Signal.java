public enum Signal {
    LOGIN("LOGIN"),
    REG("REGISTER"),
    CON("ADDCONTACT"),
    TEST("TEST");


    private final String signalType;

    Signal(String t) {
        signalType = t;
    }


    public String getType() {
        return signalType;
    }
}
