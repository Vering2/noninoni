package noninoni.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import noninoni.entity.Color;

@Repository
public interface ColorRepository extends JpaRepository<Color, Long> {
    // 필요한 경우 추가 메소드를 정의할 수 있습니다.
}