DROP DATABASE IF EXISTS projectmanagerdb;
CREATE SCHEMA projectmanagerdb;

DROP TABLE IF EXISTS projectmanagerdb.users;
CREATE TABLE projectmanagerdb.users (
	`id` INT NOT NULL AUTO_INCREMENT,
	`username` VARCHAR(35) NOT NULL,
    `password` VARCHAR(90) NOT NULL,
    `first_name` VARCHAR(35) NOT NULL,
    `last_name` VARCHAR(35) NOT NULL,
	PRIMARY KEY (`id`));

DROP TABLE IF EXISTS projectmanagerdb.projects;
CREATE TABLE projectmanagerdb.projects (
	`id`INT NOT NULL AUTO_INCREMENT,
	`owner_id`INT NOT NULL,
    `project_name` VARCHAR(35) NOT NULL,
    `project_description` VARCHAR(1000),
    `start` DATE NOT NULL,
    `deadline` DATE NOT NULL,
    `time_estimate` INT DEFAULT 0,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`owner_id`) REFERENCES projectmanagerdb.users(`id`)
    ON DELETE CASCADE);

DROP TABLE IF EXISTS projectmanagerdb.project_users;    
CREATE TABLE projectmanagerdb.project_users (
	`project_id` INT NOT NULL,
    `user_id` INT NOT NULL,
    PRIMARY KEY (`project_id`, `user_id`),
    FOREIGN KEY (`project_id`) REFERENCES projectmanagerdb.projects(`id`)
    ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES projectmanagerdb.users(`id`));
    
DROP TABLE IF EXISTS projectmanagerdb.tasks;    
CREATE TABLE projectmanagerdb.tasks ( 
	`id` INT NOT NULL AUTO_INCREMENT,
	`project_id` INT NOT NULL,
    `task_name` VARCHAR(35) NOT NULL,
    `task_description` VARCHAR(1000),
    `start` DATE,
    `deadline` DATE,
    `time_estimate` INT DEFAULT 0,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`project_id`) REFERENCES projectmanagerdb.projects(`id`)
    ON DELETE CASCADE);
    
DROP TABLE IF EXISTS projectmanagerdb.subprojects;
CREATE TABLE projectmanagerdb.subprojects (
	`id` INT NOT NULL AUTO_INCREMENT,
	`project_id` INT NOT NULL,
    `subproject_name` VARCHAR(35) NOT NULL,
    `subproject_description` VARCHAR(1000),
    `start` DATE,
    `deadline` DATE,
    `time_estimate` INT DEFAULT 0,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`project_id`) REFERENCES projectmanagerdb.projects(`id`)
    ON DELETE CASCADE);

DROP TABLE IF EXISTS projectmanagerdb.subtasks;
CREATE TABLE projectmanagerdb.subtasks (
	`id` INT NOT NULL AUTO_INCREMENT,
	`subproject_id` INT NOT NULL,
    `subtask_name` VARCHAR(35) NOT NULL,
    `subtask_description` VARCHAR(1000),
    `start` DATE,
    `deadline` DATE,
    `time_estimate` INT DEFAULT 0,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`subproject_id`) REFERENCES projectmanagerdb.subprojects(`id`)
    ON DELETE CASCADE);
    
USE projectmanagerdb;

/* Triggers herunder opdaterer project og subproject time_estimate når
	time_estimate i tasks og subproject_tasks med deres id som foreign key opdateres eller insertes
*/
DELIMITER //
CREATE TRIGGER update_project_time_estimate_tasks
AFTER UPDATE ON tasks
FOR EACH ROW
BEGIN
	IF NEW.time_estimate <> OLD.time_estimate THEN
    UPDATE projects
    SET time_estimate = (
        SELECT SUM(time_estimate) 
        FROM (
            SELECT time_estimate FROM tasks WHERE project_id = NEW.project_id
            UNION ALL
            SELECT time_estimate FROM subprojects WHERE project_id = NEW.project_id
        ) AS time_estimates
    )
    WHERE id = NEW.project_id;
