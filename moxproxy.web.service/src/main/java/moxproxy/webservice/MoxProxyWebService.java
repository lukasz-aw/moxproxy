package moxproxy.webservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MoxProxyWebService {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(MoxProxyWebService.class);
        app.run();
    }
}
