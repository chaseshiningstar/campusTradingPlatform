package com.campus.trading.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.trading.entity.ItemImage;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 物品图片Mapper
 */
@Mapper
public interface ItemImageMapper extends BaseMapper<ItemImage> {

    /**
     * 根据物品ID查询图片列表
     */
    @Select("SELECT * FROM item_image WHERE item_id = #{itemId} ORDER BY sort_order ASC, is_cover DESC")
    List<ItemImage> selectByItemId(Long itemId);

    /**
     * 删除物品的所有图片
     */
    @Delete("DELETE FROM item_image WHERE item_id = #{itemId}")
    int deleteByItemId(Long itemId);
}
