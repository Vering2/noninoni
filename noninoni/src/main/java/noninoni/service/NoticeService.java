package noninoni.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import noninoni.entity.Notice;
import noninoni.repository.NoticeRepository;

@Service
public class NoticeService {

	private final NoticeRepository noticeRepository;

	@Autowired
	public NoticeService(NoticeRepository noticeRepository) {
		this.noticeRepository = noticeRepository;
	}

	public List<Notice> findAll() {
		return noticeRepository.findAll();
	}

	public Notice findById(Long id) {
		Optional<Notice> noticeOptional = noticeRepository.findById(id);
		if (noticeOptional.isPresent()) {
			return noticeOptional.get();
		} else {
			// 처리 방법은 상황에 따라 달라질 수 있습니다. 예를 들어, 예외를 발생시키거나 null을 반환할 수 있습니다.
			throw new RuntimeException("Notice not found for id: " + id);
		}
	}

	public void save(Notice notice) {
		// 현재 시간을 설정하는 등 필요한 로직을 추가할 수 있습니다.
		notice.setCreatedAt(LocalDateTime.now());

		noticeRepository.save(notice);
	}

	public void update(Long id, Notice notice) {
		// 데이터베이스에서 해당 ID의 Notice 객체를 찾습니다.
		Optional<Notice> existingNotice = noticeRepository.findById(id);

		if (existingNotice.isPresent()) {
			// 찾은 객체에 새로운 내용을 설정합니다.
			Notice updatedNotice = existingNotice.get();
			updatedNotice.setTitle(notice.getTitle());
			updatedNotice.setContent(notice.getContent());
			// 필요한 경우 다른 필드도 업데이트합니다.

			// 변경된 객체를 저장합니다.
			noticeRepository.save(updatedNotice);
		} else {
			// 공지사항이 존재하지 않는 경우, 예외 처리를 할 수 있습니다.
			throw new RuntimeException("Notice not found for id: " + id);
		}
	}

	public void delete(Long id) {
		// 해당 ID의 Notice가 존재하는지 확인
		if (noticeRepository.existsById(id)) {
			noticeRepository.deleteById(id);
		} else {
			// 삭제하려는 공지사항이 존재하지 않는 경우, 예외 처리를 할 수 있습니다.
			throw new RuntimeException("Notice not found for id: " + id);
		}
	}

	public List<Notice> findPinnedNotices() {
		// 상단 고정된 공지사항 조회 로직
		return noticeRepository.findByPinned(true);
	}

	public Page<Notice> findPaginatedNotices(Pageable pageable) {
		// 페이징 처리된 일반 공지사항 조회 로직
		return noticeRepository.findByPinned(false, pageable);
	}

	// 생성자, 공지사항 조회, 저장, 수정, 삭제 메서드 등
}
