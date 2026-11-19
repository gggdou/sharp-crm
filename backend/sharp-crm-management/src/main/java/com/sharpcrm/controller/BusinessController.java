package com.sharpcrm.controller;

import com.sharpcrm.common.PageDate;
import com.sharpcrm.common.Result;
import com.sharpcrm.common.annotation.RequireRole;
import com.sharpcrm.dto.BusinessDto;
import com.sharpcrm.pojo.Business;
import com.sharpcrm.service.BusinessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 商机控制器，处理商机的增删改查、分配、跟进、踢回公海等接口
 */
@Slf4j
@RestController
@RequestMapping("/businesses")
public class BusinessController {

    @Autowired
    private BusinessService businessService;

    /**
     * 商机分页查询
     */
    @GetMapping
    public Result findPage(BusinessDto businessDto) {
        PageDate<Business> pageDates = businessService.findPage(businessDto);
        return Result.success(pageDates);
    }

    /**
     * 新增商机
     */
    @PostMapping
    public Result addBusiness(@Validated(com.sharpcrm.validation.Create.class) @RequestBody Business business) {
        log.info("添加商机，参数：{}", business);
        businessService.saveOrUpdate(business);
        return Result.success();
    }

    /**
     * 分配商机给指定用户
     */
    @RequireRole({"admin", "business_operator"})
    @PutMapping("/assign/{businessId}/{userId}")
    public Result updateAssign(@PathVariable Integer businessId, @PathVariable Integer userId) {
        businessService.updateAssign(businessId, userId);
        return Result.success();
    }

    /**
     * 商机踢回公海
     */
    @RequireRole({"admin", "business_operator"})
    @PutMapping("/back/{id}")
    public Result updateBack(@PathVariable Integer id) {
        businessService.updateBack(id);
        return Result.success();
    }

    /**
     * 根据id查询商机详情
     */
    @GetMapping("/{id}")
    public Result findById(@PathVariable Integer id) {
        Business business = businessService.findById(id);
        return Result.success(business);
    }

    /**
     * 跟进商机（添加跟进记录）
     */
    @PutMapping
    public Result updateFollow(@RequestBody Business business) {
        businessService.updateFollow(business);
        return Result.success();
    }

    /**
     * 公海池分页查询（status=1 且 userId 为空）
     */
    @GetMapping("/pool")
    public Result findPoolPage(BusinessDto businessDto) {
        PageDate<Business> pageDates = businessService.findPoolPage(businessDto);
        return Result.success(pageDates);
    }

    /**
     * 商机转客户
     */
    @PutMapping("/toCustomer/{id}")
    public Result toCustomer(@PathVariable Integer id) {
        Integer customerId = businessService.toCustomer(id);
        return Result.success(customerId);
    }

}
