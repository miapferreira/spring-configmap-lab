package configmaplab;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RestController
public class DemoApplication {

    @Value("${app.message:Hello from DEFAULT}")
    private String appMessage;

    @GetMapping("/hello")
    public String hello(@RequestParam(value="name", defaultValue="Michel") String name) {
        String dbHost = System.getenv().getOrDefault("DB_HOST", "db.local");
        return String.format("Hi %s! app.message='%s' | DB_HOST='%s'%n", name, appMessage, dbHost);
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
