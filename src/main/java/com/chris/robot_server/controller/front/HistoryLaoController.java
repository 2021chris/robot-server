package com.chris.robot_server.controller.front;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chris.robot_server.dao.LotteryHistoryLaoMapper;
import com.chris.robot_server.dao.LotteryHistoryMapper;
import com.chris.robot_server.model.LotteryHistory;
import com.chris.robot_server.model.LotteryHistoryLao;
import com.chris.robot_server.util.DateUtil;
import com.chris.robot_server.util.ResultUtil;
import com.chris.robot_server.util.TelegramTextUtil;
import com.chris.robot_server.vo.ResultVO;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.springframework.web.bind.annotation.RequestParam;

/**
 * 新澳门六合彩历史数据查询接口
 */
@RestController
@RequestMapping("/front/historyLao")
public class HistoryLaoController {

    @Autowired
    private LotteryHistoryLaoMapper lotteryHistoryLaoMapper;

    /**
     * 分页查询历史数据
     * @param pageNumber
     * @param pageSize
     * @return
     */
    // @GetMapping("/getHistoryAm")
    // public ResultVO<Object> getHistoryAm(
    //         @RequestParam(value = "pageNumber", defaultValue = "1") int pageNumber,
    //         @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        
    //     PageHelper.startPage(pageNumber, pageSize);
    //     List<LotteryHistory> list = lotteryHistoryMapper.selectallList();
    //     PageInfo<LotteryHistory> pageInfo = new PageInfo<>(list);
        
    //     return ResultUtil.success(pageInfo);
    // }

    /**
     * 获取最新N条记录
     * @param limit
     * @return
     */
    @GetMapping("/getHistoryLao")
    public ResultVO<Object> getHistoryLao(@RequestParam(value = "limit", defaultValue = "10") int limit) {
        List<LotteryHistoryLao> list = lotteryHistoryLaoMapper.selectLatestList(limit);
        return ResultUtil.success(list);
    }



    /**
     * 查询最新一期
     * @return
     */
    @GetMapping("/getLatestOpenCode")
    @Transactional
    public ResultVO<Object> getLatestOpenCode() {
        try {
            // 1. 获取数据库最新一条记录
            LotteryHistoryLao dbLatest = lotteryHistoryLaoMapper.selectLatest();
            if (dbLatest != null && dbLatest.getOpenTime() != null) {
                // 判断 openTime (字符串格式 "yyyy-MM-dd HH:mm:ss") 是否是今天(北京时间)
                // dbLatest.getOpenTime() 已经是北京时间字符串
                try {
                    String dbOpenTimeStr = dbLatest.getOpenTime();
                    // 截取日期部分 yyyy-MM-dd
                    String dbDate = dbOpenTimeStr.substring(0, 10); 
                    
                    // 获取当前北京时间
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai")); // 设置为北京时区
                    String todayBj = sdf.format(new Date());

                    if (dbDate.equals(todayBj)) {
                        return ResultUtil.success("今日已更新，最新数据：" + dbLatest.getExpect());
                    }
                } catch (Exception e) {
                    // 如果日期解析失败，忽略并继续请求API
                    e.printStackTrace();
                }
            }

            String url = "https://macaumarksix.com/api/macaujc.com";
            HttpResponse<String> response = Unirest.get(url).asString();

            if (response.getStatus() != 200) {
                return ResultUtil.error("Failed to fetch data from API");
            }

            String body = response.getBody();
            // 根据用户描述，API直接返回一个JSON数组
            JSONArray jsonArray = JSON.parseArray(body);

            if (jsonArray == null || jsonArray.isEmpty()) {
                return ResultUtil.error("没有接收到数据");
            }

            // 接口只会返回一条最新数据，取第一条即可
            JSONObject item = jsonArray.getJSONObject(0);
            
            String expect = item.getString("expect");
            String openCode = item.getString("openCode");

            // 检查号码是否包含7个数字
            if (openCode == null || openCode.split(",").length != 7) {
                return ResultUtil.error("数据不完整或正在开奖中");
            }

            // 检查是否已存在
            LotteryHistoryLao existing = lotteryHistoryLaoMapper.selectByExpect(expect);
            if (existing != null) {
                return ResultUtil.success("该期数据已存在，无需更新");
            }

            LotteryHistoryLao history = new LotteryHistoryLao();
            // 映射字段：expect, openCode, zodiac, wave, openTime
            history.setExpect(expect);
            history.setOpenCode(openCode);
            history.setZodiac(item.getString("zodiac"));
            history.setWave(item.getString("wave"));
            history.setOpenTime(item.getString("openTime"));
            history.setType(url);

            // 设置默认/审计字段
            Date newTime = DateUtil.ZonedBeijingNowDateTime();
            history.setCreateTime(newTime);
            history.setUpdateTime(newTime);

            lotteryHistoryLaoMapper.insertSelective(history);

            return ResultUtil.success("成功同步最新一期数据：" + expect);

        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.error("Error occurred: " + e.getMessage());
        }
    }

