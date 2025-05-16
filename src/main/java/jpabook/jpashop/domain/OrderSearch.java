package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

//검색 조건 파라미터
@Getter @Setter
public class OrderSearch {

    private String memberName;
    private OrderStatus orderStatus;
}
