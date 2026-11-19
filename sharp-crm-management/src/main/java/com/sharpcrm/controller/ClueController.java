package com.sharpcrm.controller;

import com.sharpcrm.common.PageDate;
import com.sharpcrm.common.Result;
import com.sharpcrm.common.annotation.RequireRole;
import com.sharpcrm.dto.ClueDto;
import com.sharpcrm.pojo.Clue;
import com.sharpcrm.service.ClueService;
import com.sharpcrm.validation.Create;
import com.sharpcrm.validation.Update;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 线索控制器，处理线索的增删改查、分配、跟进、转商机等接口
 */
@Slf4j
@RestController
@RequestMapping("/clues")
public class ClueController {

    @Autowired
    private ClueService clueService;

    /**
     * 线索分页查询
     */
    @GetMapping
    public Result findPage(ClueDto clueDto) {
        PageDate<Clue> pageDates = clueService.findPage(clueDto);
        return Result.success(pageDates);
    }

    /**
     * 新增线索
     */
    @PostMapping
    public Result addClue(@Validated(Create.class) @RequestBody Clue clue) {
        clueService.saveOrUpdate(clue);
        return Result.success();
    }

    /**
     * 分配线索给指定用户
     */
    @RequireRole({"admin", "clue_operator"})
    @PutMapping("/assign/{clueId}/{userId}")
    public Result updateAssign(@PathVariable Integer clueId, @PathVariable Integer userId) {
        clueService.updateAssign(clueId, userId);
        return Result.success();
    }

    /**
     * 根据id查询线索详情
     */
    @GetMapping("/{id:\\d+}")
    public Result findById(@PathVariable Integer id) {
        Clue clue = clueService.findById(id);
        return Result.success(clue);
    }

    /**
     * 跟进线索
     */
    @PutMapping
    public Result updateFollow(@Validated(Update.class) @RequestBody Clue clue) {
        clueService.updateFollow(clue);
        return Result.success();
    }

    /**
     * 标记为伪线索并跟进
     */
    @PutMapping("/false/{id}")
    public Result updateFalseFollow(@PathVariable Integer id, @RequestBody ClueDto clueDto) {
        clueService.updateFalseFollow(id, clueDto);
        return Result.success();
    }

    /**
     * 线索转商机
     */
    @PutMapping("/toBusiness/{id}")
    public Result updateToBusiness(@PathVariable Integer id) {
        clueService.updateToBusiness(id);
        return Result.success();
    }

    /**
     * 线索池分页查询
     */
    @GetMapping("/pool")
    public Result findPoolPage(ClueDto clueDto) {
        PageDate<Clue> pageDates = clueService.findPoolPage(clueDto);
        return Result.success(pageDates);
    }

    /**
     * Excel 批量导入线索
     */
    @PostMapping("/import")
    public Result importExcel(@org.springframework.web.bind.annotation.RequestPart("file")
                              org.springframework.web.multipart.MultipartFile file) throws Exception {
        java.util.List<com.sharpcrm.vo.ClueExcel> rows = new java.util.ArrayList<>();
        com.alibaba.excel.EasyExcel.read(file.getInputStream(), com.sharpcrm.vo.ClueExcel.class,
                new com.alibaba.excel.read.listener.ReadListener<com.sharpcrm.vo.ClueExcel>() {
                    @Override
                    public void invoke(com.sharpcrm.vo.ClueExcel data,
                                       com.alibaba.excel.context.AnalysisContext context) {
                        rows.add(data);
                    }

                    @Override
                    public void doAfterAllAnalysed(com.alibaba.excel.context.AnalysisContext context) {
                    }
                }).sheet().doRead();
        int count = clueService.importExcel(rows);
        return Result.success("成功导入 " + count + " 条线索", count);
    }

    /**
     * 导出线索 Excel
     */
    @GetMapping("/export")
    public void exportExcel(jakarta.servlet.http.HttpServletResponse response) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=clues.xlsx");
        com.alibaba.excel.EasyExcel.write(response.getOutputStream(), com.sharpcrm.vo.ClueExcel.class)
                .sheet("线索数据")
                .doWrite(clueService.exportExcel());
    }

}
