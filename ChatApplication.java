package org.tpl.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableMongoAuditing
@EnableFeignClients(basePackages = {"org.tpl.chat.service.remote"})
@ComponentScan(basePackages = "org.tpl")
@EnableRetry
@EnableCaching
public class ChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);
    }

}
