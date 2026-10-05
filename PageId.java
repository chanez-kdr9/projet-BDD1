public class PageId implements IPageId {
    private int pageIndex;

    public PageId(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getPageIndex() {
        return pageIndex;
    }
}