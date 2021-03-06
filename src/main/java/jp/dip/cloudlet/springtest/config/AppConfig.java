package jp.dip.cloudlet.springtest.config;

import jp.dip.cloudlet.springtest.tasklet.InitH2DbTasklet;
import jp.dip.cloudlet.springtest.tasklet.LocalDb1TransactionTestTasklet;
import jp.dip.cloudlet.springtest.tasklet.LocalDb2TransactionTestTasklet;
import jp.dip.cloudlet.springtest.tasklet.XaDbTransactionTestTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * このプロジェクト唯一のジョブ定義.
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AppConfig {

    @Autowired
    JobBuilderFactory jobBuilderFactory;

    @Autowired
    StepBuilderFactory stepBuilderFactory;

    @Autowired
    LocalDb1TransactionTestTasklet localDb1TransactionTestTasklet;

    @Autowired
    LocalDb2TransactionTestTasklet localDb2TransactionTestTasklet;

    @Autowired
    XaDbTransactionTestTasklet xaDbTransactionTestTasklet;

    @Autowired
    InitH2DbTasklet initH2DbTasklet;

    @Bean
    public Job defaultJob() {
        return jobBuilderFactory.get("defaultJob")
                .incrementer(new RunIdIncrementer())
                .start(stepBuilderFactory.get("defaultStep1").tasklet(localDb1TransactionTestTasklet).build())
                .next(stepBuilderFactory.get("defaultStep2").tasklet(localDb2TransactionTestTasklet).build())
                .next(stepBuilderFactory.get("defaultStep3").tasklet(xaDbTransactionTestTasklet).build())
//                .start(stepBuilderFactory.get("defaultStep3").tasklet(xaDbTransactionTestTasklet).build())
                .build();
    }

    @Bean
    public Job initH2DbJob() {
        return jobBuilderFactory.get("initH2DbJob")
                .incrementer(new RunIdIncrementer())
                .start(stepBuilderFactory.get("initH2DbStep").tasklet(initH2DbTasklet).build())
                .build();
    }
}
