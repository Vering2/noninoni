package noninoni.dto;

import lombok.Data;

@Data
public class PaginationGroupInfo {
    private int startPage;
    private int endPage;
    private boolean hasPrev;
    private boolean hasNext;

    public PaginationGroupInfo(int currentPage, int totalPages) {
        int pageGroupSize = 5; // 한 그룹에 보여줄 페이지 수
        int halfSize = pageGroupSize / 2;

        this.startPage = Math.max(0, currentPage - halfSize);
        this.endPage = Math.min(totalPages - 1, currentPage + halfSize);

        // 시작 페이지가 1보다 크면 이전 페이지 존재
        this.hasPrev = startPage > 1;

        // 끝 페이지가 전체 페이지 수보다 작으면 다음 페이지 존재
        this.hasNext = endPage < totalPages;
    }

    // getters and setters
}