package com.chris.robot_server.handle;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import com.chris.robot_server.dao.LotteryHistoryKlMapper;
import com.chris.robot_server.dao.LotteryHistoryMapper;
import com.chris.robot_server.dao.LotteryHistoryXgMapper;
import com.chris.robot_server.dao.TelegramGroupMapper;
import com.chris.robot_server.dao.TelegramLinkMapper;
import com.chris.robot_server.enums.OpenStatusEnum;
import com.chris.robot_server.model.LotteryHistory;
import com.chris.robot_server.model.LotteryHistoryKl;
import com.chris.robot_server.model.LotteryHistoryXg;
import com.chris.robot_server.model.TelegramLink;
import com.chris.robot_server.service.ImageGeneratorService;
import com.chris.robot_server.vo.LotteryRow;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.SendResponse;

/**
 * 处理回调查询（如按钮点击）
 */
@Component
public class CallbackHandler implements BaseHandler {
    private final TelegramBot bot;
    private final LotteryHistoryMapper lotteryHistoryMapper;
    private final LotteryHistoryXgMapper lotteryHistoryXgMapper;
    private final LotteryHistoryKlMapper lotteryHistoryKlMapper;
    private final ImageGeneratorService imageGeneratorService;
    private final TelegramLinkMapper telegramLinkMapper;
    private final TelegramGroupMapper telegramGroupMapper;

    public CallbackHandler(TelegramBot bot, LotteryHistoryMapper lotteryHistoryMapper,
            LotteryHistoryXgMapper lotteryHistoryXgMapper, LotteryHistoryKlMapper lotteryHistoryKlMapper,
            ImageGeneratorService imageGeneratorService, TelegramLinkMapper telegramLinkMapper,
            TelegramGroupMapper telegramGroupMapper) {
        this.bot = bot;
        this.lotteryHistoryMapper = lotteryHistoryMapper;
        this.lotteryHistoryXgMapper = lotteryHistoryXgMapper;
        this.lotteryHistoryKlMapper = lotteryHistoryKlMapper;
        this.imageGeneratorService = imageGeneratorService;
        this.telegramLinkMapper = telegramLinkMapper;
        this.telegramGroupMapper = telegramGroupMapper;
    }

    @Override
    public boolean supports(Update update) {
        return update.callbackQuery() != null;
    }

    @Override
    public void handle(Update update) {
        CallbackQuery query = update.callbackQuery();
        Long chatId = resolveChatId(query);
        if (chatId == null)
            return;

        String data = query.data();
        if ("xin-aomen".equals(data)) {
            // bot.execute(new SendMessage(chatId, "当前价格：100 USDT"));
            handleLotteryImage(chatId);
        } else if ("hongkong".equals(data)) {
            handleLotteryXgImage(chatId);
        }else if("kl8".equals(data)) {
            handleLotteryKlImage(chatId);
        }else if ("tiaoma".equals(data)) {
            String caption = getCaptionTelegramLinks("tm");
            bot.execute(new SendMessage(chatId, caption).parseMode(ParseMode.HTML));
        }else if("open-xin-aomen".equals(data)) {
            handleGroupStatus(chatId, OpenStatusEnum.Xin_Aomen.getCode());
        }else if("open-hongkong".equals(data)) {
            handleGroupStatus(chatId, OpenStatusEnum.Hongkong.getCode());
        }else if("open-kl8".equals(data)) {
            handleGroupStatus(chatId, OpenStatusEnum.KL8.getCode());
        }
        else if("cpzctzw".equals(data)) {
            bot.execute(new SendMessage(chatId, "民间担保已收押金 3000USDT🙊\r\n" + //
                                "万利下载地址1：168wl.app\r\n" + //
                                "万利下载地址2：https://wanli.hzlqsg.com/\r\n" + //
                                "畅玩网页版1: wanli68.com \r\n" + //
                                "畅玩网页版2: wanli98.cc\r\n" + //
                                "认准唯一房间号 彩满天 【661146】").parseMode(ParseMode.HTML));
        }else if("tggfxzwz".equals(data)) {
            String caption = getCaptionTelegramLinks("tggfxzwz");
            bot.execute(new SendMessage(chatId, caption).parseMode(ParseMode.HTML));
        }else if("dkhyzdh".equals(data)) {
            String caption = getCaptionTelegramLinks("dkhyzdh");
            bot.execute(new SendMessage(chatId, caption).parseMode(ParseMode.HTML));
        }
        else if("aqsyzwb".equals(data)) {
            String caption = getCaptionTelegramLinks("aqsyzwb");
            bot.execute(new SendMessage(chatId, caption).parseMode(ParseMode.HTML));
        }else if("mjucdy".equals(data)) {
            String caption = getCaptionTelegramLinks("mjucdy");
            bot.execute(new SendMessage(chatId, caption).parseMode(ParseMode.HTML));
        }else if("jsscpqq".equals(data)) {
            String caption = getCaptionTelegramLinks("jsscpqq");
            bot.execute(new SendMessage(chatId, caption).parseMode(ParseMode.HTML));
        }
    }

