import com.f3cinema.app.util.PasswordUtil;
public class generate_hash {
    public static void main(String[] args) {
        System.out.println("HASH: " + PasswordUtil.hash("1"));
    }
}
