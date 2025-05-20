package jpabook.jpashop.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @PersistenceContext
    EntityManager em;

    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepository orderRepository;

    @Test
    public void 상품주문() throws Exception {
        //Given
        Member member = createMember();
        Item item = createBook("JPA in Action", 10000, 10);
        int orderCount = 2;
        //When - 회원이 책을 주문을 했을 때,
        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);
        //Then - DB에 정상적으로 정보가 저장되었는지 확인한다.
        Order getOrder = orderRepository.findOne(orderId);

        //getOrder 내에 있는 메서드로 최대한 표현하도록 해보자
        assertEquals(OrderStatus.ORDER, getOrder.getStatus(), "주문 상태 일치");
        assertEquals(1, getOrder.getOrderItems().size(), "주문 상품 종류 수");
        assertEquals(10000*2, getOrder.getTotalPrice(), "주문 가격 테스트");
        assertEquals(8, item.getStockQuantity(), "재고 수량 체크");
    }

    @Test
    public void 재고수량초과() throws Exception {
        //Given
        Member member = createMember();
        Item item = createBook("JPA in Action", 10000, 1);
        int orderCount = 2;
//        //When
//        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);
//        //Then
//        assertThrows(IllegalStateException.class)
        //When, Then -> 주문 즉시 예외가 발생하니까 합쳐진다.
        assertThrows(NotEnoughStockException.class,
                () -> orderService.order(member.getId(), item.getId(), orderCount));
        //orderService.order -> OrderItem.createOrderItem
        //-> item.removeStock(count) -> NotEnoughStockException("need more stock");
    }

    @Test
    public void 주문취소() throws Exception {
        //Given
        //회원이 책을 2권 주문을 확정하였다.
        Member member = createMember();
        Item item = createBook("JPA in Action", 10000, 10);
        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);
        //When
        //취소 후에
        orderService.cancelOrder(orderId);
        //Then
        //DB에 주문 상태가 취소로 바뀌었는지 확인한다.
        Order getOrder = orderRepository.findOne(orderId);
        assertEquals(OrderStatus.CANCEL,getOrder.getStatus(),"주문 취소시 CANCEL 리턴");
        //주문 수량이 원상 복구되었는지 확인한다.
        assertEquals(10,item.getStockQuantity(),"재고 수량 다시 증가");
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "강가", "123-123"));
        em.persist(member);
        return member;
    }
    private Book createBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setStockQuantity(stockQuantity);
        book.setPrice(price);
        em.persist(book);
        return book;
    }

    /*
    JUnit 5 - assertEquals(expected,actual,message)
     */
}