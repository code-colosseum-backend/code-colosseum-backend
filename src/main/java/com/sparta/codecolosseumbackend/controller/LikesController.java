package com.sparta.codecolosseumbackend.controller;

import com.sparta.codecolosseumbackend.dto.response.ResponseDto;
import com.sparta.codecolosseumbackend.entity.Member;
import com.sparta.codecolosseumbackend.service.LikesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class LikesController {
    private final LikesService likesService;

//    // 좋아요 기능
//    @PostMapping("/auth/post/likes/{postId}")
//    public ResponseDto<?> like(@AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable Long postId) {
//        return likesService.like(userDetails.getAccount(), postId);
//    }


    // 좋아요 기능
    @PostMapping("/auth/post/likes/{postId}")
    public ResponseDto<?> like(@PathVariable Long postId) {
        return likesService.like2(postId);
    }
}
