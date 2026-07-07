package com.anime.website.controller;

import com.anime.website.dto.ApiResponse;
import com.anime.website.dto.comment.AddCommentRequest;
import com.anime.website.dto.comment.CommentDTO;
import com.anime.website.security.UserPrincipal;
import com.anime.website.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评论控制器 - 处理评论相关请求
 * 注意：context-path已配置为/api，所以这里不需要再加/api前缀
 */
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;

    /**
     * 发表评论
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CommentDTO>> addComment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody AddCommentRequest request) {
        
        CommentDTO comment = commentService.addComment(userPrincipal.getId(), request);
        // 使用正确的参数顺序：(message, data)
        return ResponseEntity.ok(ApiResponse.success("评论发表成功", comment));
    }

    /**
     * 获取动漫评论列表（外部动漫）
     */
    @GetMapping("/anime/external/{externalAnimeId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCommentsByExternalAnime(
            @PathVariable String externalAnimeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<CommentDTO> comments = commentService.getCommentsByAnime(externalAnimeId, page, size);
        long totalCount = commentService.getCommentCount(externalAnimeId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("comments", comments.getContent());
        result.put("totalElements", comments.getTotalElements());
        result.put("totalPages", comments.getTotalPages());
        result.put("currentPage", page);
        result.put("totalComments", totalCount);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 获取评论的回复列表
     */
    @GetMapping("/{commentId}/replies")
    public ResponseEntity<ApiResponse<List<CommentDTO>>> getReplies(@PathVariable Long commentId) {
        List<CommentDTO> replies = commentService.getReplies(commentId);
        return ResponseEntity.ok(ApiResponse.success(replies));
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long commentId) {
        
        commentService.deleteComment(userPrincipal.getId(), commentId);
        // 删除操作只返回消息
        return ResponseEntity.ok(ApiResponse.success("评论已删除"));
    }

    /**
     * 点赞评论
     */
    @PostMapping("/{commentId}/like")
    public ResponseEntity<ApiResponse<CommentDTO>> likeComment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long commentId) {
        
        CommentDTO comment = commentService.likeComment(userPrincipal.getId(), commentId);
        // 使用正确的参数顺序：(message, data)
        return ResponseEntity.ok(ApiResponse.success("点赞成功", comment));
    }
}