    /**
     * 开奖接口
     * 一颗一颗开奖
     * @return
     */
    @GetMapping("/getLive")
    @Transactional
    public ResultVO<Object> getLive() {
        try {
            String url = "https://macaumarksix.com/api/live";
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
            LotteryHistoryLao history = new LotteryHistoryLao();
            history.setExpect(expect);
            history.setOpenCode(openCode);
            history.setZodiac(item.getString("zodiac"));
            history.setWave(item.getString("wave"));
            history.setOpenTime(item.getString("openTime"));
            history.setType(url);
            Date newTime = DateUtil.ZonedBeijingNowDateTime();
            history.setCreateTime(newTime);
            history.setUpdateTime(newTime);

            // 检查数据库中是否已有该期数据
            LotteryHistoryLao existing = lotteryHistoryLaoMapper.selectByExpect(expect);

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
                    lotteryHistoryLaoMapper.insertSelective(history);
                    return ResultUtil.success(history);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.error("查询出错: " + e.getMessage());
        }
    }


    /**
     * 查询某一期
     * @param expect
     * @return
     */
    @GetMapping("/getHistoryByExpect/{expect}")
    @Transactional
    public ResultVO<Object> getHistoryByExpect(@PathVariable("expect") String expect) {
        try {
            // 1. 先查询数据库
            LotteryHistoryLao existing = lotteryHistoryLaoMapper.selectByExpect(expect);
            if (existing != null) {
                return ResultUtil.success(existing);
            }

            // 2. 数据库没有，查询外部接口 
            String url = "https://history.macaumarksix.com/history/macaujc/expect/" + expect;
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
            
            LotteryHistoryLao history = new LotteryHistoryLao();
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
            lotteryHistoryLaoMapper.insertSelective(history);

            return ResultUtil.success(history);

        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.error("查询出错: " + e.getMessage());
        }
    }

    /**
     * 采集某一年
     * @param year
     * @return
     */
    @GetMapping("/gethistoryLao")
    @Transactional
    public ResultVO<Object> gethistoryLao() {
        try {
            String url = "http://api.bjjfnet.com/data/opencode/2032";
            HttpResponse<String> response = Unirest.get(url).asString();
            
            if (response.getStatus() != 200) {
                return ResultUtil.error("Failed to fetch data from API");
            }

            String body = response.getBody();
            JSONObject jsonObject = JSON.parseObject(body);
            
            if (jsonObject == null || !jsonObject.containsKey("data")) {
                 return ResultUtil.error("Invalid data format");
            }

            JSONArray dataArray = jsonObject.getJSONArray("data");

            JSONObject item = dataArray.getJSONObject(0);
            LotteryHistoryLao history = new LotteryHistoryLao();
            // Map only specified fields: expect, openCode, zodiac, wave, openTime
            history.setExpect(item.getString("issue"));
            history.setOpenCode(item.getString("openCode"));
            history.setOpenTime(item.getString("openTime"));
            history.setType(url);
            
            Date newTime = DateUtil.ZonedBeijingNowDateTime();
            history.setCreateTime(newTime);
            history.setUpdateTime(newTime);


            System.out.println("111111111Saving expect: " + history.getExpect() + ", openCode: " + history.getOpenCode());

           

            return ResultUtil.success("Successfully synced " + 1 + " records.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.error("Error occurred: " + e.getMessage());
        }
    }
}
