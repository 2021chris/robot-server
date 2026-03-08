package com.chris.robot_server.controller.front;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chris.robot_server.dao.LotteryHistoryXgMapper;
import com.chris.robot_server.model.LotteryHistoryXg;
import com.chris.robot_server.util.DateUtil;
import com.chris.robot_server.util.ResultUtil;
import com.chris.robot_server.util.TelegramTextUtil;
import com.chris.robot_server.vo.ResultVO;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

/**
 * 香港六合彩历史数据查询接口
 */
@RestController
@RequestMapping("/front/historyXg")
public class HistoryXGController {

    @Autowired
    private LotteryHistoryXgMapper lotteryHistoryXgMapper;

    /**
     * 分页查询历史数据
     * 
     * @param pageNumber
     * @param pageSize
     * @return
     */
    // @GetMapping("/getHistoryXg")
    // public ResultVO<Object> getHistoryXg(
    //         @RequestParam(value = "pageNumber", defaultValue = "1") int pageNumber,
    //         @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

    //     PageHelper.startPage(pageNumber, pageSize);
    //     List<LotteryHistoryXg> list = lotteryHistoryXgMapper.selectallList();
    //     PageInfo<LotteryHistoryXg> pageInfo = new PageInfo<>(list);

    //     return ResultUtil.success(pageInfo);
    // }

    /**
     * 获取最新N条记录
     * 
     * @param limit
     * @return
     */
    @GetMapping("/getHistoryXg")
    public ResultVO<Object> getLatestHistoryXg(@RequestParam(value = "limit", defaultValue = "10") int limit) {
        List<LotteryHistoryXg> list = lotteryHistoryXgMapper.selectLatestList(limit);
        return ResultUtil.success(list);
    }