END IF;
END //

CREATE TRIGGER insert_project_time_estimate_tasks
AFTER INSERT ON tasks
FOR EACH ROW
BEGIN
    UPDATE projects
    SET time_estimate = (
        SELECT SUM(time_estimate) 
        FROM (
            SELECT time_estimate FROM tasks WHERE project_id = NEW.project_id
            UNION ALL
            SELECT time_estimate FROM subprojects WHERE project_id = NEW.project_id
        ) AS time_estimates
    )
    WHERE id = NEW.project_id;
END //

CREATE TRIGGER update_subproject_time_estimate_subtasks
AFTER UPDATE ON subtasks
FOR EACH ROW
BEGIN
	IF NEW.time_estimate <> OLD.time_estimate THEN
    UPDATE subprojects
    SET time_estimate = (
        SELECT SUM(time_estimate) 
        FROM (
            SELECT time_estimate FROM subtasks WHERE subproject_id = NEW.subproject_id
        ) AS time_estimates
    )
    WHERE id = NEW.subproject_id;
END IF;
END //

CREATE TRIGGER insert_subproject_time_estimate_subtasks
AFTER INSERT ON subtasks
FOR EACH ROW
BEGIN
    UPDATE subprojects
    SET time_estimate = (
        SELECT SUM(time_estimate) 
        FROM (
            SELECT time_estimate FROM subtasks WHERE subproject_id = NEW.subproject_id
        ) AS time_estimates
    )
    WHERE id = NEW.subproject_id;
END //



CREATE TRIGGER update_project_time_estimate_subprojects
AFTER UPDATE ON subprojects
FOR EACH ROW
BEGIN
	IF NEW.time_estimate <> OLD.time_estimate THEN
    UPDATE projects
    SET time_estimate = (
        SELECT SUM(time_estimate) 
        FROM (
            SELECT time_estimate FROM tasks WHERE project_id = NEW.project_id
            UNION ALL
            SELECT time_estimate FROM subprojects WHERE project_id = NEW.project_id
        ) AS time_estimates
    )
    WHERE id = NEW.project_id;
END IF;
END //


/* Triggers herunder opdaterer project og subproject time_estimate når
	kolonner i tasks og subproject_tasks med deres id som foreign key slettes
*/

CREATE TRIGGER update_project_time_estimate_tasks_delete
AFTER DELETE ON tasks
FOR EACH ROW
BEGIN
	UPDATE projects
	SET time_estimate = (
		SELECT SUM(time_estimate)
		FROM (
			SELECT time_estimate FROM tasks WHERE project_id = OLD.project_id
			UNION ALL
			SELECT time_estimate FROM subprojects WHERE project_id = OLD.project_id
		) AS time_estimates
	)
	WHERE id = OLD.project_id;
END //

CREATE TRIGGER update_subproject_time_estimate_subtasks_delete
AFTER DELETE ON subtasks
FOR EACH ROW
BEGIN
	UPDATE subprojects
	SET time_estimate = (
		SELECT SUM(time_estimate)
		FROM (
			SELECT time_estimate FROM subtasks WHERE subproject_id = OLD.subproject_id
		) AS time_estimates
	)
	WHERE id = OLD.subproject_id;
END //

CREATE TRIGGER update_project_time_estimate_subprojects_delete
AFTER DELETE ON subprojects
FOR EACH ROW
BEGIN
	UPDATE projects
	SET time_estimate = (
		SELECT SUM(time_estimate)
		FROM (
			SELECT time_estimate FROM tasks WHERE project_id = OLD.project_id
			UNION ALL
			SELECT time_estimate FROM subprojects WHERE project_id = OLD.project_id
		) AS time_estimates
	)
	WHERE id = OLD.project_id;
END //
DELIMITER ;
