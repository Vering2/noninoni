package noninoni.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Cart {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "cart", fetch = FetchType.EAGER)
	private List<CartItem> items = new ArrayList<>();

	@OneToOne
	@JoinColumn(name = "member_id")
	@JsonManagedReference
	private Member member;

	public void addItem(CartItem item) {
		items.add(item);
		item.setCart(this); // 양방향 관계 설정
	}

	public void removeItem(CartItem item) {
		items.remove(item);
		item.setCart(null);
	}

	@Override
	public String toString() {
		return "Cart{" + "id=" + id + ", member=" + (member != null ? member.getMemberId() : "null") + // member의 ID만 출력
				'}';
	}

	// 필요한 경우 equals()와 hashCode()도 오버라이드
}
