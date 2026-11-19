package com.sharpcrm.controller;

import com.sharpcrm.common.PageDate;
import com.sharpcrm.common.Result;
import com.sharpcrm.dto.ContractDto;
import com.sharpcrm.pojo.Contract;
import com.sharpcrm.pojo.Payment;
import com.sharpcrm.service.ContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 合同与回款接口
 */
@Tag(name = "合同与回款", description = "合同签订、分期回款计划与回款登记")
@RestController
@RequestMapping("/contracts")
public class ContractController {

    @Autowired
    private ContractService contractService;

    @Operation(summary = "合同分页查询")
    @GetMapping
    public Result findPage(ContractDto dto) {
        PageDate<Contract> page = contractService.findPage(dto);
        return Result.success(page);
    }

    @Operation(summary = "合同详情", description = "含每期回款计划、实收情况与回款进度")
    @GetMapping("/{id:\\d+}")
    public Result findDetail(@PathVariable Integer id) {
        return Result.success(contractService.findDetail(id));
    }

    @Operation(summary = "签订合同", description = "签订时按付款方式自动生成回款计划")
    @PostMapping
    public Result create(@RequestBody Contract contract,
                         @RequestParam(required = false) Integer periods) {
        return Result.success(contractService.createContract(contract, periods));
    }

    @Operation(summary = "修改合同")
    @PutMapping
    public Result update(@RequestBody Contract contract) {
        contractService.updateContract(contract);
        return Result.success();
    }

    @Operation(summary = "删除合同", description = "级联删除回款计划")
    @DeleteMapping("/{id}")
    public Result remove(@PathVariable Integer id) {
        contractService.removeContract(id);
        return Result.success();
    }

    @Operation(summary = "回款登记", description = "登记某一期的实收金额与日期，全部收齐后合同自动置为已完成")
    @PutMapping("/payment")
    public Result registerPayment(@RequestBody Payment payment) {
        contractService.registerPayment(payment);
        return Result.success();
    }
}
