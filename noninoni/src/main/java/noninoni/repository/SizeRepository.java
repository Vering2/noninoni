package noninoni.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import noninoni.entity.Size;


@Repository
public interface SizeRepository extends JpaRepository<Size, Long> {


}