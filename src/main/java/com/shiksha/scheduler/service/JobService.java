package com.shiksha.scheduler.service;

import com.shiksha.scheduler.dto.JobDTO;
import com.shiksha.scheduler.model.Job;
import com.shiksha.scheduler.model.JobStatus;
import com.shiksha.scheduler.model.User;

import java.util.List;
import java.util.Optional;

public interface JobService {
    Job createJob(JobDTO dto, User createdBy);
    Job updateJob(Long id, JobDTO dto);
    Optional<Job> findById(Long id);
    List<Job> findAll();
    List<Job> findByStatus(JobStatus status);
    List<Job> findByCreatedBy(User hr);
    void deleteJob(Long id);
    long countByStatus(JobStatus status);
}
