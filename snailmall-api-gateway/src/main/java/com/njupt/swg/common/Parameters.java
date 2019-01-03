package com.njupt.swg.common;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author swg.
 * @Date 2019/1/1 14:27
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@Component
@Data
public class Parameters {
    /*****redis config start*******/
    @Value("${redis.host}")
    private String redisHost;
    @Value("${redis.port}")
    private int redisPort;
    @Value("${redis.max-idle}")
    private int redisMaxTotal;
    @Value("${redis.max-total}")
    private int redisMaxIdle;
    @Value("${redis.max-wait-millis}")
    private int redisMaxWaitMillis;
    /*****redis config end*******/

    @Value("#{'${security.noneSecurityAdminPaths}'.split(',')}")
    private List<String> noneSecurityAdminPaths;
}
