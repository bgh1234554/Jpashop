package jpabook.jpashop.web;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    //회원 등록//
    @GetMapping(value="/members/new")
    public String createForm(Model model){
        model.addAttribute("memberForm", new MemberForm());
        return "members/createMemberForm";
    }

    @PostMapping(value="/members/new")
    public String create(@Validated MemberForm form, BindingResult result){
        if(result.hasErrors()){
            return "members/createMemberForm";
        }
        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());
        Member member = new Member();
        member.setName(form.getName());
        member.setAddress(address);

        memberService.join(member);
        return "redirect:/"; //회원 가입 후 홈 화면으로 이동
    }

    //회원 목록 조회//
    @GetMapping("/members")
    public String list(Model model){
        /*
        조회한 상품을 뷰에 전달하기 위해
        스프링 MVC가 제공하는 모델( Model ) 객체에 보관
        실행할 뷰 이름을 반환
         */
        model.addAttribute("members", memberService.findMembers());
        return "members/memberList";
    }
}
