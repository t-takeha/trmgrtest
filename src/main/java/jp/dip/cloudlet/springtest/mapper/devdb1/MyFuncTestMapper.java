package jp.dip.cloudlet.springtest.mapper.devdb1;

import jp.dip.cloudlet.springtest.model.MyFuncTestParam;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MyFuncTestMapper {

    /**
     * ストアードファンクション「MYFUNCTEST」を呼び出す
     *
     * @param param
     */
    void executeMyFuncTest(MyFuncTestParam param);
}
