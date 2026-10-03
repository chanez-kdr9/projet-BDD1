package test/disk;

import java.nio.ByteBuffer;
import java.io.File;

public class DiskManagerTests {

    public static void main(String[] args) {
        testEcritureEtLecturePage();
        testReallocationPage();
        System.out.println("Tous les tests ont réussi !");
    }

    public static void testEcritureEtLecturePage() {
        DiskManager dm = new DiskManager();
        String testDir = "./tmp_test_dm";
        int pageSize = 4; // Taille minimale pour un test rapide[cite: 1]

        dm.Init(testDir, pageSize);

        // Allocation et écriture[cite: 1]
        IPageId pid = dm.AllocPage();
        ByteBuffer writeBuffer = ByteBuffer.allocate(pageSize);
        writeBuffer.put(new byte[]{10, 20, 30, 40});

        dm.WritePage(pid, writeBuffer);

        // Lecture et vérification[cite: 1]
        ByteBuffer readBuffer = ByteBuffer.allocate(pageSize);
        dm.ReadPage(pid, readBuffer);

        for (int i = 0; i < pageSize; i++) {
            if (writeBuffer.array()[i] != readBuffer.array()[i]) {
                throw new RuntimeException("Erreur de lecture/écriture à l'index " + i);
            }
        }

        dm.Save();
    }

    public static void testReallocationPage() {
        DiskManager dm = new DiskManager();
        dm.Init("./tmp_test_dm", 4);

        IPageId p1 = dm.AllocPage();
        dm.DeallocPage(p1);
        IPageId p2 = dm.AllocPage();

        // Le DiskManager doit réutiliser la page désallouée[cite: 1]
        if (((PageId) p1).getPageNo() != ((PageId) p2).getPageNo()) {
            throw new RuntimeException("La réallocation de page n'a pas réutilisé l'ancien ID !");
        }
    }
}