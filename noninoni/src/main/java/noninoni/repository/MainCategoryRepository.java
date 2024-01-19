package noninoni.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import noninoni.entity.MainCategory;

public interface MainCategoryRepository extends JpaRepository<MainCategory, Long> {
	List<MainCategory> findByType(String type);
}