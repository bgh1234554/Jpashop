package jpabook.jpashop.web;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    //상품 등록//
    @GetMapping(value="/items/new")
    public String createForm(Model model){
        model.addAttribute("form",new BookForm());
        return "items/createItemForm";
    }
    @PostMapping(value="/items/new")
    public String create(BookForm form){
        //web에서 정보를 받아 service를 이용해 DB에 저장
        //실제로는 setter를 사용하기보다는 create 메서드를 만들어 객체를 생성하는 것이 좋다.
        Book book = new Book();
        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());

        itemService.saveItem(book);
        return "redirect:/";
    }

    //상품 목록 조회//
    @GetMapping(value = "/items")
    private String list(Model model){
        //서비스에서 상품을 조회에 모델에 얹어준다
        model.addAttribute("items", itemService.findItems());
        return "items/itemList";
    }

    //상품 수정 폼 - 기존 상품 정보를 수정하기 위해 DB에서 현 정보를 불러온다//
    @GetMapping(value = "/items/{itemId}/edit")
    //ID는 변경될 수 있으니까 @PathVariable 사용
    public String updateItemForm(@PathVariable("itemId") Long itemId, Model model){
        Book item = (Book) itemService.findOne(itemId);
        BookForm form = new BookForm();
        form.setId(item.getId());
        form.setName(item.getName());
        form.setPrice(item.getPrice());
        form.setStockQuantity(item.getStockQuantity());
        form.setAuthor(item.getAuthor());
        form.setIsbn(item.getIsbn());

        model.addAttribute("form", form);
        return "items/updateItemForm";
    }
    /*
    만약, 여러 품목을 지원해야 한다면 보통 타입 별로 수정폼을 다르게 만든다.

    @GetMapping("/items/{itemId}/edit/book")
    public String updateBookForm(@PathVariable Long itemId, Model model) {
        Book book = (Book) itemService.findOne(itemId);
        BookForm form = new BookForm();
        // ...폼에 book 정보 세팅
        model.addAttribute("form", form);
        return "items/updateBookForm";
    }

    @GetMapping("/items/{itemId}/edit/album")
    public String updateAlbumForm(@PathVariable Long itemId, Model model) {
        Album album = (Album) itemService.findOne(itemId);
        AlbumForm form = new AlbumForm();
        // ...폼에 album 정보 세팅
        model.addAttribute("form", form);
        return "items/updateAlbumForm";
    }

     */
    //상품 수정 - 정보를 바탕으로 진짜 DB에 수정된 정보를 올리는 것//
//    merge 사용 버전 - 사용하지 않는 것을 권장
//    @PostMapping(value = "/items/{itemId}/edit")
//    public String updateItem(@ModelAttribute("form") BookForm form){
        //컨트롤러에서 어설프게 Book을 만들지 않기
//        Book book = new Book();
//        book.setId(form.getId());
//        book.setName(form.getName());
//        book.setPrice(form.getPrice());
//        book.setStockQuantity(form.getStockQuantity());
//        book.setAuthor(form.getAuthor());
//        book.setIsbn(form.getIsbn());
//
//        itemService.saveItem(book); //저장 = 수정
//        return "redirect:/items"; //수정 후 원래 상품 목록 페이지로 복귀
//    }

    //권장 코드 - Dirty Checking 버전
    @PostMapping(value="/items/{itemId}/edit")
    public String updateItem(@PathVariable Long itemId, @ModelAttribute("form") BookForm form){
        itemService.updateItem(itemId, form.getName(), form.getPrice(), form.getStockQuantity());
        return "redirect:/items";
    }
}
