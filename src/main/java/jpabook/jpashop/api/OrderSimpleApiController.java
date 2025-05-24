package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderSearch;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/*
주문 + 배송정보 + 회원을 조회하는 API

ManyToOne, OneToOne (둘다 지연 로딩)
Order -> Member
Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {
    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    /**
     * V1 - 엔티티를 직접 노출하는 방법
     * 근데 연관관계는 지연로딩이라 프록시 객체가 생성돼 JSON으로 변환할 수 없어,
     * Hibernate5JakartaModule을 스프링 빈으로 등록해야 한다.
     * 그냥 이런거 상관없이 엔티티를 리턴하지 말고, DTO를 반환하게 만들자
     * 양방향 관계 문제 발생 -> @JsonIgnore
     * 엔티티를 직접 노출할 때는 양방향 연관관계가 걸린 곳은
     * 꼭! 한곳을 @JsonIgnore 처리 해야 한다.
     * 안그러면 양쪽을 서로 호출하면서 무한 루프가 걸린다.
     */
    @GetMapping("api/v1/simple-orders")
    public List<Order> ordersV1(){
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for(Order order : all){
            //프록시 객체의 초기화를 위한 for문
            order.getMember().getName();
            order.getDelivery().getAddress();
        }
        return all;
    }

    /**
     * V2 - 엔티티 대신 DTO로 반환 (fetch join 없이)
     * 단점 - 지연 로딩으로 인해 쿼리를 N번 호출하게 된다
     *      쿼리가 총 1 + N + N번 실행된다
     *      order 조회 1번(order 조회 결과 수가 N이 된다.)
     *      order -> member 지연 로딩 조회 N 번
     *      order -> delivery 지연 로딩 조회 N 번
     */
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2(){
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<SimpleOrderDto> result = orders.stream().map(SimpleOrderDto::new).toList();
        return result;
    }

    @Data
    static class SimpleOrderDto{
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order){
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
        }
    }

    /**
     * V3 - 엔티티를 DTO로 변환 with fetch join
     * 쿼리가 한번만 나가기 때문에 성능이 최적화가 된다.
     */
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3(){
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> result = orders.stream().map(SimpleOrderDto::new).toList();
        return result;
    }

    /**
     * V4 - JPA에서 DTO로 바로 조회
     * DTO를 조회하기 위한 Repository가 추가로 필요하다
     */
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4(){
        return orderSimpleQueryRepository.findOrderDtos();
    }
}
/**
 * 쿼리 방식 선택 권장 순서
 * 1. 우선 엔티티를 DTO로 변환하는 방법을 선택한다. (V2 선택)
 * 2. 필요하면 페치 조인으로 성능을 최적화 한다. (V3 선택) 대부분의 성능 이슈가 해결된다.
 * 3. 그래도 안되면 DTO로 직접 조회하는 방법을 사용한다. (V4 선택)
 * 4. 최후의 방법은 JPA가 제공하는 네이티브 SQL이나 스프링 JDBC Template을 사용해서 SQL을 직접 사용한다.
 */