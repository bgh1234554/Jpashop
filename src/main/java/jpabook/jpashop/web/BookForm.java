package jpabook.jpashop.web;

import lombok.Getter;
import lombok.Setter;

//상품 등록 폼//
@Getter @Setter
public class BookForm {
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;

    private String author;
    private String isbn;
}
