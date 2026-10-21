package cn.itcast.controller;

import cn.itcast.common.PageDate;
import cn.itcast.common.Result;
import cn.itcast.dto.ClueDto;
import cn.itcast.pojo.Clue;
import cn.itcast.service.ClueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Result addClue(@RequestBody Clue clue) {
        clueService.saveOrUpdate(clue);
        return Result.success();
    }

    /**
     * 分配线索给指定用户
     */
    @PutMapping("/assign/{clueId}/{userId}")
    public Result updateAssign(@PathVariable Integer clueId, @PathVariable Integer userId) {
        clueService.updateAssign(clueId, userId);
        return Result.success();
    }

    /**
     * 根据id查询线索详情
     */
    @GetMapping("/{id}")
    public Result findById(@PathVariable Integer id) {
        Clue clue = clueService.findById(id);
        return Result.success(clue);
    }

    /**
     * 跟进线索
     */
    @PutMapping
    public Result updateFollow(@RequestBody Clue clue) {
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

}
