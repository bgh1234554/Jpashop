package jpabook.jpashop.deprecated;

import jpabook.deprecated.MemberOld;
import jpabook.deprecated.MemberOldRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class MemberOldRepositoryTest {

    @Autowired
    MemberOldRepository memberOldRepository;

    @Test
    @Transactional //테스트 끝난 다음에 바로 DB 롤백
    @Rollback(false)
    public void testMember() throws Exception{
        //given
        MemberOld memberOld = new MemberOld();
        memberOld.setUsername("member1");
        //when
        Long savedId = memberOldRepository.save(memberOld);

        MemberOld findMemberOld = memberOldRepository.find(savedId);
        //then
        Assertions.assertThat(findMemberOld.getId()).isEqualTo(memberOld.getId());
        Assertions.assertThat(findMemberOld.getUsername()).isEqualTo(memberOld.getUsername());
        Assertions.assertThat(findMemberOld).isEqualTo(memberOld);
        // == 비교 해도 같다. 같은 영속성 컨텍스트 안에서 조회하니까.
    }
}