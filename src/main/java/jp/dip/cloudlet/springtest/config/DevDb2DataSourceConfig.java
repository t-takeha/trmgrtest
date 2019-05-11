package jp.dip.cloudlet.springtest.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * 3番目のデータソース定義
 * <p>
 * 非XAのDataSource、シングルDB向けトランザクションマネージャ（DataSourceTransactionManager）、
 * MyBatis向けSqlSessionFactoryとSqlSessionTemplateの定義。
 * MyBatisのMapperは、@MapperScanのbasePackagesで指定。2番目のデータソースとは配置場所を変えている。
 */
@Configuration
@MapperScan(basePackages = {"jp.dip.cloudlet.springtest.mapper.devdb2"},
        sqlSessionFactoryRef = DevDb2DataSourceConfig.SQLSESSION_FACTORY)
public class DevDb2DataSourceConfig {
    /**
     * トランザクションマネージャ名（外からの参照あり）
     */
    public static final String TRANSACTION_MANAGER = "devdb2TransactionManager" ;

    /**
     * SqlSessionFactory名
     */
    public static final String SQLSESSION_FACTORY = "devdb2SqlSessionFactory";

    /**
     * データソース名
     */
    private static final String _DATASOURCE = "devdb2DataSource";

    /**
     * このデータソース専用のMyBatis用Configuration
     */
    private static final String _MYBATIS_CONF_NAME = "devdb2MybatisConfiguration";

    /**
     * MyBatisのConfigurationをAutowiredする
     */
    @Autowired
    @Qualifier(_MYBATIS_CONF_NAME)
    org.apache.ibatis.session.Configuration mybatisConfiguration;

    /**
     * DataSourceプロパティ読み込み用Beanを定義する.
     * 設定値はapplication.ymlで定義
     */
    @Bean("devdb2DataSourceProperties")
    @ConfigurationProperties(prefix = "spring.datasource.devdb2")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * DEVDB2_DATASOURCEのDataSource用Beanを定義する.
     *
     * @return DEVDB2_DATASOURCEのDataSource
     */
    @Bean(_DATASOURCE)
    public DataSource dataSource(@Autowired @Qualifier("devdb2DataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }
    //   @ConfigurationProperties(prefix = "spring.datasource.devdb2")
    //   public DataSource dataSource() { return DataSourceBuilder.create().type(DriverManagerDataSource.class).build(); }

    /**
     * トランザクションマネージャの定義
     * @param dataSource DEVDB1_DATASOURCEのDataSource
     * @return PlatformTransactionManager
     */
    @Bean(TRANSACTION_MANAGER)
    public PlatformTransactionManager transactionManager(
            @Autowired @Qualifier(_DATASOURCE) DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(_MYBATIS_CONF_NAME)
    @ConfigurationProperties(prefix = "mybatis.configuration")  // ここではグローバス設定でOKな体
    public org.apache.ibatis.session.Configuration mybatisConfiguration() {
        return new org.apache.ibatis.session.Configuration();
    }

    /**
     * MyBatis用のSqlSessionFactoryを定義する.
     * (note)ここで定義している理由はDevDb1DataSourceConfigと同じ。そっちを参照。
     * @param dataSource _DEVDB2_DATASOURCEのDataSource
     * @return SqlSessionFactory
     */
    @Bean(SQLSESSION_FACTORY)
    public SqlSessionFactory sqlSessionFactory(
            @Autowired @Qualifier(_DATASOURCE) DataSource dataSource) throws Exception {

        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setVfs(SpringBootVFS.class);
        factory.setConfiguration(mybatisConfiguration);

        // (note) SqlSessionFactoryBeanが持っているsetterにもっと値を詰めたければここで実装して

        return factory.getObject();
    }

    /**
     * MyBatis用SqlSessionTemplateを定義する.
     * (note)ここで定義している理由はDevDb1DataSourceConfigと同じ。そっちを参照。
     *
     * @param sqlSessionFactory _DEVDB2_SQLSESSION_FACTORYでBean定義したSqlSessionFactory
     * @return SqlSessionTemplate
     */
    @Bean("devdb2SqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(
            @Autowired @Qualifier(SQLSESSION_FACTORY) SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
