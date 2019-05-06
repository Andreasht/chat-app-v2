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
    // declare SecureRandom object for generating salts:
    private static final SecureRandom RANDOM = new SecureRandom();

    // define the needed values for hashing the passwords:
    private static final int ITERATIONS = 20000;    // good iteration value for SHA1
    private static final int KEY_LENGTH = 160;      // good length for SHA1
    private static final String ALGORITHM = "PBKDF2WithHmacSHA1";       // use SHA1 because SHA512 is overkill for this shit

    public static Optional<String> generateSalt(final int length) {
        if(length < 1) {
            System.err.println("Error in generating salt. Length must be > 0");
            return Optional.empty();
        }

        // create bytearray with given length:
        byte[] salt = new byte[length];

        // randomise the bytes to create the salt:
        RANDOM.nextBytes(salt);

        // return salt as base64 string so we are safe from encoding shenanigans
        return Optional.of(Base64.getEncoder().encodeToString(salt));
    }

    public static Optional<String> hashPassword (char[] pass, String salt) {
        // get salt as byte array
        byte[] saltBytes = salt.getBytes();

        // specify how to generate the hashed password (what length, salt, iterations and the original pass)
        PBEKeySpec spec = new PBEKeySpec(pass,saltBytes,ITERATIONS,KEY_LENGTH);

        // clear the password array so no trace of plaintext password is left:
        Arrays.fill(pass, Character.MIN_VALUE);

        try {
            // define how to generate the key used for hashing
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);

            // generate the hashed password using the key:
            byte[] hashedPassword = keyFactory.generateSecret(spec).getEncoded();

            // return the hashed password base64 encoded so nothing can fuck up in encoding
            return Optional.of(Base64.getEncoder().encodeToString(hashedPassword));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.err.println("Exception in hashPassword()");

            //return empty value if exception happens
            return Optional.empty();
        } finally {
            // clear the key spec because it holds ALL THE INFO
            spec.clearPassword();
        }
    }

    public static boolean authenticate(char[] password, String salt, String key) {
        // hash the attempted password
        Optional<String> attemptedPasswordHashed = hashPassword(password,salt);
        // check if the attempted hash matches the correct hash
        return attemptedPasswordHashed.map(s -> s.equals(key)).orElse(false);
    }

}
