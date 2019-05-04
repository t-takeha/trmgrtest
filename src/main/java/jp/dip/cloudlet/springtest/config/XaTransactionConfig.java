package jp.dip.cloudlet.springtest.config;

import com.atomikos.icatch.config.UserTransactionService;
import com.atomikos.icatch.config.UserTransactionServiceImp;
import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.boot.autoconfigure.transaction.jta.JtaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jta.atomikos.AtomikosProperties;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.util.StringUtils;

import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import java.io.File;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableConfigurationProperties({AtomikosProperties.class, JtaProperties.class})
@ConditionalOnClass({JtaTransactionManager.class, UserTransactionManager.class})
public class XaTransactionConfig {
    /**
     * XA用トランザクションマネージャ名
     */
    public static final String XA_TRANSACTION_MANAGER = "atomikosJtaTransactionManager";

    // ロガー
    private static final Logger log = LogManager.getLogger(XaTransactionConfig.class);

    @Autowired
    JtaProperties jtaProperties;

    @Autowired
    TransactionManagerCustomizers transactionManagerCustomizers;

//    private final JtaProperties jtaProperties;
//
//    private final TransactionManagerCustomizers transactionManagerCustomizers;
//
//    XaTransactionConfig(JtaProperties jtaProperties,
//                        ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers) {
//        this.jtaProperties = jtaProperties;
//        this.transactionManagerCustomizers = transactionManagerCustomizers
//                .getIfAvailable();
//    }

    /**
     * UserTransactionServiceの定義
     *
     * @param atomikosProperties Atomikosのプロパティ
     * @return UserTransactionServiceImpのインスタンス
     */
    @Bean(initMethod = "init", destroyMethod = "shutdownWait", name = "atomikosUserTransactionService")
    @ConditionalOnMissingBean(UserTransactionService.class)
    public UserTransactionServiceImp userTransactionService(AtomikosProperties atomikosProperties) {
        Properties properties = new Properties();
        if (StringUtils.hasText(this.jtaProperties.getTransactionManagerId())) {
            properties.setProperty("com.atomikos.icatch.tm_unique_name",
                    this.jtaProperties.getTransactionManagerId());
        }
        properties.setProperty("com.atomikos.icatch.log_base_dir", getLogBaseDir());
        properties.putAll(atomikosProperties.asProperties());

        log.info("### atomikosProperties : " + properties);

        return new UserTransactionServiceImp(properties);
    }

    private String getLogBaseDir() {
        if (StringUtils.hasLength(this.jtaProperties.getLogDir())) {
            return this.jtaProperties.getLogDir();
        }
        return new File(new ApplicationHome().getDir(), "transaction-logs").getAbsolutePath();
    }

    /**
     * UserTransactionの定義
     *
     * @return UserTransactionImpのインスタンス
     * @throws SystemException タイムアウト設定に失敗した
     */
    @Bean("atomikosUserTransaction")
    @DependsOn("atomikosUserTransactionService")
    public UserTransaction userTransaction() throws SystemException {
        UserTransaction userTransaction = new UserTransactionImp();
//        userTransaction.setTransactionTimeout(300);

        return userTransaction;
    }

    /**
     * UserTransactionManagerの定義
     *
     * @param userTransactionService UserTransactionServiceのインスタンス
     * @return TransactionManager AtomikosのUserTransactionManagerインスタンス
     */
    @Bean(initMethod = "init", destroyMethod = "close", name = "atomikosUserTransactionManager")
    @ConditionalOnMissingBean
    @DependsOn("atomikosUserTransactionService")
    public TransactionManager userTransactionManager(
            UserTransactionService userTransactionService) {
        UserTransactionManager manager = new UserTransactionManager();
        manager.setStartupTransactionService(false);
        manager.setForceShutdown(false);    // ここだけはデフォルトから変えた

        return manager;
    }

    /**
     * Atomikos用トランザクションマネージャの定義.
     *
     * @param userTransaction        　AtomikosのUserTransaction
     * @param userTransactionManager AtomikosのUserTransactionManager
     * @return JtaTransactionManager
     */
    @Bean(XA_TRANSACTION_MANAGER)
    @DependsOn("atomikosUserTransactionService")
    public JtaTransactionManager transactionManager(
            @Autowired @Qualifier("atomikosUserTransaction") UserTransaction userTransaction,
            @Autowired @Qualifier("atomikosUserTransactionManager") TransactionManager userTransactionManager) {
        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager(
                userTransaction, userTransactionManager);
        if (this.transactionManagerCustomizers != null) {
            this.transactionManagerCustomizers.customize(jtaTransactionManager);
        }

        return jtaTransactionManager;
    }
}
