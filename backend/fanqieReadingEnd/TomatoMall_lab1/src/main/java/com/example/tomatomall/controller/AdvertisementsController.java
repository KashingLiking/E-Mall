package com.example.tomatomall.controller;

import com.example.tomatomall.service.AdvertisementsService;
import com.example.tomatomall.vo.AdvertisementsVO;
import com.example.tomatomall.vo.ResponseVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/advertisements")
public class AdvertisementsController {
    @Resource
    AdvertisementsService advertisementsService;
    /**
     * 获取所有广告信息
     */
    @GetMapping
    public ResponseVO<List<AdvertisementsVO>> getAdvertisements() {
        return ResponseVO.buildSuccess(advertisementsService.getAdvs());
    }

    /**
     * 更新广告信息
     */
    @PutMapping
    public ResponseVO<String> updateAdvertisement(@RequestBody AdvertisementsVO advertisementsVO) {
        return ResponseVO.buildSuccess(advertisementsService.updateAdv(advertisementsVO));
    }

    /**
     *  创建广告
     */
    @PostMapping
    public ResponseVO<AdvertisementsVO> createAdvertisement(@RequestBody AdvertisementsVO advertisementsVO) {
        return ResponseVO.buildSuccess(advertisementsService.createAdv(advertisementsVO));
    }

    /**
     * 删除广告
     */
    @DeleteMapping("/{id}")
    public ResponseVO<String> deleteAdvertisement(@PathVariable(value = "id") Integer id) {
        return ResponseVO.buildSuccess(advertisementsService.deleteAdv(id));
    }
}