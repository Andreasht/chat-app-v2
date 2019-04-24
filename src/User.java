import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class User implements java.io.Serializable {
    static int antalBrugere;
    private char[] kodeord;
    private String brugernavn;
    static final String DEFAULT_UN = "admin";
    static final char[] DEFUALT_PASS = {'a','d','m','i','n'};

    User() {
        brugernavn = DEFAULT_UN;
        kodeord = DEFUALT_PASS;
    }
    User(String bN /*, char[] k */) {
        brugernavn = bN;
     //   kodeord = k;
        antalBrugere++;
    }

    public String getBrugernavn() {
        return brugernavn;
    }

    boolean authenticate(char[] kodeIn) {
        return Arrays.equals(this.kodeord, kodeIn);
    }

    public static void main(String[] args) {

    }
}