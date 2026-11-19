package com.sharpcrm.controller;

import com.sharpcrm.common.PageDate;
import com.sharpcrm.common.Result;
import com.sharpcrm.dto.CustomerDto;
import com.sharpcrm.pojo.Customer;
import com.sharpcrm.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 客户管理接口
 */
@Tag(name = "客户管理", description = "商机转化后的客户查询与管理")
@RestController
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Operation(summary = "客户分页查询")
    @GetMapping
    public Result findPage(CustomerDto dto) {
        PageDate<Customer> page = customerService.findPage(dto);
        return Result.success(page);
    }

    @Operation(summary = "查询客户详情")
    @GetMapping("/{id:\\d+}")
    public Result findById(@PathVariable Integer id) {
        return Result.success(customerService.findByIdWithScope(id));
    }

    @Operation(summary = "客户 360 视图", description = "客户信息 + 来源线索 + 来源商机 + 全链路跟进时间线")
    @GetMapping("/{id:\\d+}/360")
    public Result find360(@PathVariable Integer id) {
        return Result.success(customerService.find360(id));
    }

    @Operation(summary = "查询全部客户")
    @GetMapping("/list")
    public Result findAll() {
        return Result.success(customerService.listWithScope());
    }
}
