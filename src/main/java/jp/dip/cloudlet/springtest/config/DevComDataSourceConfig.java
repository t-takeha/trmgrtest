package jp.dip.cloudlet.springtest.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * プライマリデータソースの定義.
 *
 * 非XAのDataSourceの定義
 * DataSourceの実装はapplication.ymlのtypeで指定する.
 * トランザクションマネージャにはローカルトランザクション用のDataSourceTransactionManagerを使用.
 * MyBatisは利用しない.
 */
@Configuration
@EnableTransactionManagement
public class DevComDataSourceConfig {
    /**
     * トランザクションマネージャ名（外からの参照あり）
     */
    public static final String TRANSACTION_MANAGER = "devcomTransactionManager";

    /**
     * データソース名
     */
    private static final String _DATASOURCE = "devcomDataSource";

    /**
     * DataSourceプロパティ読み込み用Beanを定義する.
     * 設定値はapplication.ymlで定義
     */
    @Primary
    @Bean("devcomDataSourceProperties")
    @ConfigurationProperties(prefix = "spring.datasource.devcom")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * DDEVCOM_DATASOURCEのDataSource用Beanを定義する.
     *
     * @return _DEVCOM_DATASOURCEのDataSource
     */
    @Primary
    @Bean(_DATASOURCE)
    public DataSource dataSource(@Autowired @Qualifier("devcomDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }
//    @ConfigurationProperties(prefix = "spring.datasource.devcom")
//    public DataSource dataSource() { return DataSourceBuilder.create().type(DriverManagerDataSource.class).build(); }


    /**
     * トランザクションマネージャの定義
     * @param dataSource  _DEVCOM_DATASOURCEのDataSourceのDataSource
     * @return PlatformTransactionManager
     */
    @Primary
    @Bean(TRANSACTION_MANAGER)
    public PlatformTransactionManager transactionManager(
            @Autowired @Qualifier(_DATASOURCE) DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
