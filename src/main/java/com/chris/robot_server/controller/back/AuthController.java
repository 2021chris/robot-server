package com.chris.robot_server.controller.back;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chris.robot_server.dao.ManagerMapper;
import com.chris.robot_server.model.Manager;
import com.chris.robot_server.util.ErrorCode;
import com.chris.robot_server.util.JwtUtil;
import com.chris.robot_server.util.ResultUtil;
import com.chris.robot_server.vo.ResultVO;

@RestController
@RequestMapping("/back/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private ManagerMapper managerMapper;
    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    @PostMapping("/login")
    public ResultVO<Object> login(@RequestParam(value = "username", defaultValue = "") String username, @RequestParam(value = "password", defaultValue = "") String password) {
        Manager manager = managerMapper.selectByUsername(username);
        if (manager == null) {
            return ResultUtil.error(ErrorCode.No_Manager, "用户不存在");
        }
        // Add password validation logic here
        if (!manager.getPassword().equals(password)) {
            return ResultUtil.error(ErrorCode.Error_Password, "密码不对");
        }
        String token = jwtUtil.generateToken(manager.getId());
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("managerInfo", manager);
        return ResultUtil.success(map);
    }
    
}
