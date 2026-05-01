package com.calorimate;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.calorimate.entity.User;
import com.calorimate.mapper.UserMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@MapperScan("com.calorimate.mapper")
public class CaloriMateApplication {

    public static void main(String[] args) {
        SpringApplication.run(CaloriMateApplication.class, args);
    }

    @Bean
    public CommandLineRunner initAdmin(UserMapper userMapper, BCryptPasswordEncoder passwordEncoder) {
        return args -> {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, "admin");
            User admin = userMapper.selectOne(wrapper);
            if (admin != null) {
                admin.setPassword(passwordEncoder.encode("123456"));
                userMapper.updateById(admin);
                System.out.println("Admin password updated successfully.");
            }
        };
    }
}
