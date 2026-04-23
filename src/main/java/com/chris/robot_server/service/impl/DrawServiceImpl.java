package com.chris.robot_server.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chris.robot_server.dao.LotteryHistoryKlMapper;
import com.chris.robot_server.dao.LotteryHistoryLaoMapper;
import com.chris.robot_server.dao.LotteryHistoryMapper;
import com.chris.robot_server.dao.LotteryHistoryXgMapper;
import com.chris.robot_server.dao.LotteryPushExpectMapper;
import com.chris.robot_server.dao.TelegramGroupMapper;
import com.chris.robot_server.enums.OpenStatusEnum;
import com.chris.robot_server.model.LotteryHistory;
import com.chris.robot_server.model.LotteryHistoryKl;
import com.chris.robot_server.model.LotteryHistoryLao;
import com.chris.robot_server.model.LotteryHistoryXg;
import com.chris.robot_server.model.LotteryPushExpect;
import com.chris.robot_server.model.TelegramGroup;
import com.chris.robot_server.service.DrawService;
import com.chris.robot_server.util.LotteryMessageBuilder;
import com.chris.robot_server.util.TelegramTextUtil;
import com.chris.robot_server.vo.LotteryHistoryVO;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;

@Service
public class DrawServiceImpl implements DrawService {

    @Autowired
    private LotteryPushExpectMapper lotteryPushExpectMapper;
    @Autowired
    private LotteryHistoryLaoMapper lotteryHistoryLaoMapper;
    @Autowired
    private LotteryHistoryXgMapper lotteryHistoryXgMapper;
    @Autowired
    private LotteryHistoryKlMapper lotteryHistoryKlMapper;
    @Autowired
    private LotteryHistoryMapper lotteryHistoryMapper;
    @Autowired
    private TelegramGroupMapper groupMapper;
    @Autowired
    private Map<String, TelegramBot> botMap;

    private boolean isEnabledFront(String settings, int index) {
        if (settings == null) {
            return false;
        }
        String[] parts = settings.split("\\|");
        String[] frontParts = parts[0].split("_");
        if (index < 0 || index >= frontParts.length) {
            return false;
        }
        return "1".equals(frontParts[index]);
    }

