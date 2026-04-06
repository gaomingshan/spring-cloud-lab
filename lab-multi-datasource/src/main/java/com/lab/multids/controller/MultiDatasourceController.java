package com.lab.multids.controller;

import com.lab.common.result.Result;
import com.lab.multids.mongodb.entity.UserBehaviorLog;
import com.lab.multids.mongodb.repository.UserBehaviorLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 异构数据源演示 Controller
 *
 * 场景：用户注册时
 *   1. 基础信息 → MySQL（强一致性，支持事务回滚）
 *   2. 行为日志 → MongoDB（高写入，灵活扩展字段）
 */
@Slf4j
@RestController
@RequestMapping("/multi")
@RequiredArgsConstructor
public class MultiDatasourceController {

    private final UserBehaviorLogRepository behaviorLogRepository;

    /**
     * 写入用户行为日志到 MongoDB
     */
    @PostMapping("/log")
    public Result<UserBehaviorLog> writeLog(
            @RequestParam Long userId,
            @RequestParam String action,
            @RequestBody(required = false) Map<String, Object> extra) {
        UserBehaviorLog log = new UserBehaviorLog();
        log.setUserId(userId);
        log.setAction(action);
        log.setExtra(extra);
        log.setCreateTime(LocalDateTime.now());
        UserBehaviorLog saved = behaviorLogRepository.save(log);
        this.log.info("[MongoDB] 写入行为日志: id={}, userId={}, action={}",
                saved.getId(), userId, action);
        return Result.ok(saved);
    }

    /**
     * 从 MongoDB 查询用户行为日志
     */
    @GetMapping("/log/{userId}")
    public Result<List<UserBehaviorLog>> getLogs(@PathVariable Long userId) {
        List<UserBehaviorLog> logs = behaviorLogRepository.findByUserId(userId);
        this.log.info("[MongoDB] 查询行为日志: userId={}, count={}", userId, logs.size());
        return Result.ok(logs);
    }

    /**
     * 演示异构操作：同一请求写 MySQL + MongoDB
     * 注意：异构数据源不支持跨库事务，需应用层保证最终一致性
     */
    @PostMapping("/user-register")
    public Result<String> userRegister(
            @RequestParam Long userId,
            @RequestParam String username) {
        // 1. 写 MySQL（用户基础信息，支持事务）
        log.info("[MySQL] 写入用户基础信息: userId={}, username={}", userId, username);
        // userMapper.insert(new User(userId, username));

        // 2. 写 MongoDB（注册行为日志，高写入）
        UserBehaviorLog behaviorLog = new UserBehaviorLog();
        behaviorLog.setUserId(userId);
        behaviorLog.setAction("REGISTER");
        behaviorLog.setExtra(Map.of("username", username));
        behaviorLog.setCreateTime(LocalDateTime.now());
        behaviorLogRepository.save(behaviorLog);
        log.info("[MongoDB] 写入注册行为日志: userId={}", userId);

        return Result.ok("注册成功（MySQL + MongoDB 双写）");
    }
}
