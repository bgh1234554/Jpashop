package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public long saveItem(Item item){
        itemRepository.save(item);
        return item.getId();
    }

    public List<Item> findItems(){
        return itemRepository.findAll();
    }

    public Item findOne(Long itemId){
        return itemRepository.findOne(itemId);
    }

    //영속성 컨텍스트가 알아서 UPDATE 쿼리를 날려주도록 바꿔주기
    @Transactional
    public void updateItem(Long id, String name, int price, int stockQuantity) {
        //@Transaction안에서 조회하니까 Transaction commit 시점에 UPDATE 쿼리가 나갈 수 있다.
        Item item = itemRepository.findOne(id);
        //그냥 item.change와 같이 값을 한번에 변경할 수 있는 메서드를 엔티티에 만드는 것이 좋다.
        item.setName(name);
        item.setPrice(price);
        item.setStockQuantity(stockQuantity);
    }
}
