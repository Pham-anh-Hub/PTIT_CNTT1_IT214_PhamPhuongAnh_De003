package org.example.courseservice;

import lombok.RequiredArgsConstructor;
import org.example.courseservice.models.entities.Course;
import org.example.courseservice.models.repositories.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.util.List;

@SpringBootApplication
@EnableCaching
@RequiredArgsConstructor
@EnableDiscoveryClient
public class CourseServiceApplication {
    private final CourseRepository courseRepository;

    public static void main(String[] args) {
        SpringApplication.run(CourseServiceApplication.class, args);
    }


    @Bean
    public CommandLineRunner initData(CourseRepository courseRepository) {
        return args -> {
            // Kiểm tra database rỗng thì mới thêm dữ liệu mẫu để tránh duplicate khi restart
            if (courseRepository.count() == 0) {
                Course course1 = new Course(
                        1L,
                        "Lập trình Java Spring Boot",
                        "Khóa học xây dựng Microservices",
                        new BigDecimal("1500000"),
                        50
                );

                Course course2 = new Course(
                        2L,
                        "Cấu trúc dữ liệu và giải thuật",
                        "Khóa học nền tảng cho sinh viên IT",
                        new BigDecimal("800000"),
                        100
                );

                Course course3 = new Course(
                        3L,
                        "Phân tích thiết kế hệ thống",
                        "Hướng dẫn vẽ UML và Design Pattern",
                        new BigDecimal("1200000"),
                        30
                );

                // Lưu tất cả vào database
                courseRepository.saveAll(List.of(course1, course2, course3));
                System.out.println("Đã lưu dữ liệu mẫu Course thành công!");
            }
        };
    }


}
