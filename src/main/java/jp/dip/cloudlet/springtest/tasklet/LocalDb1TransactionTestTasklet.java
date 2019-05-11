package jp.dip.cloudlet.springtest.tasklet;

import jp.dip.cloudlet.springtest.mapper.devdb1.MyFuncTestMapper;
import jp.dip.cloudlet.springtest.model.MyFuncTestParam;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static jp.dip.cloudlet.springtest.config.DevDb1DataSourceConfig.TRANSACTION_MANAGER;

/**
 * ローカルDB（DevDB1）トランザクション確認用Taskletの実装
 */
@Component
public class LocalDb1TransactionTestTasklet implements Tasklet {
    private static final Logger log = LogManager.getLogger(LocalDb1TransactionTestTasklet.class);

    @Autowired
    MyFuncTestMapper myFuncTestMapper;

    @Override
    @Transactional(transactionManager = TRANSACTION_MANAGER)
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("##### LocalDb1TransactionTestTasklet.execute ######");
        log.info("### contribution : " + contribution);
        log.info("### chunkContext : " + chunkContext);

        // ストアードファンクションの呼び出し
        MyFuncTestParam param = generateInitialParam();

        myFuncTestMapper.executeMyFuncTest(param);
        log.info("### result1 = " + param);

        // もう一度呼び出し
        MyFuncTestParam param2 = generateInitialParam();
        myFuncTestMapper.executeMyFuncTest(param2);
        log.info("### result2 = " + param2);

        // 強制ロールバック
        if (param2.getRc() == null || param2.getRc() != 0) {
            throw new RuntimeException("force runtime exception");
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
