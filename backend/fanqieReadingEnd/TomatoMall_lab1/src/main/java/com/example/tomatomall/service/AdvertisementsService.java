package com.example.tomatomall.service;

import com.example.tomatomall.vo.AdvertisementsVO;

import java.util.List;

public interface AdvertisementsService {
    List<AdvertisementsVO> getAdvs();

    String updateAdv(AdvertisementsVO advertisementsVO);

    AdvertisementsVO createAdv(AdvertisementsVO advertisementsVO);

    String deleteAdv(Integer id);
}
