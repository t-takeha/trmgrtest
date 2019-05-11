package jp.dip.cloudlet.springtest.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.configuration.BatchConfigurationException;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.MapJobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.PostConstruct;

/**
 * バッチ定義.
 * (note) 内部リポジトリMapベースで定義している.
 */
@Configuration
public class BatchBaseConfig implements BatchConfigurer {

    private static final Logger log = LogManager.getLogger();

    private PlatformTransactionManager transactionManager;

    private JobRepository jobRepository;

    private JobLauncher jobLauncher;

    private JobExplorer jobExplorer;

    @Override
    public PlatformTransactionManager getTransactionManager() throws Exception {
        return this.transactionManager;
    }

    @Override
    public JobRepository getJobRepository() throws Exception {
        return this.jobRepository;
    }

    @Override
    public JobLauncher getJobLauncher() throws Exception {
        return this.jobLauncher;
    }

    @Override
    public JobExplorer getJobExplorer() throws Exception {
        return this.jobExplorer;
    }

    /**
     * Mapベースのリポジトリを構成する
     *
     * @see DefaultBatchConfigurer#initialize()
     */
    @PostConstruct
    public void initialize() {
        try {
            log.info("### Initialize Map based JobRepository ###");
            // transactionManager生成
            this.transactionManager = new ResourcelessTransactionManager();

            // jobRepository生成
            MapJobRepositoryFactoryBean jobRepositoryFactory = new MapJobRepositoryFactoryBean(getTransactionManager());
            jobRepositoryFactory.afterPropertiesSet();
            this.jobRepository = jobRepositoryFactory.getObject();

            // jobExplorer生成
            MapJobExplorerFactoryBean jobExplorerFactory = new MapJobExplorerFactoryBean(jobRepositoryFactory);
            jobExplorerFactory.afterPropertiesSet();
            this.jobExplorer = jobExplorerFactory.getObject();

            // jobLauncher生成
            SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
            jobLauncher.setJobRepository(jobRepository);
            jobLauncher.afterPropertiesSet();
            this.jobLauncher = jobLauncher;
            
        } catch (Exception e) {
            throw new BatchConfigurationException(e);
        }
    }
}
