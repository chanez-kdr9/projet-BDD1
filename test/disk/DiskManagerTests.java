package disk;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class DiskManagerTests {

    public static void main(String[] args) throws Exception {
        
        TestEcriturePage();
        TestRealloc();
        TestPersistance();
        
        TestPageIdsDistincts();
        TestDeuxPagesIndependantes();
        TestDonneesApresSave();
        TestPlusieursDealloc();
        TestTailleFichier();
        TestReinitAutreDossier();
    }


    private static String freshDir(String name) {
        File dir = new File(System.getProperty("java.io.tmpdir"), name);
        dir.mkdirs();
        File[] files = dir.listFiles();
        if (files != null) for (File f : files) f.delete();
        return dir.getAbsolutePath();
    }

    private static void check(String test, boolean condition) {
        System.out.println(test + " : " + (condition ? "OK" : "ERREUR"));
    }

    private static ByteBuffer buf(int... values) {
        ByteBuffer bb = ByteBuffer.allocate(values.length);
        for (int v : values) bb.put((byte) v);
        return bb;
    }

    private static byte[] read(DiskManager dm, IPageId p, int size) throws Exception {
        ByteBuffer bb = ByteBuffer.allocate(size);
        dm.ReadPage(p, bb);
        return bb.array();
    }


    static void TestEcriturePage() throws Exception {
        DiskManager dm = new DiskManager();
        dm.Init(freshDir("dmtest1"), 4);
        IPageId p = dm.AllocPage();
        dm.WritePage(p, buf(1, 2, 3, 4));
        check("TestEcriturePage", Arrays.equals(read(dm, p, 4), new byte[]{1, 2, 3, 4}));
    }

    static void TestRealloc() throws Exception {
        DiskManager dm = new DiskManager();
        dm.Init(freshDir("dmtest2"), 4);
        PageId a = (PageId) dm.AllocPage();
        dm.AllocPage();
        dm.DeallocPage(a);
        PageId c = (PageId) dm.AllocPage();
        check("TestRealloc", c.getPageIdx() == 0);
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
        dm2.Init(dir, 4);
        PageId p = (PageId) dm2.AllocPage();
        check("TestPersistance", p.getPageIdx() == 0);
    }


    // Deux allocations successives ne doivent jamais donner la même page
    static void TestPageIdsDistincts() throws Exception {
        DiskManager dm = new DiskManager();
        dm.Init(freshDir("dmtest4"), 4);
        IPageId a = dm.AllocPage();
        IPageId b = dm.AllocPage();
        IPageId c = dm.AllocPage();
        check("TestPageIdsDistincts",
                !a.equals(b) && !a.equals(c) && !b.equals(c));
    }

    // Écrire dans la page 1 ne doit pas modifier la page 0 (ni l'inverse)
    static void TestDeuxPagesIndependantes() throws Exception {
        DiskManager dm = new DiskManager();
        dm.Init(freshDir("dmtest5"), 4);
        IPageId p0 = dm.AllocPage();
        IPageId p1 = dm.AllocPage();

        dm.WritePage(p0, buf(1, 1, 1, 1));
        dm.WritePage(p1, buf(2, 2, 2, 2));
        dm.WritePage(p0, buf(9, 8, 7, 6));

        check("TestDeuxPagesIndependantes",
                Arrays.equals(read(dm, p0, 4), new byte[]{9, 8, 7, 6})
                && Arrays.equals(read(dm, p1, 4), new byte[]{2, 2, 2, 2}));
    }

    // Le contenu des pages doit survivre à Save() puis à un nouvel Init()
    static void TestDonneesApresSave() throws Exception {
        String dir = freshDir("dmtest6");
        DiskManager dm = new DiskManager();
        dm.Init(dir, 4);
        IPageId p0 = dm.AllocPage();
        IPageId p1 = dm.AllocPage();
        dm.WritePage(p0, buf(10, 20, 30, 40));
        dm.WritePage(p1, buf(50, 60, 70, 80));
        dm.Save();

        DiskManager dm2 = new DiskManager();
        dm2.Init(dir, 4);
        check("TestDonneesApresSave",
                Arrays.equals(read(dm2, new PageId(0), 4), new byte[]{10, 20, 30, 40})
                && Arrays.equals(read(dm2, new PageId(1), 4), new byte[]{50, 60, 70, 80}));
    }

    // Plusieurs pages libérées : elles sont toutes réutilisées avant d'agrandir le fichier
    static void TestPlusieursDealloc() throws Exception {
        DiskManager dm = new DiskManager();
        dm.Init(freshDir("dmtest7"), 4);
        IPageId p0 = dm.AllocPage();
        dm.AllocPage();                        // page 1, reste allouée
        IPageId p2 = dm.AllocPage();
        dm.DeallocPage(p0);
        dm.DeallocPage(p2);

        int x = ((PageId) dm.AllocPage()).getPageIdx();
        int y = ((PageId) dm.AllocPage()).getPageIdx();
        int z = ((PageId) dm.AllocPage()).getPageIdx();   // plus de page libre : nouvelle page

        boolean reuse = (x == 0 && y == 2) || (x == 2 && y == 0);
        check("TestPlusieursDealloc", reuse && z == 3);
    }

    // data.bin doit faire exactement nbPages * pageSize octets
    static void TestTailleFichier() throws Exception {
        String dir = freshDir("dmtest8");
        DiskManager dm = new DiskManager();
        dm.Init(dir, 4);
        dm.AllocPage();
        dm.AllocPage();
        dm.AllocPage();
        long taille = new File(dir, "data.bin").length();
        check("TestTailleFichier", taille == 12);   // 3 pages x 4 octets
    }

    // Init sur un nouveau dossier doit oublier l'état de l'ancien dossier
    static void TestReinitAutreDossier() throws Exception {
        DiskManager dm = new DiskManager();
        dm.Init(freshDir("dmtest9a"), 4);
        dm.AllocPage();
        dm.AllocPage();
        dm.AllocPage();

        dm.Init(freshDir("dmtest9b"), 4);
        PageId p = (PageId) dm.AllocPage();
        check("TestReinitAutreDossier", p.getPageIdx() == 0);
    }
}