package com.anime.website.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 外部API响应DTO - 豪华资源API响应格式
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalAnimeResponse {

    // 响应状态码
    private Integer code;
    
    // 响应消息
    private String msg;
    
    // 当前页码
    private String page;
    
    // 总页数
    private Integer pagecount;
    
    // 每页数量限制
    private String limit;
    
    // 总记录数
    private Integer total;
    
    // 动漫列表数据
    private List<ExternalAnimeItem> list;
    
    // 分类信息
    @JsonProperty("class")
    private List<ExternalCategory> categories;

    /**
     * 外部动漫项目DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExternalAnimeItem {
        
        // 视频ID
        @JsonProperty("vod_id")
        private Integer vodId;
        
        // 动漫名称
        @JsonProperty("vod_name")
        private String vodName;
        
        // 分类ID
        @JsonProperty("type_id")
        private Integer typeId;
        
        // 分类名称
        @JsonProperty("type_name")
        private String typeName;
        
        // 英文名称
        @JsonProperty("vod_en")
        private String vodEn;
        
        // 更新时间
        @JsonProperty("vod_time")
        private String vodTime;
        
        // 备注信息（如：第几集）
        @JsonProperty("vod_remarks")
        private String vodRemarks;
        
        // 播放源
        @JsonProperty("vod_play_from")
        private String vodPlayFrom;
        
        // 封面图片
        @JsonProperty("vod_pic")
        private String vodPic;
        
        // 演员
        @JsonProperty("vod_actor")
        private String vodActor;
        
        // 导演
        @JsonProperty("vod_director")
        private String vodDirector;
        
        // 简介
        @JsonProperty("vod_blurb")
        private String vodBlurb;
        
        // 详细内容
        @JsonProperty("vod_content")
        private String vodContent;
        
        // 发布日期
        @JsonProperty("vod_pubdate")
        private String vodPubdate;
        
        // 总集数
        @JsonProperty("vod_total")
        private Integer vodTotal;
        
        // 地区
        @JsonProperty("vod_area")
        private String vodArea;
        
        // 语言
        @JsonProperty("vod_lang")
        private String vodLang;
        
        // 年份
        @JsonProperty("vod_year")
        private String vodYear;
        
        // 评分
        @JsonProperty("vod_score")
        private String vodScore;
        
        // 播放链接
        @JsonProperty("vod_play_url")
        private String vodPlayUrl;
        
        // 播放服务器
        @JsonProperty("vod_play_server")
        private String vodPlayServer;
        
        // 播放备注
        @JsonProperty("vod_play_note")
        private String vodPlayNote;
        
        // 观看次数
        @JsonProperty("vod_hits")
        private Integer vodHits;

        // 资源源标识 - 用于标识数据来源
        private String sourceKey;

        // 资源源名称 - 用于显示
        private String sourceName;
    }

    /**
     * 外部分类DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExternalCategory {
        
        // 分类ID
        @JsonProperty("type_id")
        private Integer typeId;
        
        // 父分类ID
        @JsonProperty("type_pid")
        private Integer typePid;
        
        // 分类名称
        @JsonProperty("type_name")
        private String typeName;
    }
}
