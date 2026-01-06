package org.example.reminderapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.reminderapp.entity.User;
import org.example.reminderapp.entity.enums.Role;
import org.example.reminderapp.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2USerService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String firstname = oAuth2User.getAttribute("given_name");
        String lastname = oAuth2User.getAttribute("family_name");

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFirstname(firstname);
            newUser.setLastname(lastname);
            newUser.setRole(Role.USER);
            newUser.setCreatedAt(OffsetDateTime.now());
            return userRepository.save(newUser);
        });

        log.info("OAuth2 user loaded: {}", email);
        return oAuth2User;
    }
}
