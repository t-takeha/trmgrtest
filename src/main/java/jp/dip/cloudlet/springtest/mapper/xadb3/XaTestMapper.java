package jp.dip.cloudlet.springtest.mapper.xadb3;

import jp.dip.cloudlet.springtest.model.Xatest;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface XaTestMapper {

    @Insert("INSERT INTO xatest (id, name) VALUES (#{id},#{name})")
    void insert(Xatest param);

    @Select("SELECT id, name FROM xatest WHERE id = #{id}")
    List<Xatest> select(String id);
}
