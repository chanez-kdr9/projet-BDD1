public class DiskManagerTest {

    public static void main(String[] args) {

        DiskManager dm = new DiskManager();

        dm.Init(
                "C:\\Users\\HP\\Desktop\\Projects\\data",
                4
        );

        dm.Save();

        System.out.println("Init et Save fonctionnent !");
    }
}