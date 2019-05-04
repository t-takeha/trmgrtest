package jp.dip.cloudlet.springtest.model;

import java.math.BigDecimal;

/**
 * DB2_META_GET_TESTテーブル用Entity
 */
public class Db2MetaGetTest {

    /* Map先：char(10) */
    private String id;
    /* Map先：varchar(20) */
    private String name;
    /* Map先：decimal(3) */
    private BigDecimal num;
    /* Map先：graphic(20) */
    private String fchar;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getNum() {
        return num;
    }

    public void setNum(BigDecimal num) {
        this.num = num;
    }

    public String getFchar() {
        return fchar;
    }

    public void setFchar(String fchar) {
        this.fchar = fchar;
    }

    @Override
    public String toString() {
        return "Db2MetaGetTest{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", num=" + num +
                ", fchar='" + fchar + '\'' +
                '}';
    }
}
