package com.nutricare.config.security;

import com.nutricare.model.dao.UserDao;
import com.nutricare.model.dto.User;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserDao userDao;

    public OAuth2UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 구글에서 유저 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2. 데이터 추출 (구글 기준)
        String provider = userRequest.getClientRegistration().getRegistrationId(); // "google"
        String providerId = oAuth2User.getAttribute("sub"); // 구글 고유 ID
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // 3. DB 저장 또는 업데이트 로직
        // 주의: MyBatis Dao에 findByEmail, insert, update 메서드가 있다고 가정
        User userEntity = userDao.findByEmail(email); // 이메일로 기존 회원 확인

        if (userEntity == null) {
            // 없으면 회원가입
            userEntity = new User();
            userEntity.setEmail(email);
            userEntity.setName(name);
            userEntity.setPasswordHash(UUID.randomUUID().toString()); // 비밀번호는 랜덤 더미값
            userEntity.setProvider(provider);
            userEntity.setProviderId(providerId);
            userEntity.setRole("USER");
            userDao.insertUser(userEntity); // DB 저장
        } else {
            // 있으면 정보 업데이트 (선택사항)
            // userEntity.setName(name);
            // userDao.update(userEntity);
        }

        // 4. 리턴 (SuccessHandler에서 쓸 수 있도록 속성 그대로 반환)
        return new CustomUserDetails(userEntity, oAuth2User.getAttributes());
    }
}