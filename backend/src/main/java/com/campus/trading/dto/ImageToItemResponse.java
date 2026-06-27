package com.campus.trading.dto;

import lombok.Data;

/**
 * AI图片识别生成商品信息响应
 */
@Data
public class ImageToItemResponse {

    private String title;
    private String description;
    private Double price;
    private boolean priceEstimated;  // 价格为AI预估

    public static ImageToItemResponse of(String title, String description, Double price) {
        ImageToItemResponse resp = new ImageToItemResponse();
        resp.title = title;
        resp.description = description;
        resp.price = price;
        resp.priceEstimated = true;
        return resp;
    }
}
