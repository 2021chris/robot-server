package com.chris.robot_server.controller.front;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chris.robot_server.handle.UpdateDispatcher;
import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;

import lombok.RequiredArgsConstructor;

/**
 * 处理Telegram Webhook请求: 接收 + 解析后交给 Dispatcher 分发，不写业务
 */
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class TelegramWebhookController {

    private final UpdateDispatcher dispatcher;
    private final Map<String, TelegramBot> botMap; // token → TelegramBot


    @PostMapping("/{token}")
    public ResponseEntity<String> handleUpdate(
            @PathVariable String token,
            @RequestBody String rawJson) {

        Update update = BotUtils.parseUpdate(rawJson);


        TelegramBot bot = botMap.get(token);
        if (bot == null) {
            return ResponseEntity.notFound().build();
        }

        // 交给统一处理器（你的业务逻辑写在这里）
        dispatcher.handle(bot, token, update);

        return ResponseEntity.ok("ok"); // Telegram 要求返回 200
    }

}