    @Override
    @Transactional
    public void pushAllLotteryDraw() {
        List<LotteryPushExpect> allPushExpect = lotteryPushExpectMapper.selectAll();
        if (allPushExpect == null || allPushExpect.isEmpty()) {
            return;
        }

        List<TelegramGroup> groups = groupMapper.selectAllGroups();
        if (groups == null || groups.isEmpty()) {
            return;
        }

        for (LotteryPushExpect pushExpect : allPushExpect) {
            // 快乐8
            if ("kl".equals(pushExpect.getType())) {
                LotteryHistoryKl last = lotteryHistoryKlMapper.selectLatest();
                if (!pushExpect.getExpect().equals(last.getExpect())) {
                    // 预期的期号和数据库中最新的期号不一致，说明需要推送
                    pushExpect.setExpect(last.getExpect());
                    lotteryPushExpectMapper.updateByPrimaryKeySelective(pushExpect);

                    // 推送到群
                    LotteryHistoryVO vo = new LotteryHistoryVO();
                    vo.setExpect(last.getExpect());
                    vo.setOpenTime(last.getOpenTime());
                    vo.setLotteryType(OpenStatusEnum.KL8.getCode());
                    vo.setNumbers(TelegramTextUtil.convertStringToIntArray(last.getOpenCode()));
                    String text = LotteryMessageBuilder.buildDraw(vo);

                    for (TelegramGroup group : groups) {
                        boolean kl8Enabled = isEnabledFront(group.getSettings(), 0);
                        if (!kl8Enabled) {
                            continue; // 该群未启用快乐8推送，跳过
                        }
                        TelegramBot bot = botMap.get(group.getToken());
                        bot.execute(new SendMessage(group.getGroupId(), text));
                        try {
                            Thread.sleep(1000); // 每批次后等待1秒，确保不超过30/sec
                        } catch (InterruptedException e) {
                            // 处理中断
                        }
                    }
                }
            }

            // 香港
            if ("xg".equals(pushExpect.getType())) {
                LotteryHistoryXg last = lotteryHistoryXgMapper.selectLatest();
                if (!pushExpect.getExpect().equals(last.getExpect())) {
                    // 预期的期号和数据库中最新的期号不一致，说明需要推送
                    pushExpect.setExpect(last.getExpect());
                    lotteryPushExpectMapper.updateByPrimaryKeySelective(pushExpect);

                    // 推送到群
                    LotteryHistoryVO vo = new LotteryHistoryVO();
                    vo.setExpect(last.getExpect());
                    vo.setOpenTime(last.getOpenTime());
                    vo.setLotteryType(OpenStatusEnum.Hongkong.getCode());
                    vo.setNumbers(TelegramTextUtil.convertStringToIntArray(last.getOpenCode()));
                    String text = LotteryMessageBuilder.buildDraw(vo);

                    for (TelegramGroup group : groups) {
                        boolean xgEnabled = isEnabledFront(group.getSettings(), 1);
                        if (!xgEnabled) {
                            continue; // 该群未启用香港推送，跳过
                        }
                        TelegramBot bot = botMap.get(group.getToken());
                        bot.execute(new SendMessage(group.getGroupId(), text));
                        try {
                            Thread.sleep(1000); // 每批次后等待1秒，确保不超过30/sec
                        } catch (InterruptedException e) {
                            // 处理中断
                        }
                    }
                }
            }

            // 新澳不发
            if ("xa".equals(pushExpect.getType())) {
                LotteryHistory last = lotteryHistoryMapper.selectLatest();
                if (!pushExpect.getExpect().equals(last.getExpect())) {
                    // 预期的期号和数据库中最新的期号不一致，说明需要推送
                    pushExpect.setExpect(last.getExpect());
                    lotteryPushExpectMapper.updateByPrimaryKeySelective(pushExpect);

                    // 推送到群
                    LotteryHistoryVO vo = new LotteryHistoryVO();
                    vo.setExpect(last.getExpect());
                    vo.setOpenTime(last.getOpenTime());
                    vo.setLotteryType(OpenStatusEnum.Xin_Aomen.getCode());
                    vo.setNumbers(TelegramTextUtil.convertStringToIntArray(last.getOpenCode()));
                    String text = LotteryMessageBuilder.buildDraw(vo);

                    for (TelegramGroup group : groups) {
                        boolean xaEnabled = isEnabledFront(group.getSettings(), 2);
                        if (!xaEnabled) {
                            continue; // 该群未启用新澳门推送，跳过
                        }
                        TelegramBot bot = botMap.get(group.getToken());
                        bot.execute(new SendMessage(group.getGroupId(), text));
                        try {
                            Thread.sleep(1000); // 每批次后等待1秒，确保不超过30/sec
                        } catch (InterruptedException e) {
                            // 处理中断
                        }
                    }
                }
            }
            // 老澳
            if ("lao".equals(pushExpect.getType())) {
                LotteryHistoryLao last = lotteryHistoryLaoMapper.selectLatest();
                if (!pushExpect.getExpect().equals(last.getExpect())) {
                    // 预期的期号和数据库中最新的期号不一致，说明需要推送
                    pushExpect.setExpect(last.getExpect());
                    lotteryPushExpectMapper.updateByPrimaryKeySelective(pushExpect);

                    // 推送到群
                    LotteryHistoryVO vo = new LotteryHistoryVO();
                    vo.setExpect(last.getExpect());
                    vo.setOpenTime(last.getOpenTime());
                    vo.setLotteryType(OpenStatusEnum.LaoAo.getCode());
                    vo.setNumbers(TelegramTextUtil.convertStringToIntArray(last.getOpenCode()));
                    String text = LotteryMessageBuilder.buildDraw(vo);

                    for (TelegramGroup group : groups) {
                        boolean laoEnabled = isEnabledFront(group.getSettings(), 3);
                        if (!laoEnabled) {
                            continue; // 该群未启用老澳推送，跳过
                        }
                        TelegramBot bot = botMap.get(group.getToken());
                        bot.execute(new SendMessage(group.getGroupId(), text));
                        try {
                            Thread.sleep(1000); // 每批次后等待1秒，确保不超过30/sec
                        } catch (InterruptedException e) {
                            // 处理中断
                        }
                    }
                }
            }
            
        }
    }

}
