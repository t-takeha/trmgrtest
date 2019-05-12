package jp.dip.cloudlet.springtest.mapper.xadb1;

import jp.dip.cloudlet.springtest.model.MyFuncTestParam;
import jp.dip.cloudlet.springtest.model.MyTbl;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface XaFuncTestMapper {

    /**
     * ストアードファンクション「MYFUNCTEST」を呼び出す
     *
     * @param param
     */
    void executeMyFuncTest(MyFuncTestParam param);

    @Select("SELECT id, name, num FROM MYTBL WHERE id = #{id}")
    MyTbl select(String id);
}
