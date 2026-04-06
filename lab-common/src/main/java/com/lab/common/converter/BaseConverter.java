package com.lab.common.converter;

import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * MapStruct 基础转换器接口
 *
 * <p>业务模块中的 Converter 只需继承此接口并加 @Mapper 注解即可获得全套转换能力：
 * <pre>
 * {@code
 * @Mapper(componentModel = "spring")
 * public interface UserConverter extends BaseConverter<UserDO, UserVO> {
 *     // 如需字段映射差异，在此添加 @Mapping
 * }
 * }
 * </pre>
 *
 * @param <S> Source 源对象类型（通常是 DO/Entity）
 * @param <T> Target 目标对象类型（通常是 VO/DTO）
 */
public interface BaseConverter<S, T> {

    /**
     * 单个对象转换：Source → Target
     */
    T toTarget(S source);

    /**
     * 列表转换：List<Source> → List<Target>
     */
    List<T> toTargetList(List<S> sourceList);

    /**
     * 反向转换：Target → Source
     */
    S toSource(T target);

    /**
     * 反向列表转换：List<Target> → List<Source>
     */
    List<S> toSourceList(List<T> targetList);

    /**
     * 更新已有 Target 对象（忽略 null 字段，用于 PATCH 场景）
     * 示例：将 UserUpdateDTO 的非 null 字段更新到已查出的 UserDO
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateTargetIgnoreNull(S source, @MappingTarget T target);
}
