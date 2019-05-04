package jp.dip.cloudlet.springtest.mapper.xadb2;

import jp.dip.cloudlet.springtest.model.Db2MetaGetTest;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface XaDb2MetaGetTestMapper {
    /**
     * DB2_META_GET_TESTにdb2MetaGetTestが示すレコードをINSERTする
     * @param db2MetaGetTest 登録レコード
     */
    @Insert("insert into DB2_META_GET_TEST (id, name, num, fchar) values (#{id}, #{name}, #{num}, #{fchar})")
    void insert(Db2MetaGetTest db2MetaGetTest);

    /**
     * DB2_META_GET_TESTをUPDATEする
     * @param db2MetaGetTest
     */
    @Update("update DB2_META_GET_TEST set name = #{name}, num = #{num}, fchar = #{fchar} where id = #{id}")
    void update(Db2MetaGetTest db2MetaGetTest);

    /**
     * idをもとにDB2_META_GET_TESTを検索する.
     * (note)
     *   fcharはGRAPHIC型だが、JDBCではgetNStringで取得しようとする。
     *   しかし、DB2のJCCドライバではgetNStringをサポートしていないためエラーになる。
     *   仕方がないので、NCHAR関数にかけて取得することにした。
     * @param id プライマリキー
     * @return Db2MetaGetTest
     */
    @Select("select id, name, num, NCHAR(fchar) as fchar from DB2_META_GET_TEST where id = #{id}")
    Db2MetaGetTest select(String id);
}
