package jpabook.jpashop.domain.item;

import jakarta.persistence.*;
import jpabook.jpashop.domain.Category;
import jpabook.jpashop.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) //종류가 안많으니까
@DiscriminatorColumn(name = "dtype")
public abstract class Item {

    @Id @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

    //비즈니스 로직 추가 - 재고 늘리고 줄이기
    public void addStock(int quantity){
        this.stockQuantity += quantity;
    }

    public void removeStock(int quantity){
        int restStock = this.stockQuantity - quantity;
        if(restStock<0){
            //커스텀 예외 추가
            throw new NotEnoughStockException("need more stock");
        }
        this.stockQuantity = restStock;
    }
}
