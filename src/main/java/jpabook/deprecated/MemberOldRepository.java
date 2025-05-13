package jpabook.deprecated;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class MemberOldRepository {

    @PersistenceContext
    private EntityManager em; //알아서 생성

    public Long save(MemberOld memberOld) {
        em.persist(memberOld);
        return memberOld.getId();
    }

    public MemberOld find(Long id){
        return em.find(MemberOld.class, id);
    }
}
