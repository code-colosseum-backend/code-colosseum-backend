package com.sparta.codecolosseumbackend.service;

import com.sparta.codecolosseumbackend.dto.request.CommentRequestDto;
import com.sparta.codecolosseumbackend.dto.response.CommentAllResponseDto;
import com.sparta.codecolosseumbackend.dto.response.CommentResponseDto;
import com.sparta.codecolosseumbackend.dto.response.ResponseDto;
import com.sparta.codecolosseumbackend.entity.Comment;
import com.sparta.codecolosseumbackend.entity.Member;
import com.sparta.codecolosseumbackend.entity.Problem;
import com.sparta.codecolosseumbackend.jwt.JwtProvider;
import com.sparta.codecolosseumbackend.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

	private final ProblemService problemService;
	private final CommentRepository commentRepository;
	private final JwtProvider jwtProvider;



	// 유효성 검사 - 존재하는 멤버인지 확인
	private Member validateMember(HttpServletRequest request) {
		if (!jwtProvider.validateToken(request.getHeader("Refresh-Token"))) {
			return null;
		}
		return jwtProvider.getMemberFromAuthentication();
	}

	// 유효성 검사 - 존재하는 comment 인지 확인
	@Transactional(readOnly = true)
	public Comment isPresentComment(Long commentId){
		Optional<Comment> optionalComment = commentRepository.findById(commentId);
		return optionalComment.orElse(null);
	}



	// comment 작성하기
	@Transactional
	public ResponseDto<?> createComment(Long problemId, CommentRequestDto commentRequestDto, HttpServletRequest request) {

		// 토큰값이 없을 경우
		if(null == request.getHeader("Refresh-Token")) {
			return ResponseDto.fail("MEMBER_NOT_FOUND", "로그인이 필요합니다.");
		}
		if (null == request.getHeader("Authorization")) {
			return ResponseDto.fail(HttpStatus.NOT_FOUND, "로그인이 필요합니다.");
		}
		Member member = validateMember(request);
		if(null == member){
			return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
		}
		if(commentRequestDto.getContent()==null){
			return ResponseDto.fail("CONTENT_EMPTY", "작성된 글이 없습니다.");
		}
		Problem problem = problemService.isPresentProblem(commentRequestDto.getPostId());
		if (null == problem) {
			return ResponseDto.fail("NOT_FOUND", "존재하지 않는 게시글 id 입니다.");
		}

		Comment comment = Comment.builder()
			.member(member)
			.problem(problem)
			.content(commentRequestDto.getContent())
			.build();

		commentRepository.save(comment);
		return ResponseEntity.ok().body(ResponseDto.success(
				CommentResponseDto.builder()
						.commentId(comment.getId())
						.comment(comment.getContent())
						.nickname(comment.getMember().getNickname())
						.createdAt(comment.getCreatedAt())
						.modifiedAt(comment.getModifiedAt())
						.build()
		)+);
	}


	// comment 불러오기(상세조회)
	@Transactional(readOnly = true)
	public ResponseDto<?> getAllComment() {
		List<CommentAllResponseDto> commentAllList = new ArrayList<>();
		List<Comment> commentList = commentRepository.findAllByOrderByModifiedAtDesc();
		for(Comment comment: commentList){
			commentAllList.add(
					CommentAllResponseDto.builder()
							.commentId(comment.getId())
							.comment(comment.getContent())
							.nickname(comment.getMember().getNickname())
							.createdAt(comment.getCreatedAt())
							.modifiedAt(comment.getModifiedAt())
							.build()
			);
		}
		return ResponseDto.success(commentAllList);
	}


	// comment 수정하기
	@Transactional
	public ResponseDto<?> updateComment(Long commentId, CommentRequestDto requestDto, HttpServletRequest request) {

		// 토큰값이 없을 경우
		if (null == request.getHeader("Authorization")) {
			return ResponseDto.fail(HttpStatus.NOT_FOUND, "로그인이 필요합니다.");
		}
		// 존재하지 않는 problemId로 조회하는 경우
		Problem problem = problemService.isPresentProblem(requestDto.getPostId());
		if (null == problem){
			return ResponseDto.fail(HttpStatus.BAD_REQUEST, "존재하지 않는 문제입니다.");
		}
		// 존재하지 않는 commentId로 조회하는 경우
		Comment comment = isPresentComment(commentId);
		if (null == comment){
			return ResponseDto.fail(HttpStatus.BAD_REQUEST, "존재하지 않는 댓글입니다.");
		}

		comment.update(requestDto);
		return ResponseDto.success(
				CommentResponseDto.builder()
						.commentId(comment.getId())
						.comment(comment.getContent())
						.nickname(comment.getMember().getUsername())
						.modifiedAt(comment.getModifiedAt())
						.build()
		);

	}


	// comment 삭제하기
	@Transactional
	public ResponseDto<?> deleteComment(Long commentId, HttpServletRequest request) {

		// 토큰값이 없을 경우
		if (null == request.getHeader("Authorization")) {
			return ResponseDto.fail(HttpStatus.NOT_FOUND, "로그인이 필요합니다.");
		}
		// 존재하지 않는 commentId로 조회하는 경우
		Comment comment = isPresentComment(commentId);
		if (null == comment){
			return ResponseDto.fail(HttpStatus.BAD_REQUEST, "존재하지 않는 댓글입니다.");
		}

		commentRepository.delete(comment);
		return ResponseDto.success("success");
	}

}


