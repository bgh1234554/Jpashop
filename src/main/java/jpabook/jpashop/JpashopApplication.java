package jpabook.jpashop;

import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import jpabook.deprecated.Hello;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JpashopApplication {

    public static void main(String[] args) {
        Hello hello = new Hello();
        hello.setData("Hello World");
        String data = hello.getData();
        System.out.println("data = " + data);
        SpringApplication.run(JpashopApplication.class, args);
    }

    //기본적으로 초기화 된 프록시 객체만 노출,
    //초기화 되지 않은 프록시 객체는 노출 안함
    //섹션 3 버전 1용
    @Bean
    Hibernate5JakartaModule hibernate5Module() {
        //강제 지연 로딩 하는 방법
//        Hibernate5JakartaModule hibernate5JakartaModule = new Hibernate5JakartaModule();
//        hibernate5JakartaModule.configure(Hibernate5JakartaModule.Feature.FORCE_LAZY_LOADING,true);
        return new Hibernate5JakartaModule();
    }
}
