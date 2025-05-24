package jpabook.jpashop.api;

import jakarta.validation.Valid;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController //@Controller + @ResponseBody (데이터 자체를 JSON이나 XML로 보낼 때 사용)
@RequiredArgsConstructor
public class MemberApiController {
    private final MemberService memberService;

    //회원 등록 API//
    /**
     * 등록 V1: 요청 값으로 Member 엔티티를 직접 "받는다".
     * 문제점
     * - 엔티티에 프레젠테이션 계층을 위한 로직이 추가된다.
     * - 엔티티에 API 검증을 위한 로직이 들어간다. (@NotEmpty 등등)
     * - 실무에서는 회원 엔티티를 위한 API가 다양하게 만들어지는데, 한 엔티티에 각각의 API를 위한
     모든 요청 요구사항을 담기는 어렵다.
     * - 엔티티가 변경되면 API 스펙이 변한다.
     * 결론
     * - API 요청 스펙에 맞추어 별도의 DTO를 파라미터로 받는다.
     */
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member){
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @Data
    /*
    @Data가 포함하는 기능
    - DTO 같은 단순한 데이터 전달 객체에 붙이기 좋음.

    @Getter: 모든 필드의 getter 생성

    @Setter: 모든 필드의 setter 생성

    @ToString: toString() 자동 생성

    @EqualsAndHashCode: equals()와 hashCode() 자동 생성

    @RequiredArgsConstructor: final이나 @NonNull 필드만 가지고 생성자 생성
     */
    static class CreateMemberResponse{
        private Long id;
        public CreateMemberResponse(Long id){
            this.id = id;
        }
    }

    @Data
    static class CreateMemberRequest{
        private String name;
    }

    /**
     * 등록 V2: 엔티티를 직접 받지 않고 별도의 DTO를 받아서 처리한다.
     */
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request){
        Member member = new Member();
        //이렇게 하면 엔티티의 객체 정보가 바뀌어도 setter 메서드 이름만 바꾸면 되고,
        //중간에서 매핑만 해주면 되기 때문에 상대적으로 안정적으로 운영될 수 있다.
        member.setName(request.getName());
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    /**
     * 회원 정보 수정 API
     * 등록, 수정의 경우에는 요구 데이터가 다를 수 있기 때문에 별도의 DTO를 만드는 것이 좋다.
     */
    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateMemberRequest request) {
        //수정은 가급적이면 변경 감지!
        //MemberSerivce의 update 메서드를 이용해 아이디의 이름을 업데이트
        memberService.update(id, request.getName());
        Member findMember = memberService.findOne(id);
        //회원 ID 누구를 어떤 이름으로 변경했는지 확인하여 DTO 형태로 전송
        return new UpdateMemberResponse(findMember.getId(),findMember.getName());
    }

    @Data //DTO는 크게 로직이 있는 것이 아니라서 가볍게 롬복으로 객체를 만들어 버린다.
    static class UpdateMemberRequest{
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse{
        private Long id;
        private String name;
    }

    //회원 조회 API//

    /**
     * 조회 V1: 응답 값으로 "엔티티를 직접 외부에 노출"한다.
     * 문제점
     * - 엔티티에 프레젠테이션 계층을 위한 로직이 추가된다.
     * - 기본적으로 엔티티의 "모든 값이 노출"된다.
     * - 응답 스펙을 맞추기 위해 로직이 추가된다. (@JsonIgnore, 별도의 뷰 로직 등등)
     * - 실무에서는 같은 엔티티에 대해 API가 용도에 따라 다양하게 만들어지는데,
        "한 엔티티에 각각의 API를 위한 프레젠테이션 응답 로직을 담기는 어렵다."
     * - 엔티티가 변경되면 API 스펙이 변한다.
     * - 추가로 컬렉션을 직접 반환하면 항후 API 스펙을 변경하기 어렵다.
        (별도의 Result 클래스 생성으로 해결)
     * 결론
     * - API 응답 스펙에 맞추어 별도의 DTO를 반환한다.
     */
    @GetMapping("/api/v1/members")
    public List<Member> membersV1(){
        return memberService.findMembers();
        //array를 반환하면 스펙 확장이 안되는 등 유연성이 떨어진다.
    }
    //V2 - 응답 값으로 엔티티가 아닌 별도의 DTO 반환
    @GetMapping("/api/v2/members")
    public Result membersV2(){
        List<Member> findMembers = memberService.findMembers();
        //엔티티 -> DTO 변환
        List<MemberDto> collect = findMembers.stream()
                .map(m->new MemberDto(m.getName()))
                .collect(Collectors.toList());
        return new Result(collect.size(),collect);
    }

    @Data
    @AllArgsConstructor
    static class Result<T>{
        private int count;
        private T data;
    }
    @Data
    @AllArgsConstructor
    static class MemberDto{
        private String name;
    }
    /*
    단순히 List<MemberDto>를 반환하는 것이 아닌,  Result라는 객체에 다시 감싸서 반환할 때의 이점
    앞으로 응답에 다른 정보(ex. total count, pagination info)를 추가해야 할 경우 확장성에 유리하다.
     */
}
/**
 * 실무에서는 API 응답 구조를 더 명확하게 하고 확장성을 확보하기 위해
 * 보통 아래처럼 data, error, meta 각각을 포함하는 **공통 응답 포맷(Response Wrapper DTO)**을 만들어서 사용해.
 * ✅ 실무에서 자주 쓰는 공통 응답 객체 구조 예시
 *
 * @Data
 * @AllArgsConstructor
 * @NoArgsConstructor
 * public class ApiResponse<T> {
 *     private T data;
 *     private ApiError error;
 *     private Meta meta;
 *
 *     public static <T> ApiResponse<T> success(T data) {
 *         return new ApiResponse<>(data, null, null);
 *     }
 *
 *     public static <T> ApiResponse<T> success(T data, Meta meta) {
 *         return new ApiResponse<>(data, null, meta);
 *     }
 *
 *     public static <T> ApiResponse<T> error(ApiError error) {
 *         return new ApiResponse<>(null, error, null);
 *     }
 * }
 *
 * ✅ ApiError 클래스 예시
 *
 * @Data
 * @AllArgsConstructor
 * @NoArgsConstructor
 * public class ApiError {
 *     private String code;
 *     private String message;
 * }
 *
 * 예외 발생 시:
 *
 * {
 *   "data": null,
 *   "error": {
 *     "code": "USER_NOT_FOUND",
 *     "message": "사용자를 찾을 수 없습니다."
 *   },
 *   "meta": null
 * }
 *
 * ✅ Meta 클래스 예시
 *
 * @Data
 * @AllArgsConstructor
 * @NoArgsConstructor
 * public class Meta {
 *     private int totalCount;
 *     private int page;
 *     private int size;
 * }
 *
 * 리스트 응답 시:
 *
 * {
 *   "data": [ ... ],
 *   "error": null,
 *   "meta": {
 *     "totalCount": 121,
 *     "page": 1,
 *     "size": 10
 *   }
 * }
 *
 * 💡 요약
 *
 *     data, error, meta 각각을 담는 DTO를 만들어 공통 응답 구조로 사용
 *
 *     Result<T>도 괜찮지만 실무에서는 확장성을 위해 ApiResponse<T>처럼 구성
 *
 *     실무에서는 성공/실패 케이스 분리와 프론트와의 협업을 위해 이런 구조가 거의 표준처럼 쓰임
 */