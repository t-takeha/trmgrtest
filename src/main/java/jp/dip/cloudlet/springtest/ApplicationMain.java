package jp.dip.cloudlet.springtest;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"jp.dip.cloudlet.springtest.*"})
@EnableBatchProcessing
public class ApplicationMain {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationMain.class, args);
    }
}
