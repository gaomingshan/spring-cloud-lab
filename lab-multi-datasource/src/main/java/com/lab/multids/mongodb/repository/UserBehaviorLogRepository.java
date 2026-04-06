package com.lab.multids.mongodb.repository;

import com.lab.multids.mongodb.entity.UserBehaviorLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MongoDB Repository
 * 继承 MongoRepository 即可获得 CRUD、分页等基础能力
 */
@Repository
public interface UserBehaviorLogRepository extends MongoRepository<UserBehaviorLog, String> {

    /** 按 userId 查询行为日志 */
    List<UserBehaviorLog> findByUserId(Long userId);

    /** 按行为类型查询 */
    List<UserBehaviorLog> findByAction(String action);
}
