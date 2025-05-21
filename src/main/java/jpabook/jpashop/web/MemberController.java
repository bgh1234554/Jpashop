package jpabook.jpashop.web;

import jakarta.validation.Valid;
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
    //@Valid로 Memberform의 validation 기능을 적용할 수 있다
    public String create(@Valid MemberForm form, BindingResult result){
        //BindingResult -> 오류가 있을 경우, 오류를 담아두는 객체
        if(result.hasErrors()){
            return "members/createMemberForm";
            //오류 발생 시 createMemberForm으로 다시 가서
            //빨간색 네모로 "회원 이름은 필수입니다"라는 메시지가 나왔다.
            //form 데이터도 그대로 가져가기 때문에 데이터가 그대로 화면에 남아있다.
        }
        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());
        Member member = new Member();
        member.setName(form.getName());
        member.setAddress(address);

        memberService.join(member);
        return "redirect:/"; //회원 가입 후 홈 화면으로 이동
    }
    /*
    Memberform 쓰는 이유?
    컨트롤러에서 원하는 조건과 엔티티가 원하는 조건이 다를 수 있고,
    엔티티가 점점 지저분해지기 때문에, Member 엔티티를 바로 사용하지 않고 별도의 폼 객체를 만든다.
     */

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
