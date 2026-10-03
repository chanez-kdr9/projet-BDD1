package disk;

import java.util.Objects;

public class PageId implements IPageId {
    private final int pageNo;

    public PageId(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageNo() {
        return pageNo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PageId pageId = (PageId) o;
        return pageNo == pageId.pageNo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageNo);
    }
}