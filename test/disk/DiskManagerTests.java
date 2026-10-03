package disk;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class DiskManagerTests {

    public static void main(String[] args) throws Exception {
        TestEcriturePage();
        TestRealloc();
        TestPersistance();
    }

    private static String freshDir(String name) {
        File dir = new File(System.getProperty("java.io.tmpdir"), name);
        dir.mkdirs();
        File[] files = dir.listFiles();
        if (files != null) for (File f : files) f.delete();
        return dir.getAbsolutePath();
    }

    static void TestEcriturePage() throws Exception {
        DiskManager dm = new DiskManager();
        dm.Init(freshDir("dmtest1"), 4);
        IPageId p = dm.AllocPage();

        ByteBuffer w = ByteBuffer.allocate(4);
        w.put(new byte[]{1, 2, 3, 4});
        dm.WritePage(p, w);

        ByteBuffer r = ByteBuffer.allocate(4);
        dm.ReadPage(p, r);
        System.out.println("TestEcriturePage : " + Arrays.toString(r.array())); // [1, 2, 3, 4]
    }

    static void TestRealloc() throws Exception {
        DiskManager dm = new DiskManager();
        dm.Init(freshDir("dmtest2"), 4);
        PageId a = (PageId) dm.AllocPage();   // page 0
        dm.AllocPage();                       // page 1
        dm.DeallocPage(a);
        PageId c = (PageId) dm.AllocPage();   // doit réutiliser la page 0
        System.out.println("TestRealloc : " + (c.getPageIdx() == 0 ? "OK" : "ERREUR"));
    }

    static void TestPersistance() throws Exception {
        String dir = freshDir("dmtest3");
        DiskManager dm = new DiskManager();
        dm.Init(dir, 4);
        dm.AllocPage();
        dm.AllocPage();
        dm.DeallocPage(new PageId(0));
        dm.Save();

        DiskManager dm2 = new DiskManager();
        dm2.Init(dir, 4);                     // recharge dm.save
        PageId p = (PageId) dm2.AllocPage();  // doit donner 0
        System.out.println("TestPersistance : " + (p.getPageIdx() == 0 ? "OK" : "ERREUR"));
    }
}