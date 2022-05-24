package com.example;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.sql.Date;

@DataType
@Data
@Accessors(chain = true)
public class Works {

    @Property
    Integer id;

    @Property
    String title;

    @Property
    String author;

    @Property
    String press;

    @Property
    String status;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Shanghai")
    Date pressDate;
}
