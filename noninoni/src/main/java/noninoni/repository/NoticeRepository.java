package noninoni.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import noninoni.entity.Notice;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

	List<Notice> findByPinned(boolean b);
    // 기본적인 CRUD 메서드들이 자동으로 제공됩니다.

	Page<Notice> findByPinned(boolean b, Pageable pageable);
}