package com.shiksha.scheduler.service;

import com.shiksha.scheduler.dto.JobDTO;
import com.shiksha.scheduler.exception.ResourceNotFoundException;
import com.shiksha.scheduler.model.Job;
import com.shiksha.scheduler.model.JobStatus;
import com.shiksha.scheduler.model.User;
import com.shiksha.scheduler.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class JobServiceImpl implements JobService {

    @Autowired
    private JobRepository jobRepository;

    @Override
    public Job createJob(JobDTO dto, User createdBy) {
        Job job = Job.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .department(dto.getDepartment())
                .requiredSkills(dto.getRequiredSkills())
                .experienceRequired(dto.getExperienceRequired())
                .status(dto.getStatus() != null ? dto.getStatus() : JobStatus.OPEN)
                .createdBy(createdBy)
                .build();
        return jobRepository.save(job);
    }

    @Override
    public Job updateJob(Long id, JobDTO dto) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job", id));
        job.setTitle(dto.getTitle());
        job.setDescription(dto.getDescription());
        job.setDepartment(dto.getDepartment());
        job.setRequiredSkills(dto.getRequiredSkills());
        job.setExperienceRequired(dto.getExperienceRequired());
        job.setStatus(dto.getStatus());
        return jobRepository.save(job);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Job> findById(Long id) {
        return jobRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Job> findAll() {
        return jobRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Job> findByStatus(JobStatus status) {
        return jobRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Job> findByCreatedBy(User hr) {
        return jobRepository.findByCreatedBy(hr);
    }

    @Override
    public void deleteJob(Long id) {
        if (!jobRepository.existsById(id)) {
            throw new ResourceNotFoundException("Job", id);
        }
        jobRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(JobStatus status) {
        return jobRepository.countByStatus(status);
    }
}
