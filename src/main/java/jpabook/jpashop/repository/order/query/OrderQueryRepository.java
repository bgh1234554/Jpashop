package jpabook.jpashop.repository.order.query;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    /**
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
        List<OrderQueryDto> result = findOrders();

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
        //toOne 코드를 한번에 조회
        List<OrderQueryDto> result = findOrders();

        //이후 해당 주문의 ID에 대한
        //orderItem 컬렉션을 MAP을 이용해 조회후
        Map<Long, List<OrderItemQueryDto>> orderItemMap
                = findOrderItemMap(toOrderIds(result));

        //루프를 돌면서 컬렉션 추가
        result.forEach(o->o.setOrderItems(orderItemMap.get(o.getOrderId())));

        return result;
    }

    /*
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
