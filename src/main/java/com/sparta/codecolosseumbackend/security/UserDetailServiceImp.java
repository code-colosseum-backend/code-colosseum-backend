package com.sparta.codecolosseumbackend.security;

import com.sparta.codecolosseumbackend.entity.Member;
import com.sparta.codecolosseumbackend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


// /login 요청이 오면 자동으로 UserDetailsService 타입으로 IoC되어 있는 loadUserByUsername 함수가 실행
@Service
@RequiredArgsConstructor
public class UserDetailServiceImp implements UserDetailsService {

    private final MemberRepository memberRepository;

    //시큐리티 session(내부 Authentication(내부 userDetails))
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("아이디를 찾을 수 없습니다." + username));
        return new UserDetailImp(member);
    }
}