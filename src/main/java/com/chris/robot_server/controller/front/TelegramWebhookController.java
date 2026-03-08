package com.chris.robot_server.controller.front;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chris.robot_server.handle.UpdateDispatcher;
import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.model.Update;

/**
 * 处理Telegram Webhook请求: 接收 + 解析后交给 Dispatcher 分发，不写业务
 */
@RestController
@RequestMapping("/telegram")
public class TelegramWebhookController {
    private final UpdateDispatcher dispatcher;

    public TelegramWebhookController(UpdateDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @PostMapping("/webhook")
    public void onUpdate(@RequestBody String body) {
        System.out.println("RAW BODY = " + body); // ✅ 真正的 Telegram JSON

        Update update = BotUtils.parseUpdate(body);

        // System.out.println("HAS message = " + (update.message() != null));
        // System.out.println("HAS myChatMember = " + (update.myChatMember() != null));

        dispatcher.dispatch(update);
    }

    @PostMapping("/webTest")
    public void onUpdateTest(@RequestBody String body) {
        System.out.println("RAW BODY = " + body); // ✅ 真正的 Telegram JSON

        Update update = BotUtils.parseUpdate(body);

        // System.out.println("HAS message = " + (update.message() != null));
        // System.out.println("HAS myChatMember = " + (update.myChatMember() != null));

        dispatcher.dispatch(update);
    }
}
