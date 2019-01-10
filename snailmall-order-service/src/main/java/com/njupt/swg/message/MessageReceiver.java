package com.njupt.swg.message;

import com.njupt.swg.common.utils.JsonUtil;
import com.njupt.swg.service.IOrderService;
import com.njupt.swg.vo.MessageVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author swg.
 * @Date 2019/1/7 13:23
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@Component
@Slf4j
public class MessageReceiver {
    @Autowired
    private IOrderService orderService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("order-queue"),
            exchange = @Exchange("order-exchange")
    ))
    public void proess(String message){
        log.info("接收到的消息为:{}",message);
        List<MessageVo> result = JsonUtil.Str2Obj(message, List.class, MessageVo.class);
        log.info("【MQ解析数据,前者为userId,后者为product信息：{}】",result);
        //扣减库存、下订单
        //是先扣减库存，扣减成功才可以下订单，但是这是两个数据库，那么属于跨库的事务，所以如何解决呢？
        //一种方案是：利用消息队列，订单服务订阅扣减库存服务，一旦发现数据库的库存扣减成功，就去扣减插入订单；
        //如果库存扣减不成功，那么订单也不会写入
        orderService.stockAndOrderprocess(result);
    }

}
