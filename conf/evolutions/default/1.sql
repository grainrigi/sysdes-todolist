# -- Table definitions

# --- !Ups
CREATE TABLE users (
    id int PRIMARY KEY AUTO_INCREMENT,
    name varchar(32) NOT NULL,
    password varchar(64) NOT NULL,
    password_salt varchar(32) NOT NULL
);

CREATE TABLE tasks (
    id int PRIMARY KEY AUTO_INCREMENT,
    user_id int NOT NULL,
    title varchar(32) NOT NULL,
    description varchar(255),
    is_done boolean default FALSE,
    created_at timestamp default CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

# --- !Downs
DROP TABLE enquete;

DROP TABLE task;

DROP TABLE `user`;