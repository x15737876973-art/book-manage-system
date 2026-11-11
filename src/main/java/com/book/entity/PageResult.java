package com.book.entity;

import java.util.List;

/**
 * 分页结果封装。
 *
 * <p>包含当前页数据与全部翻页所需的元信息，JSP 直接用 EL 取值即可：</p>
 * <pre>
 *   ${page.records}      当前页数据
 *   ${page.pageNumber}   当前页码（从 1 开始）
 *   ${page.totalPages}   总页数
 *   ${page.totalRecords} 总记录数
 *   ${page.hasNext}      是否有下一页
 * </pre>
 *
 * @param <T> 记录类型
 */
public class PageResult<T> {

    /** 当前页数据 */
    private final List<T> records;
    /** 当前页码，从 1 开始 */
    private final int pageNumber;
    /** 每页条数 */
    private final int pageSize;
    /** 总记录数 */
    private final long totalRecords;

    public PageResult(List<T> records, int pageNumber, int pageSize, long totalRecords) {
        this.records = records;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalRecords = totalRecords;
    }

    /** 空分页结果 */
    public static <T> PageResult<T> empty(int pageNumber, int pageSize) {
        return new PageResult<>(java.util.Collections.emptyList(), pageNumber, pageSize, 0);
    }

    public List<T> getRecords() {
        return records;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public long getTotalRecords() {
        return totalRecords;
    }

    /** 总页数，至少为 1 */
    public int getTotalPages() {
        if (pageSize <= 0) {
            return 1;
        }
        int pages = (int) ((totalRecords + pageSize - 1) / pageSize);
        return Math.max(pages, 1);
    }

    /** 是否有上一页 */
    public boolean isHasPrevious() {
        return pageNumber > 1;
    }

    /** 是否有下一页 */
    public boolean isHasNext() {
        return pageNumber < getTotalPages();
    }

    /** 上一页页码（已做边界保护） */
    public int getPreviousPage() {
        return Math.max(pageNumber - 1, 1);
    }

    /** 下一页页码（已做边界保护） */
    public int getNextPage() {
        return Math.min(pageNumber + 1, getTotalPages());
    }

    /** 当前页是否为空 */
    public boolean isEmpty() {
        return records == null || records.isEmpty();
    }

    /** 当前页第一条记录的序号（从 1 开始），用于"第 x-y 条"展示 */
    public long getStartIndex() {
        return totalRecords == 0 ? 0 : (long) (pageNumber - 1) * pageSize + 1;
    }

    /** 当前页最后一条记录的序号 */
    public long getEndIndex() {
        return Math.min((long) pageNumber * pageSize, totalRecords);
    }
}