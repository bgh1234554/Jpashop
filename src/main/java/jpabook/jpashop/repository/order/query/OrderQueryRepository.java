package jpabook.jpashop.repository.order.query;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//엔티티가 아닌 특정 화면과 쿼리에 맞게 조회를 하기 위해 따로 생성
@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    /**
     * V4용 메서드들 DTO 직접 조회
     * 컬렉션은 별도로 조회
     * Query: 루트 1번, 컬렉션 N 번
     * 단건 조회에서 많이 사용하는 방식
     * ToOne 관계는 조인으로 최적화 하기 쉬우므로 한번에 조회하고,
     * ToMany 관계는 최적화 하기 어려우므로 findOrderItems() 같은 별도의 메서드로 조회한다.
     *
     * DTO가 두개 필요하니까 각각 쿼리용 DTO를 만들고, 각각 최적화한 이후에 합쳐서 조회한다.
     */
    public List<OrderQueryDto> findOrderQueryDtos() {
        //toOne 코드를 한번에 조회
        List<OrderQueryDto> result = findOrders(); //쿼리 한번에 -> 루프 N번 : N+1 문제 발생

        //각 OrderQueryDto에 OrderItemQueryDto를 추가
        //forEach문을 돌 때마다 새로운 쿼리가 나가게 된다.
        result.forEach(o->{
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
            o.setOrderItems(orderItems);
        });
        return result;
    }

    /**
     * orderItems 빼고 조회
     * JPQL에서 컬렉션을 바로 넣을 수가 없다. 바로 new를 통해 집어넣을 수밖에 없기 때문이다.
     */
    private List<OrderQueryDto> findOrders() {
        return em.createQuery("select new" +
                        " jpabook.jpashop.repository.order.query.OrderQueryDto" +
                "(o.id, m.name, o.orderDate, o.status, d.address)" +
                " from Order o" +
                " join o.member m" +
                " join o.delivery d", OrderQueryDto.class)
                .getResultList();
    }

    /**
     * orderItems 조회
     * OrderItem과 Item은 toOne관계라 조인해도 데이터가 늘어나지 않으니까 JOIN
     */
    private List<OrderItemQueryDto> findOrderItems(Long orderId){
        return em.createQuery(
                "select new" +
                        " jpabook.jpashop.repository.order.query.OrderItemQueryDto" +
                        "(oi.order.id, i.name, oi.orderPrice, oi.count)"+
                        " from OrderItem oi"+
                        " join oi.item i"+
                        " where oi.order.id = :orderId",
                OrderItemQueryDto.class)
                .setParameter("orderId",orderId)
                .getResultList();
    }

    /**
     * 최적화 버전 (V5)
     * Query는 order에 대해 한번, 컬렉션에 대해 한번
     * 데이터를 한꺼번에 처리할 때 사용하는 방식이다.
     */
    public List<OrderQueryDto> findAllByDto_optimization() {
        //toOne 코드를 한번에 조회 (이건 V4와 동일)
        List<OrderQueryDto> result = findOrders();

        //루프 대신에 쿼리를 통해 한방에 가져오기 위해서 Map을 사용한 것이다.
        //이후 해당 주문의 ID들에 대한
        //orderItem 컬렉션을 Map으로 최적화 한 뒤에 orderItems로 삽입
        //쿼리가 단 1+1, 두번만 나간다.
        Map<Long, List<OrderItemQueryDto>> orderItemMap
                = findOrderItemMap(toOrderIds(result));

        //루프를 돌면서 컬렉션 추가
        result.forEach(o->o.setOrderItems(orderItemMap.get(o.getOrderId())));

        return result;
    }

    /*
    Ctrl + Alt + M -> Method Extract
    특정 주문들(orderIds)에 해당하는 모든 OrderItem을 "한 번에 조회"해서
    주문 ID를 키로, 해당하는 OrderItemQueryDto 리스트를 값으로 가지는 Map으로 구성
     */
    private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
        //JPQL로 모든 주문 아이템을 한번에 조회해서 OrderItemQueryDto에 담는다.
        List<OrderItemQueryDto> orderItems = em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto"+
                        "(oi.order.id, i.name, oi.orderPrice, oi.count)"+
                        " from OrderItem oi" +
                        " join oi.item i"+
                        //in이 들어가서 한번에 모든 주문 ID에 대한 orderItem 들을 조회할 수 있게 된다.
                        //이게 V4의 쿼리와 다른 부분
                        " where oi.order.id in :orderIds", OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();
        //Collectors.groupingBy(Function keyMapper)
        //List를 Map으로 바꿔주게 된다.
        //즉, 주문 ID별로 주문 상품들을 묶어주는 역할을 한다.
        return orderItems.stream().collect(Collectors.groupingBy(OrderItemQueryDto::getOrderId));
    }

    //OrderQueryDto의 리스트에서 orderId만 뽑아서 리스트로 반환
    private List<Long> toOrderIds(List<OrderQueryDto> result) {
        return result.stream()
                .map(OrderQueryDto::getOrderId)
                .toList();
    }

    /**
     * 플랫 데이터 버전 (V6)
     */
    public List<OrderFlatDto> findAllByDto_flat() {
        return em.createQuery(
                //진짜 한방으로 다 가져온다. 전부 다 JOIN 한다
                "select new jpabook.jpashop.repository.order.query.OrderFlatDto(" +
                        "o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count)"+
                        " from Order o"+
                        " join o.member m"+
                        " join o.delivery d"+
                        " join o.orderItems oi"+
                        " join oi.item i", OrderFlatDto.class)
                .getResultList();
    }
}
