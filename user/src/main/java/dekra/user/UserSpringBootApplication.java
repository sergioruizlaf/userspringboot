package dekra.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "dekra.user")
public class UserSpringBootApplication  {

	public static void main(String[] args) {
		SpringApplication.run(UserSpringBootApplication.class, args);
	}

}
