package com.shiksha.scheduler.repository;

import com.shiksha.scheduler.model.ApplicationStatus;
import com.shiksha.scheduler.model.Job;
import com.shiksha.scheduler.model.JobApplication;
import com.shiksha.scheduler.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    List<JobApplication> findByCandidate(User candidate);

    List<JobApplication> findByJob(Job job);

    List<JobApplication> findByStatus(ApplicationStatus status);

    Optional<JobApplication> findByCandidateAndJob(User candidate, Job job);

    boolean existsByCandidateAndJob(User candidate, Job job);

    @Query("SELECT ja FROM JobApplication ja WHERE ja.job.createdBy = :hr ORDER BY ja.appliedAt DESC")
    List<JobApplication> findAllApplicationsForHR(@Param("hr") User hr);

    long countByStatus(ApplicationStatus status);
}
