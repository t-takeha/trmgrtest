package jp.dip.cloudlet.springtest.tasklet;

import jp.dip.cloudlet.springtest.mapper.xadb3.TableInitializeMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static jp.dip.cloudlet.springtest.config.XaTransactionConfig.TRANSACTION_MANAGER;

/**
 * H2(xaテスト用）を初期化するタスクレット.
 */
@Component
public class InitH2DbTasklet implements Tasklet {
    private  static final Logger log = LogManager.getLogger();

    @Autowired
    TableInitializeMapper tableInitializeMapper;

    @Override
    @Transactional(transactionManager = TRANSACTION_MANAGER)
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("##### InitH2DbTasklet start #####");

        tableInitializeMapper.dropTable();
        tableInitializeMapper.createTable();

        return RepeatStatus.FINISHED;
    }
}
