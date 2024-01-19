package noninoni.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import noninoni.entity.Cart;
import noninoni.entity.Member;

public interface CartRepository extends JpaRepository<Cart, Long> {

	Cart findByMember(Member member);
}