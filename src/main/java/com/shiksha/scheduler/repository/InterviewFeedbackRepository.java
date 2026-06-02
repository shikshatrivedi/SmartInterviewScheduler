package com.shiksha.scheduler.repository;

import com.shiksha.scheduler.model.InterviewFeedback;
import com.shiksha.scheduler.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewFeedbackRepository extends JpaRepository<InterviewFeedback, Long> {

    Optional<InterviewFeedback> findByInterview_Id(Long interviewId);

    List<InterviewFeedback> findByInterviewer(User interviewer);
}
