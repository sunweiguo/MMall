package com.njupt.swg.clients;

import com.njupt.swg.common.resp.ServerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author swg.
 * @Date 2019/1/3 16:56
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@FeignClient("category-service")
public interface CategoryClient {
    @RequestMapping("/manage/category/get_category_detail.do")
    ServerResponse getCategoryDetail(@RequestParam("categoryId") Integer categoryId);

    @RequestMapping("/manage/category/get_deep_category.do")
    ServerResponse getDeepCategory(@RequestParam(value = "categoryId") Integer categoryId);
}
