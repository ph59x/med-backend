package xyz.ph59.med;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class MedApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedApplication.class, args);
    }

}
