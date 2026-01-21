create table users(
    id bigint auto_increment primary key,
    user_name varchar(100),
    email varchar(50),
    password TEXT,
    is_first_login bool,
    role varchar(50)
);