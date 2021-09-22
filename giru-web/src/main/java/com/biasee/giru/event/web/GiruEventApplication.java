package com.biasee.giru.event.web;

import com.biasee.giru.event.client.config.GiruEventAutoConfiguration;
import com.biasee.giru.event.web.bootstrap.BootFailedListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication(exclude = {GiruEventAutoConfiguration.class})
@ComponentScan({"com.biasee.giru.event.web", "com.biasee.giru.event.core"})
@EnableScheduling
public class GiruEventApplication {

    public static void main(String[] args) {

        SpringApplication springApplication = new SpringApplication(GiruEventApplication.class);
        springApplication.addListeners(new BootFailedListener());
        springApplication.run(args);
    }

}
