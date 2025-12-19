package online.backend.ghostshare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class GhostShareApplication {

    public static void main(String[] args) {
        SpringApplication.run(GhostShareApplication.class, args);
    }

}
