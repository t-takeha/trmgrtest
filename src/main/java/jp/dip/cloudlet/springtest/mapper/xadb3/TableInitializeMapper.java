package jp.dip.cloudlet.springtest.mapper.xadb3;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TableInitializeMapper {
    /**
     * テスト用のテーブルが作成済みであればDropする.
     */
    @Update("DROP TABLE IF EXISTS xatest")
    void dropTable();

    /**
     * テスト用のテーブルを作成する.
     */
    @Update("CREATE TABLE IF NOT EXISTS xatest (id CHAR(10), name VARCHAR(20))")
    void createTable();
}
