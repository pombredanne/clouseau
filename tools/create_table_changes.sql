create table changes (
    id          integer primary key asc,
    date_time   text,
    user_name   text,
    package     text,
    description text
);

-- insert into changes(data, user_name, package, description) values ('x', 'y', 'z', 'w');
-- insert into changes(data, user_name, package, description) values ('xxx', 'y', 'z', 'w');

