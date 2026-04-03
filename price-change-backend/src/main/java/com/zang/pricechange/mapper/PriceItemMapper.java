// Mapper 所在包
package com.zang.pricechange.mapper;

// MyBatis-Plus 基础 Mapper 接口：提供通用的增删改查方法
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
// 价格项实体类
import com.zang.pricechange.entity.PriceItem;
// MyBatis 注解：标记为 Mapper 接口
import org.apache.ibatis.annotations.Mapper;

/**
 * 价格项数据访问层（Mapper）
 * 
 * Mapper 层负责与数据库交互，继承 BaseMapper 后自动拥有以下方法：
 * - insert(entity)：插入一条记录
 * - deleteById(id)：根据 ID 删除
 * - delete(wrapper)：根据条件删除
 * - selectById(id)：根据 ID 查询
 * - selectList(wrapper)：根据条件查询列表
 * - selectOne(wrapper)：根据条件查询单条
 * - updateById(entity)：根据 ID 更新
 * 
 * 不需要写 SQL，MyBatis-Plus 会自动生成
 */
@Mapper  // 告诉 MyBatis 这是一个 Mapper 接口，会自动生成实现类
public interface PriceItemMapper extends BaseMapper<PriceItem> {
    // 继承 BaseMapper 后，不需要写任何代码就有增删改查功能
}