    // 唯一允许 deprecated API 的地方（隔离）
    private Long resolveChatId(CallbackQuery query) {
        try {
            if (query.message() != null && query.message().chat() != null) {
                return query.message().chat().id();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    public int priority() {
        return 5;
    }

    private void handleLotteryImage(Long chatId) {
        List<LotteryHistory> list = lotteryHistoryMapper.selectLatestList(15);
        if (CollectionUtils.isEmpty(list)) {
            bot.execute(new SendMessage(chatId, "暂无数据"));
            return;
        }
        LotteryHistory first = list.get(0);
        // String fileId = first.getFileId();
        // if (fileId != null) {
        //     sendByFileId(chatId, fileId, "新澳门六合彩");
        //     return;
        // } else {
            List<LotteryRow> rows = list.stream().map(item -> new LotteryRow(item.getExpect(), item.getOpenCode()))
                    .collect(Collectors.toList());
            try {
                byte[] imageBytes = imageGeneratorService.generateLotteryImage(rows);
                // 3️⃣ 上传图片 → 获取 file_id
                String newFileId = sendAndGetFileId(chatId, imageBytes, "新澳门六合彩");
                if (newFileId != null) {
                    // 4️⃣ 保存 file_id 到数据库
                    first.setFileId(newFileId);
                    lotteryHistoryMapper.updateByPrimaryKeySelective(first);
                }
            } catch (IOException e) {
                bot.execute(new SendMessage(chatId, "数据获取失败"));
                e.printStackTrace();
            }
        // }
    }

    private void handleLotteryKlImage(Long chatId) {
        List<LotteryHistoryKl> list = lotteryHistoryKlMapper.selectLatestList(15);
        if (CollectionUtils.isEmpty(list)) {
            bot.execute(new SendMessage(chatId, "暂无数据"));
            return;
        }
        LotteryHistoryKl first = list.get(0);
        String fileId = first.getFileId();
        if (fileId != null) {
            sendByFileId(chatId, fileId, "快乐8六合彩");
            return;
        }else {
            List<LotteryRow> rows = list.stream().map(item -> new LotteryRow(item.getExpect(), item.getOpenCode()))
                    .collect(Collectors.toList());
            try {
                byte[] imageBytes = imageGeneratorService.generateLotteryImage(rows);
                // 3️⃣ 上传图片 → 获取 file_id
                String newFileId = sendAndGetFileId(chatId, imageBytes, "快乐8六合彩");
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

    private void handleLotteryXgImage(Long chatId) {
        List<LotteryHistoryXg> list = lotteryHistoryXgMapper.selectLatestList(15);
        if (CollectionUtils.isEmpty(list)) {
            bot.execute(new SendMessage(chatId, "暂无数据"));
            return;
        }
        LotteryHistoryXg first = list.get(0);
        String fileId = first.getFileId();
        if (fileId != null) {
            sendByFileId(chatId, fileId, "香港六合彩");
            return;
        } else {
            List<LotteryRow> rows = list.stream().map(item -> new LotteryRow(item.getExpect(), item.getOpenCode()))
                    .collect(Collectors.toList());
            try {
                byte[] imageBytes = imageGeneratorService.generateLotteryImage(rows);
                // 3️⃣ 上传图片 → 获取 file_id
                String newFileId = sendAndGetFileId(chatId, imageBytes, "香港六合彩");
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
     * 发送图片字节数组到指定聊天ID并获取文件ID
     *
     * @param chatId     聊天ID
     * @param imageBytes 图片字节数组
     * @return 图片文件ID（如果成功），否则为null
     */
    private String sendAndGetFileId(Long chatId, byte[] imageBytes, String title) {

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
    private void sendByFileId(Long chatId, String fileId, String title) {

        String caption = getCaptionTelegramLinks("kj", title);

        SendPhoto req = new SendPhoto(chatId, fileId)
                .caption(caption)
                .parseMode(ParseMode.HTML);

        bot.execute(req);
    }

    private String getCaptionTelegramLinks(String type, String title) {
        List<TelegramLink> telegramLinks = telegramLinkMapper.selectByType(type);
        StringBuilder sb = new StringBuilder();
        sb.append("🎯 <b> ").append(escape(title)).append(" 开奖记录</b>\n\n");
        for (TelegramLink link : telegramLinks) {
            sb.append("📢 <b>")
                    .append(escape(link.getTitle()))
                    .append("</b>\n");

            sb.append("🌐 <a href=\"")
                    .append(escape(link.getAddress()))
                    .append("\">")
                    .append(escape(link.getAddress()))
                    .append("</a>\n\n");
        }
        return sb.toString();
    }


    private String getKeydbConfig(String type) {
        List<TelegramLink> telegramLinks = telegramLinkMapper.selectByType(type);
        StringBuilder sb = new StringBuilder();
        for (TelegramLink link : telegramLinks) {
            sb.append(escape(link.getAddress()));
        }
        return sb.toString();
    }

    private String getCaptionTelegramLinks(String type) {
        List<TelegramLink> telegramLinks = telegramLinkMapper.selectByType(type);
        StringBuilder sb = new StringBuilder();
        // sb.append("🎯 <b> 挑码助手</b>\n\n");
        for (TelegramLink link : telegramLinks) {
            sb.append("📌 <b>")
                    .append(escape(link.getTitle()))
                    .append("</b>\n");

            sb.append("🌐 <a href=\"")
                    .append(escape(link.getAddress()))
                    .append("\">")
                    .append(escape(link.getAddress()))
                    .append("</a>\n\n");
        }
        return sb.toString();
    }

    private String escape(String text) {
        return HtmlUtils.htmlEscape(text);
    }


    /**
     * 变更每日推送彩种
     *
     * @param chatId 群ID
     * @param status 状态码
     */
    private void handleGroupStatus(Long chatId, Byte status) {
        int res = telegramGroupMapper.updateStatusByGroupId(chatId, status);
        if (res > 0) {
            bot.execute(new SendMessage(chatId, "已更换定时推送"));
        }
    }
}
