package andUtils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

public final class SecurityUtils {
    // instatier SecureRandom objekt, der genererer sikre salte:
    private static final SecureRandom RANDOM = new SecureRandom();

    // definer nødvendige værdier for hashing:
    private static final int ITERATIONS = 20000;    // god iterationsværdi til SHA1
    private static final int KEY_LENGTH = 160;      // god nøglelængde til SHA1
    private static final String ALGORITHM = "PBKDF2WithHmacSHA1";       // algoritmen er SHA1

    // denne metode genererer et salt med den givne længde:
    public static Optional<String> generateSalt(final int length) {
        if(length < 1) {
            System.err.println("Error in generating salt. Length must be > 0");
            return Optional.empty();
        }

        // instantier byte[] med længde-parameteren som længde:
        byte[] salt = new byte[length];

        // fyld ovenstående array med tilfældige bytes:
        RANDOM.nextBytes(salt);

        // return salt som en base64 string for at undgå problemer med text-encoding:
        return Optional.of(Base64.getEncoder().encodeToString(salt));
    }

    public static Optional<String> hashPassword (char[] pass, String salt) {
        // lav det givne salt om til byte[]:
        byte[] saltBytes = salt.getBytes();
        // specificer hvordan kryptografiske "key" skal genereres:
        PBEKeySpec spec = new PBEKeySpec(pass,saltBytes,ITERATIONS,KEY_LENGTH);
        // fyld det indsatte char[] (koden) med null-værdier, så der ikke er et spor af den originale læsbare kode:
        Arrays.fill(pass, Character.MIN_VALUE);
        try {
            // instantier SecretKeyFactory, der hasher med den givne algoritme:
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
            // hash koden vha. keyFactory og spec:
            byte[] hashedPassword = keyFactory.generateSecret(spec).getEncoded();
            // return det genererede hash som en base64 string:
            return Optional.of(Base64.getEncoder().encodeToString(hashedPassword));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.err.println("Exception in hashPassword()");
            //return en tom værdi for Optional objektet, hvis der sker en fejl:
            return Optional.empty();
        } finally {
            // rens "spec" objektet, da det indeholder den originale kode:
            spec.clearPassword();
        }
    }

    // denne metode tjekker om en kode er rigtig:
    public static boolean authenticate(char[] password, String salt, String key) {
        // lav et hash af den givne password, med det givne salt:
        Optional<String> attemptedPasswordHashed = hashPassword(password,salt);
        // check om denne nye hash er den samme som det originale hash, også givet som argument til metoden:
        return attemptedPasswordHashed.map(s -> s.equals(key)).orElse(false);
        // hvis det nye hash er det samme som det originale, vil den indskrevne kode have været den samme.
    }

}
