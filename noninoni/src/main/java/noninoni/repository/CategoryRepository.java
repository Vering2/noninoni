package noninoni.repository;

import noninoni.entity.Category;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
public interface CategoryRepository extends JpaRepository<Category, Long> {
	List<Category> findByType(String type);
}