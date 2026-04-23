package com.chris.robot_server.handle;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import com.chris.robot_server.config.sysConfig;
import com.chris.robot_server.dao.LotteryHistoryKlMapper;
import com.chris.robot_server.dao.LotteryHistoryMapper;
import com.chris.robot_server.dao.LotteryHistoryXgMapper;
import com.chris.robot_server.dao.TelegramGroupMapper;
import com.chris.robot_server.dao.TelegramLinkMapper;
import com.chris.robot_server.enums.OpenStatusEnum;
import com.chris.robot_server.model.LotteryHistory;
import com.chris.robot_server.model.LotteryHistoryKl;
import com.chris.robot_server.model.LotteryHistoryXg;
import com.chris.robot_server.model.TelegramGroup;
import com.chris.robot_server.model.TelegramLink;
import com.chris.robot_server.service.ImageGeneratorService;
import com.chris.robot_server.util.TelegramTextUtil;
import com.chris.robot_server.vo.LotteryRow;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.SendResponse;

import lombok.RequiredArgsConstructor;

/**
 * 处理回调查询（如按钮点击）
 */
@Component
@RequiredArgsConstructor
public class CallbackHandler implements BaseHandler {
    private final LotteryHistoryMapper lotteryHistoryMapper;
    private final LotteryHistoryXgMapper lotteryHistoryXgMapper;
    private final LotteryHistoryKlMapper lotteryHistoryKlMapper;
    private final ImageGeneratorService imageGeneratorService;
    private final TelegramLinkMapper telegramLinkMapper;
    private final TelegramGroupMapper telegramGroupMapper;
    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    

    @Override
    public boolean supports(Update update) {
        return update.callbackQuery() != null;
    }

    @Override
    public void handle(TelegramBot bot, String token, Update update) {
        CallbackQuery query = update.callbackQuery();
        Long chatId = resolveChatId(query);
        if (chatId == null)
            return;

        String data = query.data();
        //==============开奖历史
        if ("xin-aomen".equals(data)) {
            handleLotteryImage(bot,chatId);
        } else if ("hongkong".equals(data)) {
            handleLotteryXgImage(bot,chatId);
        }else if("kl8".equals(data)) {
            handleLotteryKlImage(bot,chatId);
        }else if ("tiaoma".equals(data)) {
            String caption = getCaptionTelegramLinks("tm");
            bot.execute(new SendMessage(chatId, caption).parseMode(ParseMode.HTML));
        }
        //============设置快乐8_香港_新澳门_澳门|新澳实时开奖
        else if("open-kl8".equals(data)) {

            Long userId = query.from().id();
            boolean isAdmin = TelegramTextUtil.isAdmin(bot, chatId, userId);
            if (!isAdmin) {
                bot.execute(new SendMessage(chatId, "只有管理员可以设置"));
                return;
            }

            TelegramGroup telegramGroup = telegramGroupMapper.findByGroupIdAndToken(chatId, token);
            boolean kl8Enabled = isEnabledFront(telegramGroup.getSettings(), 0);
            String updatedSettings = updateSettingsFront(telegramGroup.getSettings(), 0, !kl8Enabled);
            telegramGroup.setSettings(updatedSettings);
            telegramGroupMapper.updateByPrimaryKeySelective(telegramGroup);
            sendDaoHangMenu(updatedSettings, bot, chatId);
            // handleGroupStatus(bot,chatId, OpenStatusEnum.KL8.getCode(), token);
        }
        else if("open-hongkong".equals(data)) {
            Long userId = query.from().id();
            boolean isAdmin = TelegramTextUtil.isAdmin(bot, chatId, userId);
            if (!isAdmin) {
                bot.execute(new SendMessage(chatId, "只有管理员可以设置"));
                return;
            }
            TelegramGroup telegramGroup = telegramGroupMapper.findByGroupIdAndToken(chatId, token);
            boolean xgEnabled = isEnabledFront(telegramGroup.getSettings(), 1);
            String updatedSettings = updateSettingsFront(telegramGroup.getSettings(), 1, !xgEnabled);
            telegramGroup.setSettings(updatedSettings);
            telegramGroupMapper.updateByPrimaryKeySelective(telegramGroup);
            sendDaoHangMenu(updatedSettings, bot, chatId);
        }
        else if("open-xin-aomen".equals(data)) {
            Long userId = query.from().id();
            boolean isAdmin = TelegramTextUtil.isAdmin(bot, chatId, userId);
            if (!isAdmin) {
                bot.execute(new SendMessage(chatId, "只有管理员可以设置"));
                return;
            }
            TelegramGroup telegramGroup = telegramGroupMapper.findByGroupIdAndToken(chatId, token);
            boolean xinaomenEnabled = isEnabledFront(telegramGroup.getSettings(), 2);
            String updatedSettings = updateSettingsFront(telegramGroup.getSettings(), 2, !xinaomenEnabled);
            telegramGroup.setSettings(updatedSettings);
            telegramGroupMapper.updateByPrimaryKeySelective(telegramGroup);
            sendDaoHangMenu(updatedSettings, bot, chatId);
        }
        else if("open-aomen".equals(data)) {
            Long userId = query.from().id();
            boolean isAdmin = TelegramTextUtil.isAdmin(bot, chatId, userId);
            if (!isAdmin) {
                bot.execute(new SendMessage(chatId, "只有管理员可以设置"));
                return;
            }
            TelegramGroup telegramGroup = telegramGroupMapper.findByGroupIdAndToken(chatId, token);
            boolean aomenEnabled = isEnabledFront(telegramGroup.getSettings(), 3);
            String updatedSettings = updateSettingsFront(telegramGroup.getSettings(), 3, !aomenEnabled);
            telegramGroup.setSettings(updatedSettings);
            telegramGroupMapper.updateByPrimaryKeySelective(telegramGroup);
            sendDaoHangMenu(updatedSettings, bot, chatId);
        }
        else if("open-shishi-xinaomen".equals(data)) {
            Long userId = query.from().id();
            boolean isAdmin = TelegramTextUtil.isAdmin(bot, chatId, userId);
            if (!isAdmin) {
                bot.execute(new SendMessage(chatId, "只有管理员可以设置"));
                return;
            }
            TelegramGroup telegramGroup = telegramGroupMapper.findByGroupIdAndToken(chatId, token);
            boolean shishiXinaomenEnabled = isEnabledBack(telegramGroup.getSettings());
            String updatedSettings = updateSettingBack(telegramGroup.getSettings(), !shishiXinaomenEnabled);
            telegramGroup.setSettings(updatedSettings);
            telegramGroupMapper.updateByPrimaryKeySelective(telegramGroup);
            sendDaoHangMenu(updatedSettings, bot, chatId);
        }
    }

