package com.shiksha.scheduler.repository;

import com.shiksha.scheduler.model.Job;
import com.shiksha.scheduler.model.JobStatus;
import com.shiksha.scheduler.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByStatus(JobStatus status);

    List<Job> findByCreatedBy(User hr);

    List<Job> findByDepartment(String department);

    List<Job> findByStatusOrderByCreatedAtDesc(JobStatus status);

    long countByStatus(JobStatus status);
}
