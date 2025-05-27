package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepositoryOld;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepositoryOld memberRepository;

    @Test
    //@Rollback(false)
    public void 회원가입() throws Exception {
        //Given
        Member member = new Member();
        member.setName("kim");
        //When
        Long saveId = memberService.join(member); //INSERT 쿼리 안날라감. flush가 안되기 때문에.
        //Then
        assertEquals(member, memberRepository.findOne(saveId));
    }

    @Test
    public void 중복회원예외() throws Exception {
        //Given
        Member member1 = new Member();
        member1.setName("kim");

        Member member2 = new Member();
        member2.setName("kim");
        //when
        memberService.join(member1);
        //then
        //member2의 회원가입을 시도했을 때, 우리가 정한 예외 클래스가 나오는지 테스트
        assertThrows(IllegalStateException.class,
                () -> memberService.join(member2));

        //여기 오면 안되는데 오면 테스트 케이스를 잘못 잘성한 것이니까
        fail("Exception must be thrown");
    }
}