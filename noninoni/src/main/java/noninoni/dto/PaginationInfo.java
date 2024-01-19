package noninoni.dto;

import lombok.Data;

@Data
public class PaginationInfo {
    private int currentPage;
    private int totalPages;
    private long totalItems;

    // 생성자, getter 및 setter
    public PaginationInfo(int currentPage, int totalPages, long totalItems) {
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalItems = totalItems;
    }

    // Getter 및 Setter 메소드
}
