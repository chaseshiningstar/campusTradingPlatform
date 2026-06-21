package com.campus.trading.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.trading.entity.ItemCategory;
import com.campus.trading.mapper.ItemCategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类服务
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ItemCategoryMapper categoryMapper;

    /**
     * 获取所有启用的分类
     */
    public List<ItemCategory> getAllCategories() {
        LambdaQueryWrapper<ItemCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ItemCategory::getStatus, 1)
                .orderByAsc(ItemCategory::getSortOrder);
        return categoryMapper.selectList(wrapper);
    }

    /**
     * 根据父ID获取子分类
     */
    public List<ItemCategory> getCategoriesByParentId(Long parentId) {
        LambdaQueryWrapper<ItemCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ItemCategory::getParentId, parentId)
                .eq(ItemCategory::getStatus, 1)
                .orderByAsc(ItemCategory::getSortOrder);
        return categoryMapper.selectList(wrapper);
    }
}
