package com.campus.trading.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trading.common.PageResult;
import com.campus.trading.dto.ItemPublishRequest;
import com.campus.trading.dto.ItemQueryRequest;
import com.campus.trading.entity.ItemImage;
import com.campus.trading.entity.SecondHandItem;
import com.campus.trading.mapper.ItemImageMapper;
import com.campus.trading.mapper.SecondHandItemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 物品服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private final SecondHandItemMapper itemMapper;
    private final ItemImageMapper imageMapper;

    /**
     * 分页查询物品列表
     */
    public PageResult<SecondHandItem> getItemList(ItemQueryRequest request) {
        Page<SecondHandItem> page = new Page<>(request.getCurrent(), request.getSize());

        IPage<SecondHandItem> result = itemMapper.selectPageWithConditions(
                page,
                request.getKeyword(),
                request.getCategoryId(),
                request.getStatus(),
                request.getSellerId()
        );

        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 根据ID获取物品详情
     */
    public SecondHandItem getItemDetail(Long id) {
        SecondHandItem item = itemMapper.selectById(id);
        if (item == null) {
            throw new RuntimeException("物品不存在");
        }

        // 增加浏览次数
        item.setViewCount(item.getViewCount() + 1);
        itemMapper.updateById(item);

        return item;
    }

    /**
     * 发布二手物品
     */
    @Transactional
    public Long publishItem(ItemPublishRequest request, Long userId) {
        // 创建物品记录
        SecondHandItem item = new SecondHandItem();
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setCategoryId(request.getCategoryId());
        item.setPrice(request.getPrice());
        item.setOriginalPrice(request.getOriginalPrice());
        item.setConditionLevel(request.getConditionLevel() != null ? request.getConditionLevel() : 1);
        item.setSellerId(userId);
        item.setContactInfo(request.getContactInfo());
        item.setStatus(0); // 待审核状态
        item.setViewCount(0);

        itemMapper.insert(item);

        // 保存图片
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            saveItemImages(item.getId(), request.getImages());
        }

        return item.getId();
    }

    /**
     * 更新物品
     */
    @Transactional
    public void updateItem(Long itemId, ItemPublishRequest request, Long userId) {
        SecondHandItem item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new RuntimeException("物品不存在");
        }

        // 验证权限
        if (!item.getSellerId().equals(userId)) {
            throw new RuntimeException("无权操作该物品");
        }

        // 更新物品信息
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setCategoryId(request.getCategoryId());
        item.setPrice(request.getPrice());
        item.setOriginalPrice(request.getOriginalPrice());
        item.setConditionLevel(request.getConditionLevel());
        item.setContactInfo(request.getContactInfo());
        item.setStatus(0); // 重新提交需要重新审核

        itemMapper.updateById(item);

        // 更新图片
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            imageMapper.deleteByItemId(itemId);
            saveItemImages(itemId, request.getImages());
        }
    }

    /**
     * 下架物品
     */
    public void offlineItem(Long itemId, Long userId) {
        SecondHandItem item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new RuntimeException("物品不存在");
        }

        if (!item.getSellerId().equals(userId)) {
            throw new RuntimeException("无权操作该物品");
        }

        item.setStatus(2); // 已下架
        itemMapper.updateById(item);
    }

    /**
     * 删除物品
     */
    @Transactional
    public void deleteItem(Long itemId, Long userId) {
        SecondHandItem item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new RuntimeException("物品不存在");
        }

        if (!item.getSellerId().equals(userId)) {
            throw new RuntimeException("无权操作该物品");
        }

        // 删除图片
        imageMapper.deleteByItemId(itemId);

        // 删除物品
        itemMapper.deleteById(itemId);
    }

    /**
     * 保存物品图片
     */
    private void saveItemImages(Long itemId, List<MultipartFile> images) {
        String uploadDir = "./uploads/items/";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        for (int i = 0; i < images.size(); i++) {
            MultipartFile file = images.get(i);
            if (file.isEmpty()) {
                continue;
            }

            try {
                // 生成唯一文件名
                String originalFilename = file.getOriginalFilename();
                String extension = originalFilename != null ?
                        originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
                String fileName = UUID.randomUUID() + extension;

                // 保存文件
                Path path = Paths.get(uploadDir + fileName);
                Files.write(path, file.getBytes());

                // 保存数据库记录
                ItemImage itemImage = new ItemImage();
                itemImage.setItemId(itemId);
                itemImage.setImageUrl("/uploads/items/" + fileName);
                itemImage.setIsCover(i == 0 ? 1 : 0); // 第一张作为封面
                itemImage.setSortOrder(i);

                imageMapper.insert(itemImage);

            } catch (IOException e) {
                log.error("图片上传失败", e);
                throw new RuntimeException("图片上传失败: " + e.getMessage());
            }
        }
    }
}
