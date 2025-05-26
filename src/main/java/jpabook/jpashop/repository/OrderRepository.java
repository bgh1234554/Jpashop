package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {
    public final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    //검색 주문 조건
    public List<Order> findAllByString(OrderSearch orderSearch) {
        //QueryDSL을 통해 자바문법답게 쿼리를 만들 수 있으나,
        //아직 안배웠으므로, JPQL과 JPQL Criteria로 만족하자.
        //language=JPAQL
//        String jpql = "select o From Order o join o.member m";
//        boolean isFirstCondition = true;
//        //주문 상태 검색
//        if (orderSearch.getOrderStatus() != null) {
//            if (isFirstCondition) {
//                jpql += " where";
//                isFirstCondition = false;
//            } else {
//                jpql += " and";
//            }
//            jpql += " o.status = :status";
//        }
//        //회원 이름 검색
//        if (StringUtils.hasText(orderSearch.getMemberName())) {
//            if (isFirstCondition) {
//                jpql += " where";
//                isFirstCondition = false;
//            }
//        } else {
//            jpql += " and";
//        }
//        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
//                .setMaxResults(1000); //최대 1000건
//        if (orderSearch.getOrderStatus() != null) {
//            query = query.setParameter("status", orderSearch.getOrderStatus());
//        }
//        if (StringUtils.hasText(orderSearch.getMemberName())) {
//            query = query.setParameter("name", orderSearch.getMemberName());
//        }
//        return query.getResultList();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Order, Member> m = o.join("member", JoinType.INNER); //회원과 조인
        List<Predicate> criteria = new ArrayList<>();
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"),
                    orderSearch.getOrderStatus());
            criteria.add(status);
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name =
                    cb.like(m.<String>get("name"), "%" +
                            orderSearch.getMemberName() + "%");
            criteria.add(name);
        }
        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); //최대 1000건
        return query.getResultList();
    }
    /**
     * ✅ 1. QueryDSL 환경 설정 (Gradle 기준)
     * ① Gradle 의존성 추가
     *
     * build.gradle에 아래 코드 추가해:
     *
     * plugins {
     *     id "com.ewerk.gradle.plugins.querydsl" version "1.0.10"
     * }
     *
     * dependencies {
     *     implementation "com.querydsl:querydsl-jpa"
     *     annotationProcessor "com.querydsl:querydsl-apt"
     *     annotationProcessor "jakarta.annotation:jakarta.annotation-api"
     *     annotationProcessor "jakarta.persistence:jakarta.persistence-api"
     * }
     *
     * def querydslDir = "src/main/generated"
     *
     * querydsl {
     *     jpa = true
     *     querydslSourcesDir = querydslDir
     * }
     *
     * sourceSets {
     *     main.java.srcDirs += [ querydslDir ]
     * }
     *
     * tasks.withType(JavaCompile) {
     *     options.annotationProcessorGeneratedSourcesDirectory = file(querydslDir)
     * }
     *
     * ② Q파일 생성 (한번만 해주면 됨)
     *
     * ./gradlew clean compileQuerydsl
     *
     * QOrder, QMember 등의 클래스가 src/main/generated에 생길 거야.
     * ✅ 2. JPAQueryFactory Bean 등록
     *
     * 스프링에서 @Configuration 클래스에 아래처럼 Bean 등록:
     *
     * @Configuration
     * public class QuerydslConfig {
     *
     *     @PersistenceContext
     *     private EntityManager em;
     *
     *     @Bean
     *     public JPAQueryFactory queryFactory() {
     *         return new JPAQueryFactory(em);
     *     }
     * }
     *
     * ✅ 3. OrderRepository에 QueryDSL 코드 추가
     *
     * @RequiredArgsConstructor
     * @Repository
     * public class OrderQueryRepository {
     *
     *     private final JPAQueryFactory queryFactory;
     *
     *     public List<Order> findAll(OrderSearch orderSearch) {
     *         QOrder order = QOrder.order;
     *         QMember member = QMember.member;
     *
     *         return queryFactory
     *                 .selectFrom(order)
     *                 .join(order.member, member)
     *                 .where(
     *                         statusEq(orderSearch.getOrderStatus()),
     *                         nameLike(orderSearch.getMemberName())
     *                 )
     *                 .limit(1000)
     *                 .fetch();
     *     }
     *
     *     private BooleanExpression statusEq(OrderStatus status) {
     *         return status != null ? QOrder.order.status.eq(status) : null;
     *     }
     *
     *     private BooleanExpression nameLike(String name) {
     *         return StringUtils.hasText(name) ? QOrder.order.member.name.contains(name) : null;
     *     }
     * }
     *
     *     🔍 BooleanExpression을 반환하는 메서드들을 이용하면 조건이 null일 경우 무시되어 자동으로 동적 쿼리가 만들어져!
     *
     * ✅ 정리
     * 항목	내용
     * 설정	build.gradle에 querydsl 관련 설정, Q 클래스 생성
     * 핵심	JPAQueryFactory로 쿼리 작성, where() 조건은 BooleanExpression 조합
     * 장점	컴파일 시 오류 잡기, 자동완성, 동적 쿼리 깔끔
     */

    //JPA 2편 섹션 3 - V3 용 OrderRepository 추가 코드
    //LAZY 그런거 상관없이 검색할 때 연관된 테이블을 한번에 다 긁어온다. fetch join 이니까
    public List<Order> findAllWithMemberDelivery(){
        return em.createQuery(
                "select o from Order o"+
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
        ).getResultList();
    }

    //섹션 4의 V3용 메서드
    public List<Order> findAllWithItem() {
        return em.createQuery(
                //사실 최신 Hibernate에서는 distinct 키워드가 필요 없다.
                //실무에서는 QueryDSL로 매우 쉽게 만들 수 있다
                        "select distinct o from Order o"+
                        " join fetch o.member m"+
                        " join fetch o.delivery d"+
                        " join fetch o.orderItems oi"+ // 데이터 양이 여기서 뻥튀기된다
                        " join fetch oi.item i", Order.class)
                .getResultList();
    }

    //섹션 4의 V3.1용 메서드
    public List<Order> findAllWithMemberDelivery(int offset, int limit){
        return em.createQuery(
                "select o from Order o"+
                        " join fetch o.member m"+
                        " join fetch o.delivery d", Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }
}