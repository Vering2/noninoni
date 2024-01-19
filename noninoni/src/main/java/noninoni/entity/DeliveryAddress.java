package noninoni.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "delivery_addresses")
public class DeliveryAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id")
    private String memberId; // 사용자 식별자
    
    @Column(name = "nick_name")
    private String nickName; // 사용자 식별자

    @Column(name = "recipient")
    private String recipient; // 수령인

    @Column(name = "address")
    private String address; // 주소

    @Column(name = "phone_number")
    private String phoneNumber; // 연락처

    // 기본 생성자
    public DeliveryAddress() {
    }

    // 모든 필드를 포함하는 생성자
    public DeliveryAddress(String memberId, String nickName,String recipient, String address, String phoneNumber) {
        this.memberId = memberId;
        this.recipient = recipient;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.nickName = nickName;
    }

    // getter 및 setter 메소드
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // 기타 메소드...
}
