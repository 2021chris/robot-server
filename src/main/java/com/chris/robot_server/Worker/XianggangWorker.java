package com.chris.robot_server.Worker;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.chris.robot_server.component.HttpClient;

import com.chris.robot_server.dao.LotteryHistoryXgMapper;
import com.chris.robot_server.model.LotteryHistoryXg;
import com.chris.robot_server.model.TelegramGroup;
import com.chris.robot_server.service.PushService;
import com.chris.robot_server.util.DateUtil;
import com.chris.robot_server.util.TelegramTextUtil;
import com.chris.robot_server.vo.LotteryHistoryVO;
import com.chris.robot_server.dao.TelegramGroupMapper;
import com.chris.robot_server.enums.OpenStatusEnum;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

@Component
public class XianggangWorker extends BaseLotteryWorker<LotteryHistoryXg> {

    @Autowired
    LotteryHistoryXgMapper mapper;
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
            LotteryHistoryXg last = mapper.selectLatest();

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

            body = http.get("https://macaumarksix.com/api/hkjc.com");

            if (body == null)
                return;

            JSONArray jsonArray = JSON.parseArray(body);

            if (jsonArray == null || jsonArray.isEmpty()) {
                return;
            }

            // 接口返回数组，取第一条
            JSONObject item = jsonArray.getJSONObject(0);

            String expect = item.getString("expect");
            String openCode = item.getString("openCode");
            String openTime = item.getString("openTime");
            String zodiac = item.getString("zodiac");
            String wave = item.getString("wave");

            // 3. 若开奖号相同，则不推送（恢复你原始需求）
            if (last != null && last.getOpenCode().equals(openCode))
                return;

            LotteryHistoryXg record = new LotteryHistoryXg();
            record.setExpect(expect);
            record.setOpenCode(openCode);
            record.setOpenTime(openTime);
            record.setZodiac(zodiac);
            record.setWave(wave);
            record.setType("https://macaumarksix.com/api/hkjc.com");

            Date newTime = DateUtil.ZonedBeijingNowDateTime();
            // 期号不同
            if (last == null || !last.getExpect().equals(expect)) {
                record.setCreateTime(newTime);
                mapper.insert(record);
            } else {
                // 期号相同但开奖号码不同才能更新
                if(openCode.equals(last.getOpenCode())) return;
                last.setOpenCode(openCode);
                last.setUpdateTime(newTime);
                last.setZodiac(zodiac);
                last.setWave(wave);
                mapper.updateByPrimaryKeySelective(last);
            }
            notifyGroups(record);

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

    private void notifyGroups(LotteryHistoryXg r) {
        List<TelegramGroup> groups = groupMapper.findByStatus(OpenStatusEnum.Hongkong.getCode());
        int batchSize = 10;
        LotteryHistoryVO vo = new LotteryHistoryVO();
        vo.setExpect(r.getExpect());
        vo.setOpenTime(r.getOpenTime());
        vo.setNumbers(TelegramTextUtil.convertStringToIntArray(r.getOpenCode()));
        vo.setLotteryType(OpenStatusEnum.Hongkong.getCode());
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
