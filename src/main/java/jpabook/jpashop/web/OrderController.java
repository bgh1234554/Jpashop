package jpabook.jpashop.web;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderSearch;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.service.ItemService;
import jpabook.jpashop.service.MemberService;
import jpabook.jpashop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final MemberService memberService;
    private final ItemService itemService;

    //상품 주문//
    @GetMapping(value="/order")
    public String createForm(Model model){
        /*
        메인 화면에서 상품 주문을 선택하면 /order 를 GET 방식으로 호출
        OrderController 의 createForm() 메서드
        주문 화면에는 주문할 고객정보와 상품 정보가 필요하므로
        model 객체에 담아서 뷰에 넘겨줌
         */
        List<Member> members = memberService.findMembers();
        List<Item> items = itemService.findItems();
        model.addAttribute("members", members);
        model.addAttribute("items", items);
        return "order/orderForm";
    }

    @PostMapping(value="/order")
    //@RequestParam - form submit 방식. orderForm.html에 id로 선언된 변수 이름 지정
    public String order(@RequestParam("memberId") Long memberId, @RequestParam("itemId") Long itemId,
                        @RequestParam("count") int count){
        //상품 정보를 받아 주문해야 하니까, 주문을 하기 위한 정보를 form에서 받아 실행한다
        orderService.order(memberId, itemId, count);
        return "redirect:/orders";
    }
    /*
    주문할 회원과 상품 그리고 수량을 선택해서 Submit 버튼을 누르면,
    /order URL을 POST 방식으로 호출.
    컨트롤러의 order() 메서드를 실행.
    이 메서드는 고객 식별자( memberId ), 주문할 상품 식별자( itemId ),
    수량( count ) 정보를 받아서 주문 서비스에 주문을 요청.
    주문이 끝나면 상품 주문 내역이 있는 /orders URL로 리다이렉트.
     */

    //주문 목록 검색, 취소//
    @GetMapping(value="/orders")
    //@ModelAttribute - 요청 파라미터를 객체에 바인딩하고, 동시에 해당 객체를 Model에 자동 등록
    //알아서 Model에 자동으로 담아준다는 뜻
    //GET /orders?orderStatus=ORDER&memberName=김철수
    //-> OrderSearch 클래스의 setOrderStatus()와 setMemberName()을 자동 호출해서 값 넣어줘.
    public String orderList(@ModelAttribute("orderSearch") OrderSearch orderSearch, Model model){
        List<Order> orders = orderService.findOrders(orderSearch);
        model.addAttribute("orders", orders);
        return "order/orderList";
    }
    @PostMapping(value="/orders/{orderId}/cancel")
    public String cancelOrder(@PathVariable("orderId") Long orderId){
        orderService.cancelOrder(orderId);
        return "redirect:/orders";
    }
}
