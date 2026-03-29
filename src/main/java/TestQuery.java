import com.f3cinema.app.service.impl.InventoryServiceImpl;

public class TestQuery {
    public static void main(String[] args) {
        try {
            System.out.println("Starting test query...");
            InventoryServiceImpl.getInstance().getAllInventory();
            System.out.println("Success!");
        } catch (Exception e) {
            System.err.println("Exception caught!");
            e.printStackTrace();
            if (e.getCause() != null) {
                System.err.println("Cause:");
                e.getCause().printStackTrace();
            }
        }
        System.exit(0);
    }
}
