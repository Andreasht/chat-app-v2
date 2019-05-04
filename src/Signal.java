public enum Signal {
    LOGIN("LOGIN"),
    REG("REGISTER");


    private final String signalType;

    Signal(String t) {
        signalType = t;
    }


    public String getType() {
        return signalType;
    }
}
