package com.njupt.swg.common.utils;

import com.njupt.swg.cache.Parameters;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @Author swg.
 * @Date 2019/1/1 17:57
 * @CONTACT 317758022@qq.com
 * @DESC zk的curator客户端
 */
@Component
public class ZkClient {
    @Autowired
    private Parameters parameters;

    @Bean
    public CuratorFramework getZkClient(){

        CuratorFrameworkFactory.Builder builder= CuratorFrameworkFactory.builder()
                .connectString(parameters.getZkHost())
                .connectionTimeoutMs(3000)
                .retryPolicy(new RetryNTimes(5, 10));
        CuratorFramework framework = builder.build();
        framework.start();
        return framework;

    }
}
