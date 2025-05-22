package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
//    @Autowired //생성자 Injection 많이 사용, 생성자가 하나면 생략 가능
//    MemberRepository memberRepository;

    //생성자 주입 방식을 권장
    //변경 불가능한 안전한 객체 생성 가능
    private final MemberRepository memberRepository;

    /*
    회원 가입
     */
    @Transactional //정보를 변경하는 거니까
    //여기에서는 readOnly=false로 적용된다. 위쪽 어노테이션이 우선 적용된다.
    public Long join(Member member){
        validateDuplicateMember(member);
        memberRepository.save(member);
        return member.getId();
    }
    private void validateDuplicateMember(Member member){
        //비즈니스 로직 상 중복 회원이 없도록 설계되었더라도, 데이터 무결성이 깨질 가능성을 고려해 방어적으로 작성된 코드입니다.
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if(!findMembers.isEmpty()){
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    /*
    전체 회원 조회
     */
    public List<Member> findMembers(){
        return memberRepository.findAll();
    }

    public Member findOne(Long memberId){
        return memberRepository.findOne(memberId);
    }

    @Transactional
    //변경 감지를 사용해서 커밋 시점에 자동으로 엔티티 정보 수정
    public void update(Long id, String name) {
        Member member = memberRepository.findOne(id);
        member.setName(name);
    }
}
