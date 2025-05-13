package jpabook.jpashop.domain.item;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue(value = "B")
@Getter
@Setter
public class Book extends Item {
    private String author;
    private String isbn;
}
