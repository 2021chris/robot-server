package com.chris.robot_server.Worker;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.chris.robot_server.component.HttpClient;
import com.chris.robot_server.dao.LotteryHistoryKlMapper;
import com.chris.robot_server.model.LotteryHistoryKl;
import com.chris.robot_server.model.TelegramGroup;
import com.chris.robot_server.service.PushService;
import com.chris.robot_server.util.DateUtil;
import com.chris.robot_server.util.TelegramTextUtil;
import com.chris.robot_server.vo.LotteryHistoryVO;
import com.chris.robot_server.dao.TelegramGroupMapper;
import com.chris.robot_server.enums.OpenStatusEnum;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

@Component
public class Kl8Worker extends BaseLotteryWorker<LotteryHistoryKl> {

    @Autowired
    LotteryHistoryKlMapper mapper;
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
            LotteryHistoryKl last = mapper.selectLatest();

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
                    // 今日已开奖
                    return;
                }
            }

            body = http.get("https://160kjb.com/api/lottery/info/happysix");

            if (body == null)
                return;

            JSONObject jsonObject = JSON.parseObject(body);

            if (jsonObject == null) {
                return;
            }

            // 2. 拿到 data
            JSONObject data = jsonObject.getJSONObject("data");
            if (data == null) {
                return;
            }
            // 3. 拿到 happysix
            JSONObject happysix = data.getJSONObject("happysix");
            if (happysix == null) {
                return;
            }
            // 4. 拿到 current（你要的就是这个）
            JSONObject current = happysix.getJSONObject("current");
            if (current == null) {
                return;
            }

            // 5. 取出 current 里各个字段
            String expect = current.getString("periodNumber");
            String openTime = current.getString("awardTime");
            String numStr = current.getString("awardNumbers");
            String openCode = "";

            if (!numStr.isEmpty()) {
                openCode = Arrays.stream(numStr.split(","))
                        .map(String::trim)
                        .map(s -> s.length() == 1 ? "0" + s : s)
                        .collect(Collectors.joining(","));
            }

            // 3. 若开奖号相同，则不推送（恢复你原始需求）
            if (last != null && last.getOpenCode().equals(openCode))
                return;

            LotteryHistoryKl record = new LotteryHistoryKl();
            record.setExpect(expect);
            record.setOpenCode(openCode);
            record.setOpenTime(openTime);
            record.setType("https://160kjb.com/api/lottery/info/happysix");

            Date newTime = DateUtil.ZonedBeijingNowDateTime();
            // 期号不同
            if (last == null || !last.getExpect().equals(expect)) {
                record.setCreateTime(newTime);
                mapper.insert(record);
            } else {
                last.setOpenCode(openCode);
                last.setUpdateTime(newTime);
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

    private void notifyGroups(LotteryHistoryKl r) {
        List<TelegramGroup> groups = groupMapper.findByStatus(OpenStatusEnum.KL8.getCode());
        int batchSize = 10;
        LotteryHistoryVO vo = new LotteryHistoryVO();
        vo.setExpect(r.getExpect());
        vo.setOpenTime(r.getOpenTime());
        vo.setNumbers(TelegramTextUtil.convertStringToIntArray(r.getOpenCode()));
        vo.setLotteryType(OpenStatusEnum.KL8.getCode());
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
