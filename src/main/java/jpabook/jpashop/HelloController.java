package jpabook.jpashop;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {
    @GetMapping("/hello")
    //Model - data를 실어서 view에 넘긴다.
    public String hello(Model model){
        model.addAttribute("data", "Hello!");
        return "hello"; //화면 이름을 return에 적어준다. 알아서 hello.html이 된다.
    }
}
