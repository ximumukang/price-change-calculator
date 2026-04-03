// Mapper 所在包
package com.zang.pricechange.mapper;

// MyBatis-Plus 基础 Mapper 接口
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
// 用户实体类
import com.zang.pricechange.entity.User;
// MyBatis 注解
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据访问层（Mapper）
 * 
 * 负责用户表的数据库操作，继承 BaseMapper 后自动拥有增删改查方法
 * 在 UserService 中通过 userMapper.selectOne()、userMapper.insert() 等方法操作数据库
 */
@Mapper  // 标记为 MyBatis Mapper 接口
public interface UserMapper extends BaseMapper<User> {
    // 不需要写任何代码，BaseMapper 已经提供了所有常用方法
}
