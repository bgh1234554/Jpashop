package jpabook.jpashop.api;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.repository.OrderRepository;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/*
OrderSimpleApiController에서 Order와 OneToMany 관계인
OrderItems가 들어가 컬렉션을 조회하게 될 때
성능 최적화를 하는 방법을 알아보자
 */
@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;

    /**
     * V1. 엔티티 직접 노출 (당연히 쓰면 안되는거)
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1(){
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for(Order order : all){
            //LAZY 로딩이기 때문에 강제로 초기화를 해준다.
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o->o.getItem().getName());
        }
        return all;
    }

    /**
     * V2 - 엔티티 대신 DTO 사용 (페치 조인 미사용)
     * OrderSimpleApiController처럼 N+1 쿼리 문제가 발생한다
     */
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2(){
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> result = orders.stream()
                .map(OrderDto::new).toList();
        return result;
    }

    @Data
    static class OrderDto{
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems; //OrderItem에 대해서도 Dto를 만든다

        public OrderDto(Order order){
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            //orderItem에 넣을 때도 Dto로 형식을 맞춰서 집어넣는다.
            orderItems = order.getOrderItems().stream()
                    .map(OrderItemDto::new)
                    .toList();
        }

        @Data
        static class OrderItemDto{
            private String itemName;
            private int orderPrice;
            private int count;

            public OrderItemDto(OrderItem orderItem){
                itemName = orderItem.getItem().getName();
                orderPrice = orderItem.getOrderPrice();
                count = orderItem.getCount();
            }
        }
    }

    /**
     * V3 - DTO와 fetch join 동시 이용
     * 페치 조인으로 SQL이 1번만 실행됨
     * 다만 페이징이 불가능하다는 단점이 있다.
     * 또한 컬렉션 페치 조인은 1개만 사용할 수 있다.
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3(){
        List<Order> orders = orderRepository.findAllWithItem();
        List<OrderDto> result = orders.stream()
                .map(OrderDto::new).toList();
        return result;
    }

    /**
     * V3.1 - 페이징과 한계 돌파
     * 페치 조인 시 일대다 조인이 발생하기 대문에 데이터가 예측할 수도 없이 증가하고,
     * 일대다에서 다를 기준으로 row가 생성되기 때문에 페이징이 불가능하다.
     *
     * 페이징 + 컬렉션 엔티티를 함께 조회하려면?
     * 1. xxToOne 관계를 모두 fetch join한다.
     * 2. 컬렉션은 지연 로딩으로 조회한다. (기본으로 다 세팅되어있으니까)
     * 3. 지연 로딩 성능 최적화를 위해 컬렉션 관계는
     *    hibernate.default_batch_fetch_size , @BatchSize 를 적용한다.
     *      * hibernate.default_batch_fetch_size: 글로벌 설정
     *      * @BatchSize: 개별 최적화
     *      * 이 옵션을 사용하면 컬렉션이나, 프록시 객체를 한꺼번에 설정한 size 만큼 IN 쿼리로 조회한다.
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> orderV3_page(@RequestParam(value="offset",defaultValue = "0") int offset,
                                       @RequestParam(value="limit",defaultValue = "100") int limit){
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset,limit);
        List<OrderDto> result = orders.stream()
                .map(OrderDto::new).toList();
        /*
        여기서 매핑을 할 때 OrderDto 생성자에서 OrderItemsDto를 호출할 때,
        Hibernate가 설정된 batch size 만큼 묶어서 쿼리를 보낸다.
        이렇게 하면 1+N개의 쿼리가 1+1 수준으로 최적화된다.
        =>  컬렉션 fetch join은 페이징에 문제를 일으키지만,
            Lazy + BatchSize는 페이징도 되고 성능도 나쁘지 않다.
         */
        return result;
    }
    /*
    BatchSize관련 질문
    1. BatchSize의 동작 방식
        BatchSize(size = 100) 또는 hibernate.default_batch_fetch_size: 100 이 설정된 경우

        만약 Order가 230개이고, 각 Order에 있는 OrderItem들을 조회해야 한다면,
        JPA는 내부적으로 Order.getOrderItems()를 호출하는 시점에 지연로딩이 한 번에 처리되도록 IN 쿼리를 날려.

        -- 1차 요청 (id 1~100)
        SELECT * FROM order_item WHERE order_id IN (1, 2, ..., 100);

        -- 2차 요청 (id 101~200)
        SELECT * FROM order_item WHERE order_id IN (101, 102, ..., 200);

        -- 3차 요청 (id 201~230)
        SELECT * FROM order_item WHERE order_id IN (201, 202, ..., 230);

        위 예시의 경우에는 총 4번의 쿼리만 날라가게 된다.
        (하이버네이트 6.2 부터는 where in 대신에 array_contains 를 사용한다.)

     2. BatchSize가 너무 크면 안되는 이유?
        2-1. IN 절이 너무 길어져서 쿼리 성능 저하
            IN (...)에 ID가 1,000개, 10,000개가 들어가면 SQL 쿼리 문자열이 길어지고,
            DB가 파싱하거나 실행 계획을 수립하는 데 시간이 오래 걸릴 수 있어.
            일부 DB는 IN 절에 허용되는 최대 개수 제한이 있을 수도 있어 (예: Oracle 1000개 제한).

        2-2. 너무 많은 데이터를 한꺼번에 가져오면 메모리 부담
            예를 들어 batch size: 10000이면,
            한 번의 쿼리로 10000개의 연관 객체들이 한꺼번에 메모리에 로딩됨.
            컬렉션 로딩 → 연관된 엔티티 생성
            → 영속성 컨텍스트 저장 → GC 부담 & OutOfMemory 가능성 증가.

        2-3. 네트워크 전송량 증가
            DB ↔ WAS 간 네트워크로 전송되는 데이터가 많아져서 병목이 생길 수 있어.
            특히 JSON API 응답이라면 대량의 직렬화로 인해 응답 속도가 급격히 느려짐.

     3. @BatchSize를 붙이는 위치
        연관 필드에 붙이는게 맞다. 해당 엔티티를 프록시로 불러올 일이 많을 경우에는,
        클래스 위에 일괄적으로 @BatchSize를 붙이면 전체에 일괄 적용된다.

        @Entity
        public class Order {

            @Id @GeneratedValue
            private Long id;

            @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
            @BatchSize(size = 100) //바로 여기에 붙이면 된다.
            private List<OrderItem> orderItems = new ArrayList<>();

            // ...
        }
        | 붙이는 위치                    | 설명                                                     |
        | ------------------------- | ------------------------------------------------------ |
        | `@OneToMany` 연관 필드        | 컬렉션(batch로 묶어서 조회됨)                                    |
        | `@ManyToOne`, `@OneToOne` | 지연 로딩 프록시를 여러 개 한 번에 조회 가능 (예: `member`, `delivery` 등) |
        | 클래스 위                     | 해당 엔티티의 **모든 지연 로딩 필드**에 적용됨                           |

     */
}