    /**
     * 开奖接口
     * 一颗一颗开奖
     * 
     * @return
     */
    @GetMapping("/getLive")
    @Transactional
    public ResultVO<Object> getLive() {
        try {
            String url = "https://macaumarksix.com/api/hkjc.com";
            HttpResponse<String> response = Unirest.get(url).asString();

            if (response.getStatus() != 200) {
                return ResultUtil.error("接口请求失败");
            }

            String body = response.getBody();
            JSONArray jsonArray = JSON.parseArray(body);

            if (jsonArray == null || jsonArray.isEmpty()) {
                return ResultUtil.error("未获取到开奖数据");
            }

            // 接口返回数组，取第一条
            JSONObject item = jsonArray.getJSONObject(0);

            String expect = item.getString("expect");
            String openCode = item.getString("openCode");

            // 构造对象
            LotteryHistoryXg history = new LotteryHistoryXg();
            history.setExpect(expect);
            history.setOpenCode(openCode);
            history.setZodiac(item.getString("zodiac"));
            history.setWave(item.getString("wave"));
            history.setOpenTime(item.getString("openTime"));
            history.setType("https://macaumarksix.com/api/hkjc.com");
            Date newTime = DateUtil.ZonedBeijingNowDateTime();
            history.setCreateTime(newTime);
            history.setUpdateTime(newTime);

            // 检查数据库中是否已有该期数据
            LotteryHistoryXg existing = lotteryHistoryXgMapper.selectByExpect(expect);


            int codeCount = TelegramTextUtil.countResult(openCode);

            if (existing != null) {
                // 数据库中有数据，就表示已经开完奖并且是完整的7位号码
                return ResultUtil.success(existing);
            } else {
                // 数据库没有
                if (codeCount < 7) {
                    // 场景2: 数据库没有 并且 openCode < 7位 -> 表示在开奖中。只返回对象，不入库
                    return ResultUtil.success(history);
                } else {
                    // 场景3: 数据库没有 并且 openCode = 7位 -> 返回给前端并写入数据库
                    lotteryHistoryXgMapper.insertSelective(history);
                    return ResultUtil.success(history);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.error("查询出错: " + e.getMessage());
        }
    }

    /**
     * 采集某一年
     * 
     * @param year
     * @return
     */
    @GetMapping("/gethistoryXgByYear/{year}")
    @Transactional
    public ResultVO<Object> gethistoryXgByYear(@PathVariable("year") String year) {
        try {
            String url = "https://api.macaumarksix.com/history/hkjc/y/" + year;
            HttpResponse<String> response = Unirest.get(url).asString();

            if (response.getStatus() != 200) {
                url = "https://open.macaumarksix.com/history/hkjc/y/" + year;
                response = Unirest.get(url).asString();

                if (response.getStatus() != 200) {
                    return ResultUtil.error("Failed to fetch data from API");
                }
            }

            String body = response.getBody();
            JSONObject jsonObject = JSON.parseObject(body);

            if (jsonObject == null || !jsonObject.containsKey("data")) {
                return ResultUtil.error("Invalid data format");
            }

            JSONArray dataArray = jsonObject.getJSONArray("data");
            List<LotteryHistoryXg> historyList = new ArrayList<>();

            for (int i = 0; i < dataArray.size(); i++) {
                JSONObject item = dataArray.getJSONObject(i);
                LotteryHistoryXg history = new LotteryHistoryXg();
                // Map only specified fields: expect, openCode, zodiac, wave, openTime
                history.setExpect(item.getString("expect"));
                history.setOpenCode(item.getString("openCode"));
                history.setZodiac(item.getString("zodiac"));
                history.setWave(item.getString("wave"));
                history.setOpenTime(item.getString("openTime"));
                history.setType(url);

                Date newTime = DateUtil.ZonedBeijingNowDateTime();
                history.setCreateTime(newTime);
                history.setUpdateTime(newTime);

                historyList.add(history);
            }

            // Sort by expect ascending (small to large)
            historyList.sort(Comparator.comparing(LotteryHistoryXg::getExpect));

            // Save to database
            int count = 0;
            for (LotteryHistoryXg history : historyList) {
                // Check if exists to avoid duplicates
                LotteryHistoryXg existing = lotteryHistoryXgMapper.selectByExpect(history.getExpect());
                if (existing == null) {
                    lotteryHistoryXgMapper.insertSelective(history);
                    count++;
                }
            }

            return ResultUtil.success("Successfully synced " + count + " records.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.error("Error occurred: " + e.getMessage());
        }
    }


     /**
     * 查询某一期
     * @param expect
     * @return
     */
    @GetMapping("/getHistoryXgByExpect/{expect}")
    @Transactional
    public ResultVO<Object> getHistoryXgByExpect(@PathVariable("expect") String expect) {
        try {
            // 1. 先查询数据库
            LotteryHistoryXg existing = lotteryHistoryXgMapper.selectByExpect(expect);
            if (existing != null) {
                return ResultUtil.success(existing);
            }

            // 2. 数据库没有，查询外部接口
            String url = "https://api.macaumarksix.com/history/hkjc/expect/" + expect;
            HttpResponse<String> response = Unirest.get(url).asString();

            if (response.getStatus() != 200) {
                return ResultUtil.error("接口请求失败");
            }

            String body = response.getBody();
            JSONObject jsonObject = JSON.parseObject(body);

            if (jsonObject == null || !jsonObject.containsKey("data") || jsonObject.get("data") == null) {
                return ResultUtil.error("未查询到该期数据");
            }
            JSONArray dataArray = jsonObject.getJSONArray("data");
            if (dataArray.isEmpty()) {
                return ResultUtil.error("未查询到该期数据");
            }

            JSONObject item = dataArray.getJSONObject(0);

            LotteryHistoryXg history = new LotteryHistoryXg();
            history.setExpect(item.getString("expect"));
            history.setOpenCode(item.getString("openCode"));
            history.setZodiac(item.getString("zodiac"));
            history.setWave(item.getString("wave"));
            history.setOpenTime(item.getString("openTime"));
            history.setType(url);

            Date newTime = DateUtil.ZonedBeijingNowDateTime();
            history.setCreateTime(newTime);
            history.setUpdateTime(newTime);

            // 3. 保存到数据库
            lotteryHistoryXgMapper.insertSelective(history);
            return ResultUtil.success(history);

        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.error("查询出错: " + e.getMessage());
        }
    }
}
