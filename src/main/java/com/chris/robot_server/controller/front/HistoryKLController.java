package com.chris.robot_server.controller.front;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
import com.chris.robot_server.dao.LotteryHistoryKlMapper;
import com.chris.robot_server.model.LotteryHistoryKl;
import com.chris.robot_server.util.DateUtil;
import com.chris.robot_server.util.ResultUtil;
import com.chris.robot_server.vo.ResultVO;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

/**
 * 快乐8六合彩历史数据查询接口
 */
@RestController
@RequestMapping("/front/historyKl")
public class HistoryKLController {

    @Autowired
    private LotteryHistoryKlMapper lotteryHistoryKlMapper;

    /**
     * 获取最新N条记录
     * @param limit
     * @return
     */
    @GetMapping("/getHistoryKl")
    public ResultVO<Object> getLatestHistoryKl(@RequestParam(value = "limit", defaultValue = "10") int limit) {
        List<LotteryHistoryKl> list = lotteryHistoryKlMapper.selectLatestList(limit);
        return ResultUtil.success(list);
    }


    /**
     * 采集某一年
     * @param year
     * @return
     */
    @GetMapping("/gethistoryKlByYear/{year}")
    @Transactional
    public ResultVO<Object> gethistoryKlByYear(@PathVariable("year") String year){
        try {
            String url = "https://160kjb.com/api/lottery/history/happysix?date=" + year;
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
            List<LotteryHistoryKl> historyList = new ArrayList<>();

            for (int i = 0; i < dataArray.size(); i++) {
                JSONObject item = dataArray.getJSONObject(i);
                LotteryHistoryKl history = new LotteryHistoryKl();

                history.setExpect(item.getString("issue"));

                // 1. 处理 num 数组 -> "01,05,..." 格式
                JSONArray numArray = item.getJSONArray("num");
                if (numArray != null) {
                    String openCode = numArray.stream()
                            .map(Object::toString)
                            .map(s -> s.length() == 1 ? "0" + s : s) // 补0
                            .collect(Collectors.joining(","));
                    history.setOpenCode(openCode);
                }

                // 2. 处理 zodiac 数组 -> "马,蛇..." 格式 (顺便处理，防止存成JSON串)
                JSONArray zodiacArray = item.getJSONArray("zodiac");
                if (zodiacArray != null) {
                     String zodiacStr = zodiacArray.stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(","));
                     history.setZodiac(zodiacStr);
                }

                // 3. 处理 kjTime 时间戳 -> "yyyy-MM-dd HH:mm:ss"
                Long kjTime = item.getLong("kjTime");
                if (kjTime != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    history.setOpenTime(sdf.format(new Date(kjTime * 1000L)));
                }

                history.setType(url);

                Date newTime = DateUtil.ZonedBeijingNowDateTime();
                history.setCreateTime(newTime);
                history.setUpdateTime(newTime);

                historyList.add(history);
            }

            // Sort by expect ascending (small to large)
            historyList.sort(Comparator.comparing(LotteryHistoryKl::getExpect));

            // Save to database
            int count = 0;
            for (LotteryHistoryKl history : historyList) {
                LotteryHistoryKl existing= lotteryHistoryKlMapper.selectByExpect(history.getExpect());
                if (existing == null) {
                    lotteryHistoryKlMapper.insertSelective(history);
                    count++;
                }
            }
            return ResultUtil.success("Successfully synced " + count + " records.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.error("Error occurred: " + e.getMessage());
        }
    }
}