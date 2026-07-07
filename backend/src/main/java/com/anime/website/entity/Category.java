package com.anime.website.entity;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashSet;
import java.util.Set;

/**
 * 动漫分类实体类 - 存储动漫分类信息
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image")
    private String image; // 分类图片URL

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    // 多对多关系：分类-动漫（反向关联）
    @JsonIgnore // 避免JSON序列化时的懒加载问题
    @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY)
    private Set<Anime> animes = new HashSet<>();
}
