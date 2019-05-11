package jp.dip.cloudlet.springtest.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = {"jp.dip.cloudlet.springtest.mapper.xadb1"},
        sqlSessionFactoryRef = XaDb1DataSourceConfig.SQLSESSION_FACTORY)
public class XaDb1DataSourceConfig {

    /**
     * SqlSessionFactory名
     */
    public static final String SQLSESSION_FACTORY = "xadb1SqlSessionFactory";

    /**
     * データソース名
     */
    private static final String _DATASOURCE = "xadb1DataSource";

    /**
     * このデータソース専用のMyBatis用Configuration
     */
    private static final String _MYBATIS_CONF_NAME = "xadb1MybatisConfiguration";

    /**
     * MyBatisのConfigurationをAutowiredする
     */
    @Autowired
    @Qualifier(_MYBATIS_CONF_NAME)
    org.apache.ibatis.session.Configuration mybatisConfiguration;

    /**
     * XA用データソースの定義（AtomikosのBeanを使用する）
     *
     * @return
     */
    @Bean(_DATASOURCE)
    @ConfigurationProperties(prefix = "spring.jta.atomikos.datasource.xadb1")
    public DataSource dataSource() {
        return new AtomikosDataSourceBean();
    }

    @Bean(_MYBATIS_CONF_NAME)
    @ConfigurationProperties(prefix = "mybatis.configuration")  // ここではグローバス設定でOKな体
    public org.apache.ibatis.session.Configuration mybatisConfiguration() {
        return new org.apache.ibatis.session.Configuration();
    }

    /**
     * MyBatis用のSqlSessionFactoryを定義する.
     * (note)ここで定義している理由はDevDb1DataSourceConfigと同じ。そっちを参照。
     *
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
     * @param sqlSessionFactory _XA_SQLSESSION_FACTORYでBean定義したSqlSessionFactory
     * @return SqlSessionTemplate
     */
    @Bean("xadb1SqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(
            @Autowired @Qualifier(SQLSESSION_FACTORY) SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
