package com.njupt.swg.keygen;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 基于ZK节点序列
 */
@Component
@Slf4j
public class WorkerIDSenquence {

    @Value("${zk.host}")
    private String ZkHost ;

    private static final String ZK_PATH = "/snowflake/workID";

    private static CuratorFramework client;

    @PostConstruct
    void initZKNode() throws Exception {
        client = CuratorFrameworkFactory.newClient(ZkHost,new RetryNTimes(10, 5000));
        client.start();
        log.info("zk client start successfully!");
        Stat stat = client.checkExists().forPath(ZK_PATH);
        if (stat==null){
            client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(ZK_PATH);
        }
    }

    public  long getSequence(String hostname) throws Exception {
        if(StringUtils.isBlank(hostname)){
            hostname = "snowflake_";
        }
        String path = client.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(ZK_PATH+"/"+hostname);
        // snowflake_0000000000
        long sequence = Long.valueOf(path.substring(path.length()-4,path.length()));
        return sequence;
    }



}