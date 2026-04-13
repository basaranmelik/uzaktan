package com.guzem.uzaktan.repository;

import com.guzem.uzaktan.model.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstructorRepository extends JpaRepository<Instructor, Long> {

    List<Instructor> findAllByOrderByNameAsc();
}
