package com.example.emall.service;

import com.example.emall.vo.SeckillActivityVO;
import com.example.emall.vo.SeckillResultVO;

import java.util.List;

public interface SeckillService {

    List<SeckillActivityVO> getActivities();

    SeckillActivityVO createActivity(SeckillActivityVO vo);

    SeckillActivityVO updateActivity(Integer id, SeckillActivityVO vo);

    void deleteActivity(Integer id);

    SeckillResultVO doSeckill(Integer activityId);

    void preloadStock(Integer activityId);
}
