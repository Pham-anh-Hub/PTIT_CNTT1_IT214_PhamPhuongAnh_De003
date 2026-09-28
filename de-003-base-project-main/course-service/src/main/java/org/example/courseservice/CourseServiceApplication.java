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


//    @Bean
//    public CommandLineRunner runner(){
//        return args -> {
//            List<Course> courses = List.of(
//                    new Course(1L,"Course 1", "Course so 01", BigDecimal.valueOf(1200000), 10),
//                    new Course(2L,"Course 2", "Course so 02", BigDecimal.valueOf(1500000), 10),
//                    new Course(3L,"Course 3", "Course so 03", BigDecimal.valueOf(2100000), 10)
//            );
//
//            courseRepository.saveAll(courses);
//        };
//    }


}
