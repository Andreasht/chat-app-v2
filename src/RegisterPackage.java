import java.io.Serializable;
import java.util.ArrayList;

class RegisterPackage implements Serializable {

    private ArrayList<String> data;

    RegisterPackage(String nameIn, String saltIn, String hashIn) {
        data = new ArrayList<>();
        data.add(nameIn);
        data.add(saltIn);
        data.add(hashIn);
    }

    public ArrayList<String> getData() {
        return data;
    }
}
