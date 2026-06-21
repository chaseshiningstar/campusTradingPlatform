package com.campus.trading.controller;

import com.campus.trading.common.Result;
import com.campus.trading.entity.ItemCategory;
import com.campus.trading.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分类控制器
 */
@Tag(name = "分类管理", description = "物品分类接口")
@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "获取所有分类")
    @GetMapping("/list")
    public Result<List<ItemCategory>> getAllCategories() {
        try {
            List<ItemCategory> categories = categoryService.getAllCategories();
            return Result.success(categories);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取子分类")
    @GetMapping("/children")
    public Result<List<ItemCategory>> getChildren(@RequestParam Long parentId) {
        try {
            List<ItemCategory> categories = categoryService.getCategoriesByParentId(parentId);
            return Result.success(categories);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
