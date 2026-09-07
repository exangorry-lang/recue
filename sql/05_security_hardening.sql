-- Apply once to an existing database after confirming there are no duplicates.
-- The same constraints are included in 01_schema.sql for new installations.

USE `rescue_training`;

SET NAMES utf8mb4;

ALTER TABLE `exam_record`
  ADD UNIQUE KEY `uk_session_user` (`session_id`, `user_id`);

ALTER TABLE `exam_answer`
  ADD UNIQUE KEY `uk_record_question` (`record_id`, `question_id`);

ALTER TABLE `exam_score`
  ADD UNIQUE KEY `uk_record_id` (`record_id`);
