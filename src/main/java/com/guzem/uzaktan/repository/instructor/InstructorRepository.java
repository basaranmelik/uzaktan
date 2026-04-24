package com.guzem.uzaktan.repository.instructor;

import com.guzem.uzaktan.model.instructor.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstructorRepository extends JpaRepository<Instructor, Long> {

    List<Instructor> findAllByOrderByNameAsc();
    java.util.Optional<Instructor> findByName(String name);
}
