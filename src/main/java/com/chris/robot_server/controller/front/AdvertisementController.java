package com.chris.robot_server.controller.front;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chris.robot_server.dao.AdvertisementMapper;
import com.chris.robot_server.model.Advertisement;
import com.chris.robot_server.util.ResultUtil;
import com.chris.robot_server.vo.ResultVO;

@RestController
@RequestMapping("/front/advertisement")
public class AdvertisementController {

    @Autowired
    private AdvertisementMapper advertisementMapper;

    @GetMapping("/getAdvertBySeat")
    public ResultVO<Object> getAdvertBySeat(@RequestParam(value = "seat", defaultValue = "") String seat,
            @RequestParam(value = "type", defaultValue = "0") Byte type) {
        Advertisement record = new Advertisement();
        record.setSeat(seat);
        record.setType(type);
        Advertisement advertisement = advertisementMapper.selectBySeatAndType(record);
        if (advertisement == null) {
            return ResultUtil.error("广告不存在");
        }
        return ResultUtil.success(advertisement.getContent());
    }
}
