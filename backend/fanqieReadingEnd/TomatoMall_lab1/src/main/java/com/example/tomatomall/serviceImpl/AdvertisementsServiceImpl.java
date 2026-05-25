package com.example.tomatomall.serviceImpl;

import com.example.tomatomall.exception.TomatomallException;
import com.example.tomatomall.po.Advertisements;
import com.example.tomatomall.repository.AdvertisementsRepository;
import com.example.tomatomall.service.AdvertisementsService;
import com.example.tomatomall.vo.AdvertisementsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdvertisementsServiceImpl implements AdvertisementsService {
    @Autowired
    private AdvertisementsRepository advertisementsRepository;

    @Override
    public List<AdvertisementsVO> getAdvs() {
        List<Advertisements> advs = advertisementsRepository.findAll();
        return advs.stream()
                .map(this::convertToAdvertisementsVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String updateAdv(AdvertisementsVO advertisementsVO) {
        Optional<Advertisements> advertisementsOptional = advertisementsRepository.findById(advertisementsVO.getId());
        if (advertisementsOptional.isPresent()) {
            Advertisements advertisements = advertisementsOptional.get();
            advertisements.setTitle(advertisementsVO.getTitle());
            advertisements.setContent(advertisementsVO.getContent());
            advertisements.setImgUrl(advertisementsVO.getImgUrl());
            advertisements.setProductId(advertisementsVO.getProductId());

            advertisementsRepository.save(advertisements);
            return "更新成功";
        }else {
            throw TomatomallException.productsNotExits();
        }
    }
    @Override
    @Transactional
    public AdvertisementsVO createAdv(AdvertisementsVO advertisementsVO) {
        try {
            Advertisements advertisements = advertisementsVO.toPO();
            advertisementsRepository.save(advertisements);
            return advertisements.toVO();
        } catch (Exception e) {
            throw TomatomallException.productsNotExits();
        }
    }
    @Override
    @Transactional
    public String deleteAdv(Integer id) {
        try {
            advertisementsRepository.deleteById(id);
            return "删除成功";
        } catch (Exception e) {
            return null;
        }
    }
    private AdvertisementsVO convertToAdvertisementsVO(Advertisements advertisements) {
        AdvertisementsVO advertisementsVO = advertisements.toVO();
        advertisementsVO.setId(advertisements.getId());
        return advertisementsVO;
    }
}
