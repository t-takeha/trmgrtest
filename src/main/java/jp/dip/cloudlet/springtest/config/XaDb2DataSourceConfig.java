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
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = {"jp.dip.cloudlet.springtest.mapper.xadb2"},
        sqlSessionFactoryRef = "xadb2SqlSessionFactory")
public class XaDb2DataSourceConfig {

    /**
     * データソース名
     */
    private static final String _XADB2_DATASOURCE = "xadb2DataSource";

    /**
     * SqlSessionFactory名
     */
    private static final String _XADB2_SQLSESSION_FACTORY = "xadb2SqlSessionFactory";

    /**
     * MyBatisの自動コンフィグにより有効になるMyBatis用プロパティをAutowiredする
     */
    @Autowired
    private MybatisProperties mybatisProperties;

    /**
     * XA用データソースの定義（AtomikosのBeanを使用する）
     * @return
     */
    @Bean(_XADB2_DATASOURCE)
    @ConfigurationProperties(prefix = "spring.jta.atomikos.datasource.xadb2")
    public DataSource dataSource() {
        return new AtomikosDataSourceBean();
    }

    /**
     * MyBatis用のSqlSessionFactoryを定義する.
     * (note)ここで定義している理由はDevDb1DataSourceConfigと同じ。そっちを参照。
     * @param dataSource _DEVDB2_DATASOURCEのDataSource
     * @return SqlSessionFactory
     */
    @Bean(_XADB2_SQLSESSION_FACTORY)
    public SqlSessionFactory sqlSessionFactory(
            @Autowired @Qualifier(_XADB2_DATASOURCE) DataSource dataSource) throws Exception {

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
     * @param sqlSessionFactory _XA_SQLSESSION_FACTORYでBean定義したSqlSessionFactory
     * @return SqlSessionTemplate
     */
    @Bean("xadb2SqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(
            @Autowired @Qualifier(_XADB2_SQLSESSION_FACTORY) SqlSessionFactory sqlSessionFactory) {

        ExecutorType executorType = mybatisProperties.getExecutorType();
        if (executorType != null) {
            return new SqlSessionTemplate(sqlSessionFactory, executorType);
        } else {
            return new SqlSessionTemplate(sqlSessionFactory);
        }
    }
}
