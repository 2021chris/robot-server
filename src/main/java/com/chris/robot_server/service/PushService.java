package com.chris.robot_server.service;

import com.chris.robot_server.model.TelegramGroup;
import com.chris.robot_server.vo.LotteryHistoryVO;

public interface PushService {

    public void send(long groupId, String text, String token);

    public void pushToGroups(LotteryHistoryVO vo,TelegramGroup group);

}
