package com.campus.trading.controller;

import com.campus.trading.common.Result;
import com.campus.trading.entity.ItemCategory;
import com.campus.trading.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "分类管理", description = "物品分类接口")
@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "获取所有分类")
    @GetMapping("/list")
    public Result<List<ItemCategory>> getAllCategories() {
        return Result.success(categoryService.getAllCategories());
    }

    @Operation(summary = "获取分类树(管理端)")
    @GetMapping("/tree")
    public Result<List<Map<String, Object>>> getCategoryTree() {
        return Result.success(categoryService.getCategoryTree());
    }

    @Operation(summary = "获取子分类")
    @GetMapping("/children")
    public Result<List<ItemCategory>> getChildren(@RequestParam Long parentId) {
        return Result.success(categoryService.getCategoriesByParentId(parentId));
    }

    @Operation(summary = "新增分类")
    @PostMapping("/add")
    public Result<ItemCategory> addCategory(@RequestBody ItemCategory category) {
        try {
            return Result.success(categoryService.addCategory(category));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "更新分类")
    @PutMapping("/update")
    public Result<Void> updateCategory(@RequestBody ItemCategory category) {
        try {
            categoryService.updateCategory(category);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "启用/禁用分类")
    @PutMapping("/toggle-status/{id}")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        try {
            categoryService.toggleStatus(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
