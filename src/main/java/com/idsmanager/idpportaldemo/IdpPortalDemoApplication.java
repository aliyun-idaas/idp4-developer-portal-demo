package com.idsmanager.idpportaldemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.idsmanager.idpportaldemo"})
public class IdpPortalDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdpPortalDemoApplication.class, args);
    }

}
