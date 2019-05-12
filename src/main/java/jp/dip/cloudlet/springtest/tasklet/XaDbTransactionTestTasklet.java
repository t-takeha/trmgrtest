package jp.dip.cloudlet.springtest.tasklet;

import jp.dip.cloudlet.springtest.mapper.xadb1.XaFuncTestMapper;
import jp.dip.cloudlet.springtest.mapper.xadb2.XaDb2MetaGetTestMapper;
import jp.dip.cloudlet.springtest.mapper.xadb3.XaTestMapper;
import jp.dip.cloudlet.springtest.model.Db2MetaGetTest;
import jp.dip.cloudlet.springtest.model.MyFuncTestParam;
import jp.dip.cloudlet.springtest.model.Xatest;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * XA DBトランザクション確認用Taskletの実装
 */
@Component
public class XaDbTransactionTestTasklet implements Tasklet {
    private static final Logger log = LogManager.getLogger(XaDbTransactionTestTasklet.class);

    // XA専用のMapperを用意しなくてもいい。XAのDataSource定義＋MapperScan定義に含まれるMapperでさえあれば非XAと共有していてもOK
    @Autowired
    XaFuncTestMapper xaFuncTestMapper;
    //MyFuncTestMapper xaFuncTestMapper;

    // XA専用のMapperを用意しなくてもいい。XAのDataSource定義＋MapperScan定義に含まれるMapperでさえあれば非XAと共有していてもOK
    @Autowired
    XaDb2MetaGetTestMapper xaDb2MetaGetTestMapper;
    //Db2MetaGetTestMapper xaDb2MetaGetTestMapper;

    @Autowired
    XaTestMapper xaTestMapper;

    @Autowired
    @Qualifier(jp.dip.cloudlet.springtest.config.XaTransactionConfig.TRANSACTION_MANAGER)
    JtaTransactionManager jtaTransactionManager;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("##### XaDbTransactionTestTasklet.execute ######");
        log.info("### contribution : " + contribution);
        log.info("### chunkContext : " + chunkContext);

        // トランザクション開始
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        TransactionStatus transactionStatus = jtaTransactionManager.getTransaction(definition);

        List<String> idList1 = new ArrayList<>();
        List<String> idList2 = new ArrayList<>();
        List<String> idList3 = new ArrayList<>();

        try {

            /* 1個目のDataSource向けDB処理 */

            // ストアードファンクションの呼び出し
            MyFuncTestParam param1 = generateInitialParam();
            idList1.add(param1.getIn1());
            xaFuncTestMapper.executeMyFuncTest(param1);

            // もう一度呼び出し
            MyFuncTestParam param2 = generateInitialParam();
            idList1.add(param2.getIn1());
            xaFuncTestMapper.executeMyFuncTest(param2);

            // 強制ロールバック
            if (param2.getRc() == null || param2.getRc() != 0) {
                throw new RuntimeException("force runtime exception");
            }

            /* 2個目のDataSource向けDB処理 */
            Db2MetaGetTest db2MetaGetTest = new Db2MetaGetTest();
            db2MetaGetTest.setId(RandomStringUtils.randomAlphanumeric(10));
            db2MetaGetTest.setName("XATEST NAME");
            db2MetaGetTest.setNum(new BigDecimal(RandomStringUtils.randomNumeric(3)));
            db2MetaGetTest.setFchar("ＸＡ２バイト文字");
            log.info("### xaDb2MetaGetTestMapper param = " + db2MetaGetTest);
            idList2.add(db2MetaGetTest.getId());

            // SELECT処理
            Db2MetaGetTest db2MetaGetTest1 = xaDb2MetaGetTestMapper.select(db2MetaGetTest.getId());
            if (db2MetaGetTest1 == null || db2MetaGetTest1.getId() == null) {
                // INSERT処理
                xaDb2MetaGetTestMapper.insert(db2MetaGetTest);
            } else {
                // UPDATE処理
                xaDb2MetaGetTestMapper.update(db2MetaGetTest);
            }

            /* 3個目のDataSource向け処理 */
            Xatest xatest = new Xatest();
            xatest.setId(RandomStringUtils.randomAlphanumeric(10));
            xatest.setName(LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuuMMddHHmmssSSS")));
            log.info("### xaTestMapper param = " + xatest);
            idList3.add(xatest.getId());

            xaTestMapper.insert(xatest);
        } catch (Exception e) {
            jtaTransactionManager.rollback(transactionStatus);
            throw e;
        }
        jtaTransactionManager.commit(transactionStatus);

        // トランザクション完了後にレコードを確認する
        DefaultTransactionDefinition definition2 = new DefaultTransactionDefinition();
        TransactionStatus transactionStatus2 = jtaTransactionManager.getTransaction(definition2);
        try {

            // 1
            idList1.stream().forEach(id -> {
                log.info("### xaFuncTestMapper result = " + xaFuncTestMapper.select(id));
            });

            // 2
            idList2.stream().forEach(id -> {
                log.info("### XaDb2MetaGetTestMapper result = " + xaDb2MetaGetTestMapper.select(id));
            });

            // 3
            idList3.stream().map(id -> xaTestMapper.select(id))
                    .collect(Collectors.toList()).stream().forEach(r -> {
                log.info("xaTestMapper result = " + r);
            });
        } catch (Exception e) {
            jtaTransactionManager.rollback(transactionStatus2);
            throw e;
        }
        jtaTransactionManager.commit(transactionStatus2);

        return RepeatStatus.FINISHED;
    }

    private MyFuncTestParam generateInitialParam() {
        MyFuncTestParam param = new MyFuncTestParam();
        param.setIn1(LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuuMMddHHmmssSSS")));

        log.info("### param = " + param);
        return param;
    }
}