    private void sendDaoHangMenu(String settings, TelegramBot bot, Long chatId) {
        if(settings == null) {
            settings = "1_0_0_0|0";// 快乐8_香港_新澳门_澳门|新澳实时开奖
        }
        String[] parts = settings.split("\\|");
        String[] frontParts = parts[0].split("_");
        int shishiXinaomen = Integer.parseInt(parts[1]);
        int kl8 = Integer.parseInt(frontParts[0]);
        int hongkong = Integer.parseInt(frontParts[1]);
        int xinAomen = Integer.parseInt(frontParts[2]);
        int aomen = Integer.parseInt(frontParts[3]);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(
                new InlineKeyboardButton[] {
                        new InlineKeyboardButton(kl8 == 1 ? "✅快乐8六合彩" : "❌快乐8六合彩").callbackData("open-kl8"),
                        new InlineKeyboardButton(hongkong == 1 ? "✅香港六合彩" : "❌香港六合彩").callbackData("open-hongkong"),
                },
                new InlineKeyboardButton[] {
                        new InlineKeyboardButton(xinAomen == 1 ? "✅新澳门六合彩" : "❌新澳门六合彩").callbackData("open-xin-aomen"),
                        new InlineKeyboardButton(aomen == 1 ? "✅澳门六合彩" : "❌澳门六合彩").callbackData("open-aomen"),
                },
                new InlineKeyboardButton[] {
                        new InlineKeyboardButton(shishiXinaomen == 1 ? "✅新澳六合彩实时开奖" : "❌新澳六合彩实时开奖").callbackData("open-shishi-xinaomen"),
                });
        String messIdStr = redisTemplate.opsForValue().get(sysConfig.BOT_GROUP_DAHHANG_KEY + chatId);
        if(messIdStr != null) {
            bot.execute(new EditMessageText(chatId,Integer.parseInt(messIdStr), "选择开奖订阅功能：").replyMarkup(keyboard));
        }
        
    }

    private String updateSettingsFront(String settings, int index, boolean enable) {
        String[] parts = settings.split("\\|");
        String[] frontParts = parts[0].split("_");
        if (index < frontParts.length) {
            frontParts[index] = enable ? "1" : "0";
            parts[0] = String.join("_", frontParts);
            settings = parts[0] + "|" + parts[1];
        }
        return settings;
    }

    private String updateSettingBack(String settings, boolean enable) {
        String[] parts = settings.split("\\|");
        if (parts.length == 2) {
            parts[1] = enable ? "1" : "0";
            settings = parts[0] + "|" + parts[1];
        }
        return settings;
    }

    private boolean isEnabledFront(String settings, int index) {
        String[] parts = settings.split("\\|");
        String[] frontParts = parts[0].split("_");
        if (index < frontParts.length) {
            return "1".equals(frontParts[index]);
        }
        return false;
    }

    private boolean isEnabledBack(String settings) {
        String[] parts = settings.split("\\|");
        if (parts.length == 2) {
            return "1".equals(parts[1]);
        }
        return false;
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

    private void handleLotteryImage(TelegramBot bot,Long chatId) {
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
        // }
    }

    private void handleLotteryKlImage(TelegramBot bot, Long chatId) {
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

    private void handleLotteryXgImage(TelegramBot bot,Long chatId) {
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
     * @param token 令牌
     */
    private void handleGroupStatus(TelegramBot bot,Long chatId, Byte status, String token) {
        int res = telegramGroupMapper.updateStatusByGroupIdAndToken(chatId, token, status);
        if (res > 0) {
            bot.execute(new SendMessage(chatId, "已更换定时推送"));
        }
    }
}
