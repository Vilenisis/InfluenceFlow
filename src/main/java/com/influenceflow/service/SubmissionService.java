package com.influenceflow.service;

import com.influenceflow.dao.CreatorDao;
import com.influenceflow.dao.PostMetricDao;
import com.influenceflow.dao.SubmissionDao;
import com.influenceflow.dao.TaskDao;
import com.influenceflow.model.PostMetric;
import com.influenceflow.model.Submission;
import com.influenceflow.model.SubmissionStatus;
import com.influenceflow.util.RegexValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class SubmissionService {
    private final SubmissionDao submissionDao;
    private final PostMetricDao postMetricDao;
    private final TaskDao taskDao;
    private final CreatorDao creatorDao;

    public SubmissionService(SubmissionDao submissionDao, PostMetricDao postMetricDao, TaskDao taskDao, CreatorDao creatorDao) {
        this.submissionDao = submissionDao;
        this.postMetricDao = postMetricDao;
        this.taskDao = taskDao;
        this.creatorDao = creatorDao;
    }

    public Submission submitWork(long taskId, long creatorId, String url) {
        if (!RegexValidator.isValidSubmissionUrl(url)) {
            throw new IllegalArgumentException("URL не соответствует поддерживаемым платформам");
        }
        if (!taskDao.existsById(taskId)) {
            throw new IllegalArgumentException("Задание с идентификатором " + taskId + " не найдено");
        }
        if (!creatorDao.existsById(creatorId)) {
            throw new IllegalArgumentException("Профиль креатора не найден. Повторите регистрацию командой /start");
        }
        if (submissionDao.existsByTaskAndCreator(taskId, creatorId)) {
            throw new IllegalStateException("Вы уже отправили работу по этой задаче");
        }
        Submission submission = new Submission();
        submission.setTaskId(taskId);
        submission.setCreatorId(creatorId);
        submission.setUrl(url);
        submission.setStatus(SubmissionStatus.PENDING);
        submission.setSubmittedAt(LocalDateTime.now());
        return submissionDao.save(submission);
    }

    public List<Submission> getPendingSubmissions() {
        return submissionDao.findByStatus(SubmissionStatus.PENDING);
    }

    public void moderateSubmission(long submissionId, SubmissionStatus status) {
        if (status == SubmissionStatus.PENDING) {
            throw new IllegalArgumentException("Невозможно перевести сдачу обратно в PENDING");
        }
        submissionDao.updateStatus(submissionId, status);
    }

    public PostMetric recordMetrics(long submissionId, Map<String, Integer> metrics) {
        PostMetric postMetric = new PostMetric();
        postMetric.setSubmissionId(submissionId);
        postMetric.setViews(metrics.getOrDefault("views", 0));
        postMetric.setLikes(metrics.getOrDefault("likes", 0));
        postMetric.setComments(metrics.getOrDefault("comments", 0));
        postMetric.setReportedAt(LocalDateTime.now());
        return postMetricDao.save(postMetric);
    }
}
