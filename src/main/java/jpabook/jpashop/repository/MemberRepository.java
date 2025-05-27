package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

//스프링 데이터 JPA 체험
//JpaRepository<타입, ID>
public interface MemberRepository extends JpaRepository<Member, Long> {
    //findOne은 findById로 사용 가능
    //단, Optional로 반환해주기 때문에 마지막에 .get()을 찍어야 한다.

    //일반적으로 상상할 수 있는 메서드들을 이미 다 구현해 놓은 것.

    //밑을 보고 select m from Member m where m.name = ? 이걸 만들어준다.
    List<Member> findByName(String name); //이렇게 하면 알아서 구현해준다!
}