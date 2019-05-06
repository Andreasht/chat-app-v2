public enum Signal {
    LOGIN("LOGIN"),
    REG("REGISTER"),
    CON("ADDCONTACT"),
    CHECK("CHECKIFONLINE"),
    GET("GETLOG");


    private final String signalType;

    Signal(String t) {
        signalType = t;
    }


    public String getType() {
        return signalType;
    }
}
