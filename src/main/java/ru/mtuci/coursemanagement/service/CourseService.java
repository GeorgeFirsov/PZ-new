package ru.mtuci.coursemanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mtuci.coursemanagement.model.Course;
import ru.mtuci.coursemanagement.repository.CourseRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository repo;

    public List<Course> findAll() {
        return repo.findAll();
    }

    public Course save(Course c) {
        return repo.save(c);
    }

    public Course get(Long id) {
        return repo.findById(id).orElse(null);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
//убираем sql инъецию
    public List<Course> searchByTitle(String title) {
        if (title == null || title.isBlank()) {
            return repo.findAll();
        }
        return repo.findByTitleContainingIgnoreCase(title);
    }
}
