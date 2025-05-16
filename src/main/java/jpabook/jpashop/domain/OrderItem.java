package jpabook.jpashop.domain;

import jakarta.persistence.*;
import jpabook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "order_item")
@Getter
@Setter
public class OrderItem {
    @Id @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item; //주문 상품

    private int orderPrice; //주문 가격
    private int count; //주문 수량

    //생성 메서드 - 주문 상품, 가격, 수량 정보를 사용해서 주문상품 엔티티를 생성
    public static OrderItem createOrderItem(Item item, int orderPrice, int count){
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);
        item.removeStock(count);
        return orderItem;
    }

    //비즈니스 로직
    //주문 취소 - 취소한 주문 수량만큼 상품의 재고를 증가
    public void cancel(){
        getItem().addStock(count);
    }

    //주문 가격 조회 - 주문 가격에 수량을 곱한 값을 반환한다
    public int getTotalPrice(){
        return getOrderPrice() * getCount();
    }
}
