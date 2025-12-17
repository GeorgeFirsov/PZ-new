package ru.mtuci.coursemanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import ru.mtuci.coursemanagement.model.Course;
import ru.mtuci.coursemanagement.repository.CourseRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository repo;
    private final JdbcTemplate jdbc;

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

    public List<Course> searchByTitle(String title) {
        String sql = "SELECT id, title, description, teacher_id FROM courses WHERE title = ?";

        RowMapper<Course> rm = (rs, i) -> {
            Course c = new Course();
            c.setId(rs.getLong("id"));
            c.setTitle(rs.getString("title"));
            c.setDescription(rs.getString("description"));
            c.setTeacherId(rs.getLong("teacher_id"));
            return c;
        };

        return jdbc.query(sql, rm, title);
    }
}
