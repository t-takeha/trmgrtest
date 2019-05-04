package jp.dip.cloudlet.springtest.config;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
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
        sqlSessionFactoryRef = "devdb2SqlSessionFactory")
public class DevDb2DataSourceConfig {
    /**
     * トランザクションマネージャ名（外からの参照あり）
     */
    public static final String DEVDB2_TRANSACTION_MANAGER = "devdb2TransactionManager" ;

    /**
     * データソース名
     */
    private static final String _DEVDB2_DATASOURCE = "devdb2DataSource";

    /**
     * SqlSessionFactory名
     */
    private static final String _DEVDB2_SQLSESSION_FACTORY = "devdb2SqlSessionFactory";

    /**
     * MyBatisの自動コンフィグにより有効になるMyBatis用プロパティをAutowiredする
     */
    @Autowired
    private MybatisProperties mybatisProperties;

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
    @Bean(_DEVDB2_DATASOURCE)
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
    @Bean(DEVDB2_TRANSACTION_MANAGER)
    public PlatformTransactionManager transactionManager(
            @Autowired @Qualifier(_DEVDB2_DATASOURCE) DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * MyBatis用のSqlSessionFactoryを定義する.
     * (note)ここで定義している理由はDevDb1DataSourceConfigと同じ。そっちを参照。
     * @param dataSource _DEVDB2_DATASOURCEのDataSource
     * @return SqlSessionFactory
     */
    @Bean(_DEVDB2_SQLSESSION_FACTORY)
    public SqlSessionFactory sqlSessionFactory(
            @Autowired @Qualifier(_DEVDB2_DATASOURCE) DataSource dataSource) throws Exception {

        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setVfs(SpringBootVFS.class);

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
            @Autowired @Qualifier(_DEVDB2_SQLSESSION_FACTORY) SqlSessionFactory sqlSessionFactory) {

        ExecutorType executorType = mybatisProperties.getExecutorType();
        if (executorType != null) {
            return new SqlSessionTemplate(sqlSessionFactory, executorType);
        } else {
            return new SqlSessionTemplate(sqlSessionFactory);
        }
    }
}
