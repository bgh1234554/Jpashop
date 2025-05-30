package jpabook.jpashop.repository.order.simplequery;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {
    public final EntityManager em;

    public List<OrderSimpleQueryDto> findOrderDtos(){
        //검색 결과를 DTO로 반환할 때 new를 사용한다.
        return em.createQuery("select new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto" +
                "(o.id, m.name, o.orderDate, o.status, d.address)" +
                " from Order o" +
                " join o.member m" +
                " join o.delivery d", OrderSimpleQueryDto.class).getResultList();
    }
}
/**
 * 일반적인 SQL을 사용할 때 처럼 원하는 값을 선택해서 조회
 * new 명령어를 사용해서 JPQL의 결과를 DTO로 즉시 변환
 * SELECT 절에서 원하는 데이터를 직접 선택하므로 DB 애플리케이션 네트웍 용량 최적화(생각보다 미비)
 * 리포지토리 재사용성 떨어짐, API 스펙에 맞춘 코드가 리포지토리에 들어가는 단점
 */