package com.chris.robot_server.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import com.chris.robot_server.dao.LotteryHistoryKlMapper;
import com.chris.robot_server.dao.LotteryHistoryLaoMapper;
import com.chris.robot_server.dao.LotteryHistoryMapper;
import com.chris.robot_server.dao.LotteryHistoryXgMapper;
import com.chris.robot_server.dao.TelegramLinkMapper;
import com.chris.robot_server.model.LotteryHistory;
import com.chris.robot_server.model.LotteryHistoryKl;
import com.chris.robot_server.model.LotteryHistoryLao;
import com.chris.robot_server.model.LotteryHistoryXg;
import com.chris.robot_server.model.TelegramGroup;
import com.chris.robot_server.model.TelegramLink;
import com.chris.robot_server.service.ImageGeneratorService;
import com.chris.robot_server.service.PushService;
import com.chris.robot_server.util.LotteryMessageBuilder;
import com.chris.robot_server.util.TelegramTextUtil;
import com.chris.robot_server.vo.LotteryHistoryVO;
import com.chris.robot_server.vo.LotteryRow;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.SendResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PushServiceImpl implements PushService {

    private final LotteryHistoryMapper lotteryHistoryMapper;
    private final LotteryHistoryXgMapper lotteryHistoryXgMapper;
    private final LotteryHistoryKlMapper lotteryHistoryKlMapper;
    private final LotteryHistoryLaoMapper lotteryHistoryLaoMapper;
    private final ImageGeneratorService imageGeneratorService;
    private final TelegramLinkMapper telegramLinkMapper;


    private final Map<String, TelegramBot> botMap; // token → TelegramBot

    private final Map<Long, Integer> lastMessageMap = new ConcurrentHashMap<>();


    /**
     * 推送开奖结果到群/频道
     */
    @Override
    public void pushToGroups(LotteryHistoryVO vo,TelegramGroup group) {
        String text = LotteryMessageBuilder.build(vo);
        Integer msgId = lastMessageMap.get(group.getGroupId());
        String token = group.getToken();
        if (msgId == null) {
            TelegramBot bot = botMap.get(token);
            if (bot != null) {
                var resp = bot.execute(new SendMessage(group.getGroupId(), text));
                if (resp.isOk()) {
                    lastMessageMap.put(group.getGroupId(), resp.message().messageId());
                }
            }
        } else {
            TelegramBot bot = botMap.get(token);
            if (bot != null) {
                bot.execute(new EditMessageText(group.getGroupId(), msgId, text));
            }
            if(vo.getNumbers().size()>=7) {
                lastMessageMap.remove(group.getGroupId());
            }
        }
    }


    /**
     * 处理开奖历史图片（新澳六合彩）
     */
    @Override
    public void handleLotteryImage(TelegramBot bot, Long chatId) {
        List<LotteryHistory> list = lotteryHistoryMapper.selectLatestList(15);
        if (CollectionUtils.isEmpty(list)) {
            bot.execute(new SendMessage(chatId, "暂无数据"));
            return;
        }
        LotteryHistory first = list.get(0);
            List<LotteryRow> rows = list.stream().map(item -> new LotteryRow(item.getExpect(), item.getOpenCode()))
                    .collect(Collectors.toList());
            try {
                byte[] imageBytes = imageGeneratorService.generateLotteryImage(rows);
                // 3️⃣ 上传图片 → 获取 file_id
                String newFileId = sendAndGetFileId(bot,chatId, imageBytes, "新澳门六合彩");
                if (newFileId != null) {
                    // 4️⃣ 保存 file_id 到数据库
                    first.setFileId(newFileId);
                    lotteryHistoryMapper.updateByPrimaryKeySelective(first);
                }
            } catch (IOException e) {
                bot.execute(new SendMessage(chatId, "数据获取失败"));
                e.printStackTrace();
            }
    }


    /**
     * 处理开奖历史图片（快乐8六合彩）
     */
    @Override
    public void handleLotteryKlImage(TelegramBot bot, Long chatId) {
        List<LotteryHistoryKl> list = lotteryHistoryKlMapper.selectLatestList(15);
        if (CollectionUtils.isEmpty(list)) {
            bot.execute(new SendMessage(chatId, "暂无数据"));
            return;
        }
        LotteryHistoryKl first = list.get(0);
        String fileId = first.getFileId();
        if (fileId != null) {
            sendByFileId(bot,chatId, fileId, "快乐8六合彩");
            return;
        }else {
            List<LotteryRow> rows = list.stream().map(item -> new LotteryRow(item.getExpect(), item.getOpenCode()))
                    .collect(Collectors.toList());
            try {
                byte[] imageBytes = imageGeneratorService.generateLotteryImage(rows);
                // 3️⃣ 上传图片 → 获取 file_id
                String newFileId = sendAndGetFileId(bot,chatId, imageBytes, "快乐8六合彩");
                if (newFileId != null) {
                    // 4️⃣ 保存 file_id 到数据库
                    first.setFileId(newFileId);
                    lotteryHistoryKlMapper.updateByPrimaryKeySelective(first);
                }
            } catch (IOException e) {
                bot.execute(new SendMessage(chatId, "数据获取失败"));
                e.printStackTrace();
            }
        }
    }


    /**
     * 处理开奖历史图片（香港六合彩）
     */
    @Override
    public void handleLotteryXgImage(TelegramBot bot, Long chatId) {
        List<LotteryHistoryXg> list = lotteryHistoryXgMapper.selectLatestList(15);
        if (CollectionUtils.isEmpty(list)) {
            bot.execute(new SendMessage(chatId, "暂无数据"));
            return;
        }
        LotteryHistoryXg first = list.get(0);
        String fileId = first.getFileId();
        if (fileId != null) {
            sendByFileId(bot,chatId, fileId, "香港六合彩");
            return;
        } else {
            List<LotteryRow> rows = list.stream().map(item -> new LotteryRow(item.getExpect(), item.getOpenCode()))
                    .collect(Collectors.toList());
            try {
                byte[] imageBytes = imageGeneratorService.generateLotteryImage(rows);
                // 3️⃣ 上传图片 → 获取 file_id
                String newFileId = sendAndGetFileId(bot,chatId, imageBytes, "香港六合彩");
                if (newFileId != null) {
                    // 4️⃣ 保存 file_id 到数据库
                    first.setFileId(newFileId);
                    lotteryHistoryXgMapper.updateByPrimaryKeySelective(first);
                }
            } catch (IOException e) {
                bot.execute(new SendMessage(chatId, "数据获取失败"));
                e.printStackTrace();
            }
        }
    }


    /**
     * 处理开奖历史图片（老澳门六合彩）
     */
    @Override
    public void handleLotterylaoImage(TelegramBot bot, Long chatId) {
        List<LotteryHistoryLao> list = lotteryHistoryLaoMapper.selectLatestList(15);
        if (CollectionUtils.isEmpty(list)) {
            bot.execute(new SendMessage(chatId, "暂无数据"));
            return;
        }
        LotteryHistoryLao first = list.get(0);
        String fileId = first.getFileId();
        if (fileId != null) {
            sendByFileId(bot,chatId, fileId, "澳门六合彩");
            return;
        } else {
            List<LotteryRow> rows = list.stream().map(item -> new LotteryRow(item.getExpect(), item.getOpenCode()))
                    .collect(Collectors.toList());
            try {
                byte[] imageBytes = imageGeneratorService.generateLotteryImage(rows);
                // 3️⃣ 上传图片 → 获取 file_id
                String newFileId = sendAndGetFileId(bot,chatId, imageBytes, "老澳门六合彩");
                if (newFileId != null) {
                    // 4️⃣ 保存 file_id 到数据库
                    first.setFileId(newFileId);
                    lotteryHistoryLaoMapper.updateByPrimaryKeySelective(first);
                }
            } catch (IOException e) {
                bot.execute(new SendMessage(chatId, "数据获取失败"));
                e.printStackTrace();
            }
        }
    }


    /**
     * 发送图片字节数组到指定聊天ID并获取文件ID
     *
     * @param chatId     聊天ID
     * @param imageBytes 图片字节数组
     * @return 图片文件ID（如果成功），否则为null
     */
    private String sendAndGetFileId(TelegramBot bot,Long chatId, byte[] imageBytes, String title) {

        String caption = getCaptionTelegramLinks("kj", title);

        SendPhoto req = new SendPhoto(chatId, imageBytes)
                .caption(caption)
                .parseMode(ParseMode.HTML);

        SendResponse res = bot.execute(req);

        if (!res.isOk()) {
            System.err.println("Send image failed: " + res.errorCode());
            return null;
        }

        Message msg = res.message();

        if (msg.photo() != null && msg.photo().length > 0) {
            return msg.photo()[msg.photo().length - 1].fileId(); // 最高质量图
        }

        return null;
    }

    /**
     * 发送图片文件ID到指定聊天ID
     *
     * @param chatId 聊天ID
     * @param fileId 图片文件ID
     */
    private void sendByFileId(TelegramBot bot,Long chatId, String fileId, String title) {

        String caption = getCaptionTelegramLinks("kj", title);

        SendPhoto req = new SendPhoto(chatId, fileId)
                .caption(caption)
                .parseMode(ParseMode.HTML);

        bot.execute(req);
    }

    private String getCaptionTelegramLinks(String type, String title) {
        List<TelegramLink> telegramLinks = telegramLinkMapper.selectByType(type);
        StringBuilder sb = new StringBuilder();
        sb.append("🎯 <b> ").append(TelegramTextUtil.escape(title)).append(" 开奖记录</b>\n\n");
        for (TelegramLink link : telegramLinks) {
            sb.append("📢 <b>")
                    .append(TelegramTextUtil.escape(link.getTitle()))
                    .append("</b>\n");

            sb.append("🌐 <a href=\"")
                    .append(TelegramTextUtil.escape(link.getAddress()))
                    .append("\">")
                    .append(TelegramTextUtil.escape(link.getAddress()))
                    .append("</a>\n\n");
        }
        return sb.toString();
    }

}
