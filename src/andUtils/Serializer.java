package andUtils;


import java.io.*;

public final class Serializer {
    public static void serialize(Object obj, String path) {
        try {
            FileOutputStream fileOut = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(obj);
            out.close();
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static Object deserialize(String path) throws ClassNotFoundException {
        try {
            FileInputStream fileIn = new FileInputStream(path);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            Object out = in.readObject();
            in.close();
            fileIn.close();
            return out;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
