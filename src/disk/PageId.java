package disk;

public class PageId implements IPageId {
    private final int pageIdx;

    public PageId(int pageIdx) { this.pageIdx = pageIdx; }

    public int getPageIdx() { return pageIdx; }

    @Override
    public boolean equals(Object o) {
        return o instanceof PageId p && p.pageIdx == pageIdx;
    }

    @Override
    public int hashCode() { return pageIdx; }
}