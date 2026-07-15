package com.lab.commerce.mapper;

import com.lab.commerce.entity.OutboxEvent;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OutboxEventMapper extends BaseMapper<OutboxEvent> {

    List<OutboxEvent> selectByAggregateId(@Param("aggregateId") String aggregateId);

    List<OutboxEvent> selectDispatchCandidates(@Param("limit") int limit);

    int claim(@Param("id") Long id, @Param("token") String token, @Param("until") LocalDateTime until);

    int markPublished(@Param("id") Long id, @Param("token") String token);

    int releaseForRetry(@Param("id") Long id, @Param("token") String token);
}
