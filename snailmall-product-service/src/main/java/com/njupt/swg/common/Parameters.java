package com.njupt.swg.common;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


/**
 * @Author swg.
 * @Date 2019/1/1 14:27
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@Component
@Data
public class Parameters {
    @Value("${image.host}")
    private String imageHost;
}
