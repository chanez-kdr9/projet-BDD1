package disk;


import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class DiskManager {
    private String dmDir;
    private int pageSize;
    private int pageCount;
    private Stack<Integer> freePages;
    private RandomAccessFile dbFile;

    public void Init(String dmDir, int pageSize) {
        this.dmDir = dmDir;
        this.pageSize = pageSize;
        this.freePages = new Stack<>();
        
        File dir = new File(dmDir);
        File metaFile = new File(dir, "dm.meta");
        File dataFile = new File(dir, "db.bin");

        // Réinitialisation / Effacement si changement de dossier[cite: 1]
        try {
            if (metaFile.exists() && dataFile.exists()) {
                // Charger les métadonnées existantes[cite: 1]
                try (DataInputStream dis = new DataInputStream(new FileInputStream(metaFile))) {
                    int savedPageSize = dis.readInt();
                    if (savedPageSize != pageSize) {
                        throw new IllegalArgumentException("Incompatibilité de pageSize avec les données existantes");
                    }
                    this.pageCount = dis.readInt();
                    int freePagesCount = dis.readInt();
                    for (int i = 0; i < freePagesCount; i++) {
                        this.freePages.push(dis.readInt());
                    }
                }
            } else {
                // Création du dossier et initialisation à zéro[cite: 1]
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                this.pageCount = 0;
            }

            this.dbFile = new RandomAccessFile(dataFile, "rw");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Save() {
        File metaFile = new File(dmDir, "dm.meta");
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(metaFile))) {
            dos.writeInt(pageSize);
            dos.writeInt(pageCount);
            dos.writeInt(freePages.size());
            for (Integer pageNo : freePages) {
                dos.writeInt(pageNo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public IPageId AllocPage() {
        int pageNo;
        if (!freePages.isEmpty()) {
            pageNo = freePages.pop(); // Réutilisation[cite: 1]
        } else {
            pageNo = pageCount++;     // Nouvelle page[cite: 1]
        }
        return new PageId(pageNo);
    }

    public void ReadPage(IPageId ipid, ByteBuffer buffer) {
        PageId pid = (PageId) ipid;
        long offset = (long) pid.getPageNo() * pageSize;
        try {
            dbFile.seek(offset);
            dbFile.readFully(buffer.array(), buffer.arrayOffset(), pageSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void WritePage(IPageId ipid, ByteBuffer buffer) {
        PageId pid = (PageId) ipid;
        long offset = (long) pid.getPageNo() * pageSize;
        try {
            dbFile.seek(offset);
            dbFile.write(buffer.array(), buffer.arrayOffset(), pageSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void DeallocPage(IPageId ipid) {
        PageId pid = (PageId) ipid;
        freePages.push(pid.getPageNo());
    }
}