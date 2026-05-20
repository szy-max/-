package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.JwtProperties;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import springfox.documentation.spring.web.json.Json;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        String code = userLoginDTO.getCode();
        HashMap<String, String> map = new HashMap<>();
        map.put("js_code", code);
        map.put("appid", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("grant_type", weChatProperties.getGrant_type());
        String json = HttpClientUtil.doGet(weChatProperties.getLoginUrl(), map);
        String openid = JSON.parseObject(json).getString("openid");
        if(openid == null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        User oldUser = userMapper.selece(openid);
        if(oldUser==null){User user = new User();
            user.setOpenid(openid);
            user.setCreateTime(LocalDateTime.now());
            userMapper.save(user);
            Map<String, Object> hashMap = new HashMap() {{
                put(JwtClaimsConstant.USER_ID, user.getId());
            }};
            String jwt = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), hashMap);
            return  new UserLoginVO(user.getId(), openid, jwt);
        }
        Map<String, Object> hashMap = new HashMap() {{
            put(JwtClaimsConstant.USER_ID, oldUser.getId());
        }};
        String jwt = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), hashMap);
        return new UserLoginVO(oldUser.getId(), openid, jwt);
    }
}
