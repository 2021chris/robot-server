package com.chris.robot_server.Worker;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chris.robot_server.component.HttpClient;
import com.chris.robot_server.dao.LotteryHistoryLaoMapper;
import com.chris.robot_server.dao.TelegramGroupMapper;
import com.chris.robot_server.enums.OpenStatusEnum;
import com.chris.robot_server.model.LotteryHistory;
import com.chris.robot_server.model.LotteryHistoryLao;
import com.chris.robot_server.model.TelegramGroup;
import com.chris.robot_server.service.PushService;
import com.chris.robot_server.util.DateUtil;
import com.chris.robot_server.util.ResultUtil;
import com.chris.robot_server.util.TelegramTextUtil;
import com.chris.robot_server.vo.LotteryHistoryVO;

@Component
public class LaoaoWorker extends BaseLotteryWorker<LotteryHistory>{

    @Autowired
    LotteryHistoryLaoMapper mapper;
    @Autowired
    TelegramGroupMapper groupMapper;
    @Autowired
    HttpClient http;
    @Autowired
    PushService pushService;

    @Override
    protected void fetchAndProcess() {
        String body;
        try {
            LotteryHistoryLao last = mapper.selectLatest();

            if (last != null) {
                // 数据库中记录的开奖时间是今天，并且有7个开奖号码
                String dbOpenTimeStr = last.getOpenTime();
                // 截取日期部分 yyyy-MM-dd
                String dbDate = dbOpenTimeStr.substring(0, 10);

                // 获取当前北京时间
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai")); // 设置为北京时区
                String todayBj = sdf.format(new Date());

                List<Integer> lastOpenCodeCount = TelegramTextUtil.convertStringToIntArray(last.getOpenCode());
                if (dbDate.equals(todayBj) && lastOpenCodeCount != null && lastOpenCodeCount.size() == 7) {
                    // 今日已开奖:最后一条记录=今日，并且开奖号码有7个
                    return;
                }
            }

            body = http.get("http://api.bjjfnet.com/data/opencode/2032");

            if (body == null)return;

            JSONObject jsonObject = JSON.parseObject(body);
            if (jsonObject == null || !jsonObject.containsKey("data")) {
                 return;
            }

            JSONArray dataArray = jsonObject.getJSONArray("data");
            JSONObject item = dataArray.getJSONObject(0);

            String expect = item.getString("issue");
            int expectNum = Integer.parseInt(expect);
            int lastExpectNum = last != null ? Integer.parseInt(last.getExpect()) : -1;
            if (expectNum <= lastExpectNum) {
                // 已经处理过了
                return;
            }
            LotteryHistoryLao history = new LotteryHistoryLao();

            history.setExpect(expect);
            history.setOpenCode(item.getString("openCode"));
            history.setOpenTime(item.getString("openTime"));
            history.setType("http://api.bjjfnet.com/data/opencode/2032");
            
            Date newTime = DateUtil.ZonedBeijingNowDateTime();
            history.setCreateTime(newTime);

            mapper.insert(history);
            
            // notifyGroups(history);
            
        } catch (IOException e) {
            sleepRandom(5000, 9000);
        }
    }

    private void sleepRandom(int min, int max) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(min, max + 1));
        } catch (InterruptedException ignored) {
        }
    }

    private void notifyGroups(LotteryHistoryLao r) {
        List<TelegramGroup> groups = groupMapper.findByStatus(OpenStatusEnum.LaoAo.getCode());
        int batchSize = 10;
        LotteryHistoryVO vo = new LotteryHistoryVO();
        vo.setExpect(r.getExpect());
        vo.setOpenTime(r.getOpenTime());
        vo.setLotteryType(OpenStatusEnum.LaoAo.getCode());
        vo.setNumbers(TelegramTextUtil.convertStringToIntArray(r.getOpenCode()));
        for (int i = 0; i < groups.size(); i += batchSize) {
            int end = Math.min(i + batchSize, groups.size());
            for (TelegramGroup g : groups.subList(i, end)) {
                pushService.pushToGroups(vo, g);
            }
            try {
                Thread.sleep(1000); // 每批次后等待1秒，确保不超过30/sec
            } catch (InterruptedException e) {
                // 处理中断
            }
        }

    }

}
