package disk;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;

public class DiskManager {
    private String dmDir;
    private int pageSize;
    private int nbPages;
    private final Deque<Integer> freePages = new ArrayDeque<>();
    private RandomAccessFile dataFile;

    public void Init(String dmDir, int pageSize) throws IOException {
        // 1. oublier l'état précédent
        if (dataFile != null) dataFile.close();
        nbPages = 0;
        freePages.clear();

        this.dmDir = dmDir;
        this.pageSize = pageSize;

        // 2. recharger les données si elles existent
        File saveFile = new File(dmDir, "dm.save");
        if (saveFile.exists()) {
            try (DataInputStream in = new DataInputStream(new FileInputStream(saveFile))) {
                int savedPageSize = in.readInt();
                if (savedPageSize != pageSize) {
                    throw new IllegalStateException("pageSize incompatible");
                }
                nbPages = in.readInt();
                int nbFree = in.readInt();
                for (int i = 0; i < nbFree; i++) freePages.add(in.readInt());
            }
        }

        // 3. ouvrir le méga fichier
        dataFile = new RandomAccessFile(new File(dmDir, "data.bin"), "rw");
    }

    public void Save() throws IOException {
        try (DataOutputStream out = new DataOutputStream(
                new FileOutputStream(new File(dmDir, "dm.save")))) {
            out.writeInt(pageSize);
            out.writeInt(nbPages);
            out.writeInt(freePages.size());
            for (int idx : freePages) out.writeInt(idx);
        }
    }

    public IPageId AllocPage() throws IOException {
        if (!freePages.isEmpty()) {
            return new PageId(freePages.poll());
        }
        int idx = nbPages;
        nbPages++;
        dataFile.setLength((long) nbPages * pageSize);
        return new PageId(idx);
    }

    public void ReadPage(IPageId ipid, ByteBuffer buffer) throws IOException {
        int idx = ((PageId) ipid).getPageIdx();
        byte[] tmp = new byte[pageSize];
        dataFile.seek((long) idx * pageSize);
        dataFile.readFully(tmp);
        buffer.clear();
        buffer.put(tmp);
    }

    public void WritePage(IPageId ipid, ByteBuffer buffer) throws IOException {
        int idx = ((PageId) ipid).getPageIdx();
        byte[] tmp = new byte[pageSize];
        buffer.rewind();
        buffer.get(tmp);
        dataFile.seek((long) idx * pageSize);
        dataFile.write(tmp);
    }

    public void DeallocPage(IPageId ipid) {
        freePages.add(((PageId) ipid).getPageIdx());
    }
}