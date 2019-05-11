package jp.dip.cloudlet.springtest.tasklet;

import jp.dip.cloudlet.springtest.mapper.devdb2.Db2MetaGetTestMapper;
import jp.dip.cloudlet.springtest.model.Db2MetaGetTest;
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

import static jp.dip.cloudlet.springtest.config.DevDb2DataSourceConfig.TRANSACTION_MANAGER;

/**
 * ローカルDB（DevDb2）トランザクション確認用Taskletの実装
 */
@Component
public class LocalDb2TransactionTestTasklet implements Tasklet {
    private static final Logger log = LogManager.getLogger(LocalDb2TransactionTestTasklet.class);

    @Autowired
    Db2MetaGetTestMapper db2MetaGetTestMapper;

    @Override
    @Transactional(transactionManager = TRANSACTION_MANAGER)
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("##### LocalDb2TransactionTestTasklet.execute ######");
        log.info("### contribution : " + contribution);
        log.info("### chunkContext : " + chunkContext);

        // INSERT用パラメータ生成(idは自動採番）
        Db2MetaGetTest db2MetaGetTest = new Db2MetaGetTest();
        db2MetaGetTest.setId(RandomStringUtils.randomAlphanumeric(10));
        db2MetaGetTest.setName("DUMMY NAME");
        db2MetaGetTest.setNum(new BigDecimal(RandomStringUtils.randomNumeric(3)));
        db2MetaGetTest.setFchar("２バイト文字");
        log.info("### param = " + db2MetaGetTest);

        // SELECT処理
        Db2MetaGetTest db2MetaGetTest1 = db2MetaGetTestMapper.select(db2MetaGetTest.getId());
        if (db2MetaGetTest1 == null || db2MetaGetTest1.getId() == null) {
            // INSERT処理
            db2MetaGetTestMapper.insert(db2MetaGetTest);

            // SELECT処理
            db2MetaGetTest1 = db2MetaGetTestMapper.select(db2MetaGetTest.getId());
            log.info("### Db2MetaGetTest(after insert) = " + db2MetaGetTest1);
        } else {
            // UPDATE処理
            db2MetaGetTestMapper.update(db2MetaGetTest);

            // SELECT処理
            db2MetaGetTest1 = db2MetaGetTestMapper.select(db2MetaGetTest.getId());
            log.info("### Db2MetaGetTest(after update) = " + db2MetaGetTest1);
        }

//        // 強制ロールバック
//        if (db2MetaGetTest1.getId() == null) {
//            throw new RuntimeException("force runtime exception");
//        }

        return RepeatStatus.FINISHED;
    }
}
