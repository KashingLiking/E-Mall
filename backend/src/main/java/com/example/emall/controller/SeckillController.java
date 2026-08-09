package com.example.emall.controller;

import com.example.emall.service.SeckillService;
import com.example.emall.vo.ResponseVO;
import com.example.emall.vo.SeckillActivityVO;
import com.example.emall.vo.SeckillResultVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/seckill")
public class SeckillController {

    @Resource
    private SeckillService seckillService;

    @GetMapping
    public ResponseVO<List<SeckillActivityVO>> getActivities() {
        return ResponseVO.buildSuccess(seckillService.getActivities());
    }

    @PostMapping
    public ResponseVO<SeckillActivityVO> createActivity(@RequestBody SeckillActivityVO vo) {
        return ResponseVO.buildSuccess(seckillService.createActivity(vo));
    }

    @PutMapping("/{id}")
    public ResponseVO<SeckillActivityVO> updateActivity(
            @PathVariable Integer id, @RequestBody SeckillActivityVO vo) {
        return ResponseVO.buildSuccess(seckillService.updateActivity(id, vo));
    }

    @DeleteMapping("/{id}")
    public ResponseVO<String> deleteActivity(@PathVariable Integer id) {
        seckillService.deleteActivity(id);
        return ResponseVO.buildSuccess("删除成功");
    }

    @PostMapping("/{activityId}/buy")
    public ResponseVO<SeckillResultVO> doSeckill(@PathVariable Integer activityId) {
        return ResponseVO.buildSuccess(seckillService.doSeckill(activityId));
    }
}
