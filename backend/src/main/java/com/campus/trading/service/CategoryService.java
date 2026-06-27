package com.campus.trading.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.campus.trading.entity.ItemCategory;
import com.campus.trading.entity.SecondHandItem;
import com.campus.trading.mapper.ItemCategoryMapper;
import com.campus.trading.mapper.SecondHandItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ItemCategoryMapper categoryMapper;
    private final SecondHandItemMapper itemMapper;

    /**
     * "其他"分类名称常量
     */
    private static final String OTHER_CATEGORY_NAME = "其他";

    public List<ItemCategory> getAllCategories() {
        LambdaQueryWrapper<ItemCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ItemCategory::getStatus, 1)
                .orderByAsc(ItemCategory::getSortOrder);
        return categoryMapper.selectList(wrapper);
    }

    public List<ItemCategory> getAllForAdmin() {
        LambdaQueryWrapper<ItemCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(ItemCategory::getSortOrder);
        return categoryMapper.selectList(wrapper);
    }

    public List<Map<String, Object>> getCategoryTree() {
        List<ItemCategory> all = getAllForAdmin();
        Map<Long, List<ItemCategory>> childrenMap = all.stream()
                .filter(c -> c.getParentId() != null && c.getParentId() > 0)
                .collect(Collectors.groupingBy(ItemCategory::getParentId));

        List<Map<String, Object>> tree = new ArrayList<>();
        for (ItemCategory parent : all) {
            if (parent.getParentId() == null || parent.getParentId() == 0) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", parent.getId());
                node.put("categoryName", parent.getCategoryName());
                node.put("parentId", parent.getParentId());
                node.put("sortOrder", parent.getSortOrder());
                node.put("icon", parent.getIcon());
                node.put("status", parent.getStatus());
                node.put("createTime", parent.getCreateTime());

                List<Map<String, Object>> childNodes = new ArrayList<>();
                List<ItemCategory> children = childrenMap.getOrDefault(parent.getId(), Collections.emptyList());
                for (ItemCategory child : children) {
                    Map<String, Object> childNode = new HashMap<>();
                    childNode.put("id", child.getId());
                    childNode.put("categoryName", child.getCategoryName());
                    childNode.put("parentId", child.getParentId());
                    childNode.put("sortOrder", child.getSortOrder());
                    childNode.put("icon", child.getIcon());
                    childNode.put("status", child.getStatus());
                    childNode.put("createTime", child.getCreateTime());
                    childNodes.add(childNode);
                }
                node.put("children", childNodes);
                tree.add(node);
            }
        }
        return tree;
    }

    public List<ItemCategory> getCategoriesByParentId(Long parentId) {
        LambdaQueryWrapper<ItemCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ItemCategory::getParentId, parentId)
                .eq(ItemCategory::getStatus, 1)
                .orderByAsc(ItemCategory::getSortOrder);
        return categoryMapper.selectList(wrapper);
    }

    @Transactional
    public ItemCategory addCategory(ItemCategory category) {
        LambdaQueryWrapper<ItemCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ItemCategory::getCategoryName, category.getCategoryName())
                .eq(ItemCategory::getParentId, category.getParentId() != null ? category.getParentId() : 0L);
        if (categoryMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("该分类名称已存在");
        }

        if (category.getSortOrder() == null) {
            LambdaQueryWrapper<ItemCategory> maxWrapper = new LambdaQueryWrapper<>();
            maxWrapper.eq(ItemCategory::getParentId, category.getParentId() != null ? category.getParentId() : 0L)
                    .orderByDesc(ItemCategory::getSortOrder)
                    .last("LIMIT 1");
            ItemCategory last = categoryMapper.selectOne(maxWrapper);
            category.setSortOrder(last != null ? last.getSortOrder() + 1 : 1);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        categoryMapper.insert(category);
        return category;
    }

    @Transactional
    public void updateCategory(ItemCategory category) {
        ItemCategory existing = categoryMapper.selectById(category.getId());
        if (existing == null) {
            throw new RuntimeException("分类不存在");
        }
        if (category.getCategoryName() != null && !category.getCategoryName().equals(existing.getCategoryName())) {
            LambdaQueryWrapper<ItemCategory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ItemCategory::getCategoryName, category.getCategoryName())
                    .eq(ItemCategory::getParentId, existing.getParentId())
                    .ne(ItemCategory::getId, category.getId());
            if (categoryMapper.selectCount(wrapper) > 0) {
                throw new RuntimeException("该分类名称已存在");
            }
            // 分类名称变更时,将其下所有商品迁移到"其他"分类
            reassignItemsToOtherCategory(category.getId());
        }
        categoryMapper.updateById(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        ItemCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }
        LambdaQueryWrapper<ItemCategory> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(ItemCategory::getParentId, id);
        if (categoryMapper.selectCount(childWrapper) > 0) {
            throw new RuntimeException("该分类下存在子分类,请先删除子分类");
        }
        // 删除前,将其下所有商品迁移到"其他"分类
        reassignItemsToOtherCategory(id);
        categoryMapper.deleteById(id);
    }

    @Transactional
    public void toggleStatus(Long id) {
        ItemCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }
        category.setStatus(category.getStatus() == 1 ? 0 : 1);
        categoryMapper.updateById(category);
    }

    /**
     * 将指定分类下的所有商品迁移到"其他"分类
     * 若"其他"分类不存在,则不迁移(仅清空原分类关联,商品category_id置为"其他"id)
     *
     * @param categoryId 原分类ID
     */
    private void reassignItemsToOtherCategory(Long categoryId) {
        // 查找"其他"分类(必须是顶层分类)
        LambdaQueryWrapper<ItemCategory> otherWrapper = new LambdaQueryWrapper<>();
        otherWrapper.eq(ItemCategory::getCategoryName, OTHER_CATEGORY_NAME)
                .eq(ItemCategory::getParentId, 0L);
        ItemCategory otherCategory = categoryMapper.selectOne(otherWrapper);
        if (otherCategory == null) {
            // "其他"分类不存在,无法迁移
            return;
        }
        // 不能把"其他"分类下的商品迁移到自身
        if (otherCategory.getId().equals(categoryId)) {
            return;
        }
        // 批量更新该分类下所有商品的category_id为"其他"分类ID
        LambdaUpdateWrapper<SecondHandItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SecondHandItem::getCategoryId, categoryId)
                .set(SecondHandItem::getCategoryId, otherCategory.getId());
        itemMapper.update(null, updateWrapper);
    }
}
