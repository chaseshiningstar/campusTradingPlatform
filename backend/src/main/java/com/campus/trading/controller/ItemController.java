package com.campus.trading.controller;

import com.campus.trading.common.PageResult;
import com.campus.trading.common.Result;
import com.campus.trading.dto.ItemPublishRequest;
import com.campus.trading.dto.ItemQueryRequest;
import com.campus.trading.dto.TagGenerationRequest;
import com.campus.trading.entity.SecondHandItem;
import com.campus.trading.interceptor.JwtInterceptor;
import com.campus.trading.service.ItemService;
import com.campus.trading.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 物品控制器
 */
@Tag(name = "物品管理", description = "二手物品发布、查询等接口")
@RestController
@RequestMapping("/item")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final TagService tagService;

    @Operation(summary = "获取物品列表(分页)")
    @GetMapping("/list")
    public Result<PageResult<SecondHandItem>> getItemList(ItemQueryRequest request) {
        try {
            if (request.getStatus() == null) {
                request.setStatus(1);
            }
            PageResult<SecondHandItem> result = itemService.getItemList(request);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取物品详情")
    @GetMapping("/detail/{id}")
    public Result<SecondHandItem> getItemDetail(@PathVariable Long id) {
        try {
            SecondHandItem item = itemService.getItemDetail(id);
            return Result.success(item);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "发布物品")
    @PostMapping("/publish")
    public Result<Long> publishItem(@Valid ItemPublishRequest request, HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute(JwtInterceptor.USER_ID_KEY);
            Long itemId = itemService.publishItem(request, userId);
            return Result.success(itemId);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "更新物品")
    @PutMapping("/update/{id}")
    public Result<Void> updateItem(@PathVariable Long id,
                                   @Valid ItemPublishRequest request,
                                   HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute(JwtInterceptor.USER_ID_KEY);
            itemService.updateItem(id, request, userId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "下架物品")
    @PutMapping("/offline/{id}")
    public Result<Void> offlineItem(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute(JwtInterceptor.USER_ID_KEY);
            itemService.offlineItem(id, userId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "删除物品")
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteItem(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute(JwtInterceptor.USER_ID_KEY);
            itemService.deleteItem(id, userId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取我的发布")
    @GetMapping("/my-items")
    public Result<PageResult<SecondHandItem>> getMyItems(ItemQueryRequest request, HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute(JwtInterceptor.USER_ID_KEY);
            request.setSellerId(userId);
            PageResult<SecondHandItem> result = itemService.getItemList(request);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "AI生成商品标签")
    @PostMapping("/generate-tags")
    public Result<List<String>> generateTags(@Valid @RequestBody TagGenerationRequest request) {
        try {
            List<String> tags = tagService.generateTags(request.getTitle(), request.getDescription());
            return Result.success(tags);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
