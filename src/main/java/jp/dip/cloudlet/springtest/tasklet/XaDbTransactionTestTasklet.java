package jp.dip.cloudlet.springtest.tasklet;

import jp.dip.cloudlet.springtest.mapper.xadb1.XaFuncTestMapper;
import jp.dip.cloudlet.springtest.mapper.xadb2.XaDb2MetaGetTestMapper;
import jp.dip.cloudlet.springtest.model.Db2MetaGetTest;
import jp.dip.cloudlet.springtest.model.MyFuncTestParam;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static jp.dip.cloudlet.springtest.config.XaTransactionConfig.TRANSACTION_MANAGER;

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

    @Override
    @Transactional(transactionManager = TRANSACTION_MANAGER)
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("##### XaDbTransactionTestTasklet.execute ######");
        log.info("### contribution : " + contribution);
        log.info("### chunkContext : " + chunkContext);

        /* 1個目のDataSource向けDB処理 */

        // ストアードファンクションの呼び出し
        MyFuncTestParam param = generateInitialParam();

        xaFuncTestMapper.executeMyFuncTest(param);
        log.info("### result_xa1 = " + param);

        // もう一度呼び出し
        MyFuncTestParam param2 = generateInitialParam();
        xaFuncTestMapper.executeMyFuncTest(param2);
        log.info("### result_xa2 = " + param2);

        // 強制ロールバック
        if (param2.getRc() == null || param2.getRc() != 0) {
            throw new RuntimeException("force runtime exception");
        }

        /* 2個目のDataSource向けDB処理 */

        // INSERT用パラメータ生成(idは自動採番）
        Db2MetaGetTest db2MetaGetTest = new Db2MetaGetTest();
        db2MetaGetTest.setId(RandomStringUtils.randomAlphanumeric(10));
        db2MetaGetTest.setName("XATEST NAME");
        db2MetaGetTest.setNum(new BigDecimal(RandomStringUtils.randomNumeric(3)));
        db2MetaGetTest.setFchar("ＸＡ２バイト文字");
        log.info("### param(xa) = " + db2MetaGetTest);

        // SELECT処理
        Db2MetaGetTest db2MetaGetTest1 = xaDb2MetaGetTestMapper.select(db2MetaGetTest.getId());
        if (db2MetaGetTest1 == null || db2MetaGetTest1.getId() == null) {
            // INSERT処理
            xaDb2MetaGetTestMapper.insert(db2MetaGetTest);

            // SELECT処理
            db2MetaGetTest1 = xaDb2MetaGetTestMapper.select(db2MetaGetTest.getId());
            log.info("### Db2MetaGetTest(after insert xa) = " + db2MetaGetTest1);
        } else {
            // UPDATE処理
            xaDb2MetaGetTestMapper.update(db2MetaGetTest);

            // SELECT処理
            db2MetaGetTest1 = xaDb2MetaGetTestMapper.select(db2MetaGetTest.getId());
            log.info("### Db2MetaGetTest(after update xa) = " + db2MetaGetTest1);
        }

        return RepeatStatus.FINISHED;
    }

    private MyFuncTestParam generateInitialParam() {
        MyFuncTestParam param = new MyFuncTestParam();
        param.setIn1(LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuuMMddHHmmssSSS")));

        log.info("### param = " + param);
        return param;
    }
}
