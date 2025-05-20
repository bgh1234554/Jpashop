package jpabook.jpashop.service;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {
    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;

    //신규 주문
    //주문과 배달 정보 생성 후 아이디 반환
    @Transactional
    public Long order(Long memberId, Long itemId, int count){
        //회원과 아이템 정보를 DB에서 찾는다
        Member member = memberRepository.findOne(memberId);
        Item item = itemRepository.findOne(itemId);

        //배송 정보를 생성한다.
        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());
        delivery.setStatus(DeliveryStatus.READY);

        //주문 상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(item,item.getPrice(),count);
        //주문 생성 - createOrder 안에 재고 수량 정리가 들어있다.
        Order order = Order.createOrder(member,delivery,orderItem);

        orderRepository.save(order);
        return order.getId();
    }

    //주문 취소
    @Transactional
    public void cancelOrder(Long orderId){
        Order order = orderRepository.findOne(orderId);
        order.cancel();
    }

    public List<Order> findOrders(OrderSearch orderSearch) {
        return orderRepository.findAllByString(orderSearch);
    }
}
/*
주문 서비스는 주문 엔티티와 주문 상품 엔티티의 비즈니스 로직을 활용해서
주문, 주문 취소, 주문 내역 검색 기능을 제공한다.

주문 서비스의 주문과 주문 취소 메서드를 보면 비즈니스 로직 대부분이 엔티티에 있다.
서비스 계층은 단순히 엔티티에 필요한 요청을 위임하는 역할을 한다.
이처럼 엔티티가 비즈니스 로직을 가지고 객체 지향의 특성을 적극 활용하는 것을
도메인 모델 패턴(http://martinfowler.com/eaaCatalog/domainModel.html)이라 한다.

반대로 엔티티에는 비즈니스 로직이 거의 없고
서비스 계층에서 대부분의 비즈니스 로직을 처리하는 것을
트랜잭션 스크립트 패턴(http://martinfowler.com/eaaCatalog/transactionScript.html)이라 한다.
 */