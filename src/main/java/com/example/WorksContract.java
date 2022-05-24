package com.example;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import lombok.extern.java.Log;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.hyperledger.fabric.shim.ledger.QueryResultsIteratorWithMetadata;

import java.sql.Date;
import java.util.List;
import java.util.logging.Level;


@Contract(
        name = "CatContract",
        info = @Info(
                title = "Cat contract",
                description = "The hyperlegendary car contract",
                version = "0.0.1-SNAPSHOT",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "f.carr@example.com",
                        name = "F Carr",
                        url = "https://hyperledger.example.com")))
@Default
@Log
public class WorksContract implements ContractInterface {


    @Transaction
    public Works queryWorks(final Context ctx, final String author) {

        ChaincodeStub stub = ctx.getStub();
        String worksState = stub.getStringState(author);

        if (StringUtils.isBlank(worksState)) {
            String errorMessage = String.format("Works %s does not exist.", author);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        return JSON.parseObject(worksState , Works.class);
    }

    @Transaction
    public WorksQueryResultList queryWorksByName(final Context ctx, String author) {

        log.info(String.format("使用 author 查询 works , author = %s" , author));

        String query = String.format("{\"selector\":{\"author\":\"%s\"} }", author);

        log.info(String.format("query string = %s" , query));
        return queryWorks(ctx.getStub() , query);
    }

    @Transaction
    public WorksQueryPageResult queryWorksPageByName(final Context ctx, String author , Integer pageSize , String bookmark) {

        log.info(String.format("使用 author 分页查询 works , name = %s" , author));

        String query = String.format("{\"selector\":{\"author\":\"%s\"} }", author);

        log.info(String.format("query string = %s" , query));

        ChaincodeStub stub = ctx.getStub();
        QueryResultsIteratorWithMetadata<KeyValue> queryResult = stub.getQueryResultWithPagination(query, pageSize, bookmark);

        List<WorksQueryResult> works = Lists.newArrayList();

        if (! IterableUtils.isEmpty(queryResult)) {
            for (KeyValue kv : queryResult) {
                works.add(new WorksQueryResult().setKey(kv.getKey()).setWorks(JSON.parseObject(kv.getStringValue() , Works.class)));
            }
        }

        return new WorksQueryPageResult()
                .setWorks(works)
                .setBookmark(queryResult.getMetadata().getBookmark());
    }


    private WorksQueryResultList queryWorks(ChaincodeStub stub , String query) {

        WorksQueryResultList resultList = new WorksQueryResultList();
        QueryResultsIterator<KeyValue> queryResult = stub.getQueryResult(query);
        List<WorksQueryResult> results = Lists.newArrayList();

        if (! IterableUtils.isEmpty(queryResult)) {
            for (KeyValue kv : queryResult) {
                results.add(new WorksQueryResult().setKey(kv.getKey()).setWorks(JSON.parseObject(kv.getStringValue() , Works.class)));
            }
            resultList.setWorks(results);
        }

        return resultList;
    }

    @Transaction
    public Works saveWorks(final Context ctx, final String key , Integer  id, String title , String author , String press, String status, Date pressDate) {

        ChaincodeStub stub = ctx.getStub();
        String worksState = stub.getStringState(key);

        if (StringUtils.isNotBlank(worksState)) {
            String errorMessage = String.format("Works %s already exists", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        Works works = new Works().setId(id)
                .setTitle(title)
                .setPress(press)
                .setAuthor(author)
                .setStatus(status)
                .setPressDate(pressDate);

        String json = JSON.toJSONString(works);
        stub.putStringState(key, json);

        stub.setEvent("createWorksEvent" , org.apache.commons.codec.binary.StringUtils.getBytesUtf8(json));
        return works;
    }

    @Transaction
    public Works updateWorks(final Context ctx, final String key , Integer  id, String title , String author , String press, String status, Date pressDate) {

        ChaincodeStub stub = ctx.getStub();
        String worksState = stub.getStringState(key);

        if (StringUtils.isBlank(worksState)) {
            String errorMessage = String.format("Works %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        Works works = new Works().setId(id)
                .setTitle(title)
                .setPress(press)
                .setAuthor(author)
                .setStatus(status)
                .setPressDate(pressDate);

        stub.putStringState(key, JSON.toJSONString(works));

        return works;
    }

    @Transaction
    public Works deleteWorks(final Context ctx, final String key) {

        ChaincodeStub stub = ctx.getStub();
        String worksState = stub.getStringState(key);

        if (StringUtils.isBlank(worksState)) {
            String errorMessage = String.format("Works %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        stub.delState(key);

        return JSON.parseObject(worksState , Works.class);
    }

//    @Transaction
//    public byte[] queryPrivateWorksHash(final Context ctx, final String collection ,final String key) {
//
//        ChaincodeStub stub = ctx.getStub();
//
//        byte[] hash = stub.getPrivateDataHash(collection, key);
//
//        if (ArrayUtils.isEmpty(hash)) {
//            String errorMessage = String.format("Private Works %s does not exist", key);
//            log.log(Level.WARNING , errorMessage);
//            throw new ChaincodeException(errorMessage);
//        }
//
//        return hash;
//    }

    @Override
    public void beforeTransaction(Context ctx) {
        log.info("*************************************** beforeTransaction ***************************************");
    }

    @Override
    public void afterTransaction(Context ctx, Object result) {
        log.info("*************************************** afterTransaction ***************************************");
        System.out.println("result --------> " + result);
    }
}
