package com.chris.robot_server.controller.back;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chris.robot_server.model.TelegramBots;
import com.chris.robot_server.service.BotManagementService;
import com.chris.robot_server.util.ErrorCode;
import com.chris.robot_server.util.ResultUtil;
import com.chris.robot_server.vo.ResultVO;

import lombok.RequiredArgsConstructor;

/**
 * 后端机器人相关接口
 */
@RestController
@RequestMapping("/back/bots")
@RequiredArgsConstructor
public class BackBotController {

    private final BotManagementService service;

    /**
     * 添加新的机器人
     * @param req
     * @return
     */
    @PostMapping("/add")
    public ResultVO<Object> add(@RequestParam(value = "token", defaultValue = "") String token,@RequestParam(value = "description", defaultValue = "") String description) {
        TelegramBots bot = service.addBot(token, description);
        if (bot == null) {
            return ResultUtil.error(ErrorCode.Token_Invalid, "Token 无效或已存在");
        }
        return ResultUtil.success(bot);
    }

    @GetMapping("/list")
    public ResultVO<Object> list() {
        return ResultUtil.success(service.listAll());
    }

    /**
     * 禁用机器人
     * @param id
     */
    @GetMapping("/disable/{id}")
    public ResultVO<Object> disable(@PathVariable Long id) {
        TelegramBots bot = service.disableBot(id);
        return ResultUtil.success(bot);
    }

    /**
     * 启用机器人
     * @param id
     */
    @GetMapping("/enable/{id}")
    public ResultVO<Object> enable(@PathVariable Long id) {
        TelegramBots bot = service.enableBot(id);
        return ResultUtil.success(bot);
    }

    /**
     * 重置 Webhook（切换环境时调用）
     * @param token
     */
    @GetMapping("/reset")
    public ResultVO<Object> reset(String token) {
        service.resetWebhook(token);
        return ResultUtil.success();
    }


    // 机器人关键词列表
    // 机器人关键词添加
    // 机器人关键词编辑
}
