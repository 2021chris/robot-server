package com.chris.robot_server.controller.front;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chris.robot_server.model.TelegramBots;
import com.chris.robot_server.service.BotManagementService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bots")
@RequiredArgsConstructor
public class BotController {

    private final BotManagementService service;

    /**
     * 添加新的机器人
     * @param req
     * @return
     */
    @PostMapping("/add")
    public TelegramBots add(@RequestBody Map<String, String> req) {
        String token = req.get("token");
        String desc = req.getOrDefault("description", "");
        return service.addBot(token, desc);
    }

    @GetMapping("/list")
    public List<TelegramBots> list() {
        return service.listAll();
    }

    @DeleteMapping("/{id}")
    public void disable(@PathVariable Long id) {
        service.disableBot(id);
    }
}
