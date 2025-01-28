package org.gym.basic;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableFeignClients
public class BasicGymApplication {

	public static void main(String[] args) {
		SpringApplication.run(BasicGymApplication.class, args);
	}

	@Bean
	public MeterBinder meterBinder(){
		return meterRegistry -> {
			Counter.builder("login_counter")
					.tag("version", "v1.0")
					.description("Number of logging")
					.register(meterRegistry);
		};
	}
}
