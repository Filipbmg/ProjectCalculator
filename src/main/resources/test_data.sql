USE projectmanagerdb;
INSERT INTO projectmanagerdb.users (username, password, first_name, last_name) VALUES
    ('user1', 'password', 'Dennis', 'Hansen'),
    ('user2', 'password', 'Dennis2', 'Hansen2'),
    ('user3', 'password', 'Dennis3', 'Hansen3'),
    ('user4', 'password', 'Dennis4', 'Hansen4');

INSERT INTO projectmanagerdb.projects (owner_id, project_name, project_description, start, deadline, time_estimate) VALUES
    (1, 'Project 1', 'Dette er et test projekt', '2023-06-01', '2023-06-28', 0),
    (1, 'Project 2', 'Dette er et test projekt2', '2023-06-01', '2023-06-30', 0),
    (2, 'Project 3', 'Dette er et test projekt3', '2023-05-01', '2023-06-19', 0);

INSERT INTO projectmanagerdb.project_users (project_id, user_id) VALUES
    (1, 2),
    (1, 3),
    (2, 1),
    (1, 1),
    (3, 1),
    (3, 2);

INSERT INTO projectmanagerdb.tasks (project_id, task_name, task_description, start, deadline, time_estimate) VALUES
    (1, 'Task 1', 'dette er en test opgave', '2023-06-01', '2023-06-10', 10),
    (1, 'Task 2', 'dette er en test opgave', '2023-06-06', '2023-06-13', 15),
    (2, 'Task 1', 'dette er en test opgave', '2023-06-08', '2023-06-20', 20),
    (3, 'Task 1', 'dette er en test opgave', '2023-05-31', '2023-06-26', 25),
    (3, 'Task 2', 'dette er en test opgave', '2023-5-21', '2023-06-28', 30);

INSERT INTO projectmanagerdb.subprojects (project_id, subproject_name, subproject_description, start, deadline, time_estimate) VALUES
    (1, 'Subproject 1', 'dette er et test delprojekt', '2023-06-07', '2023-06-20', 0),
    (1, 'Subproject 2', 'dette er et test delprojekt', '2023-06-04', '2023-07-08', 0),
    (2, 'Subproject 1', 'dette er et test delprojekt', '2023-06-10', '2023-07-10', 0);

INSERT INTO projectmanagerdb.subtasks (subproject_id, subtask_name, subtask_description, start, deadline, time_estimate) VALUES
    (1, 'Subtask 1', 'dette er en test delopgave', '2023-06-01', '2023-06-15', 2),
    (1, 'Subtask 2', 'dette er en test delopgave', '2023-06-16', '2023-06-28', 3),
    (2, 'Subtask 1', 'dette er en test delopgave', '2023-06-11', '2023-07-15', 4);