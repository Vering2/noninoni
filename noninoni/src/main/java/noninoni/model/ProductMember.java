package noninoni.model;

import lombok.Data;
import noninoni.entity.Member;
import noninoni.entity.Product;

@Data
public class ProductMember {
    private Product product;
    private Member member;

    public ProductMember(Product product, Member member) {
        this.product = product;
        this.member = member;
    }

    // getter 및 setter 메서드...
}
