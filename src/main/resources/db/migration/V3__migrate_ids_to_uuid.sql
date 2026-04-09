ALTER TABLE user_roles
    DROP FOREIGN KEY fk_user_roles_user,
    DROP FOREIGN KEY fk_user_roles_role;

ALTER TABLE menu_items
    DROP FOREIGN KEY fk_menu_restaurant;

ALTER TABLE chat_history
    DROP FOREIGN KEY fk_chat_history_user;

ALTER TABLE roles ADD COLUMN new_id CHAR(36) NULL;
ALTER TABLE users ADD COLUMN new_id CHAR(36) NULL;
ALTER TABLE restaurants ADD COLUMN new_id CHAR(36) NULL;
ALTER TABLE menu_items ADD COLUMN new_id CHAR(36) NULL;
ALTER TABLE chat_history ADD COLUMN new_id CHAR(36) NULL;

UPDATE roles SET new_id = UUID();
UPDATE users SET new_id = UUID();
UPDATE restaurants SET new_id = UUID();
UPDATE menu_items SET new_id = UUID();
UPDATE chat_history SET new_id = UUID();

ALTER TABLE user_roles
    ADD COLUMN new_user_id CHAR(36) NULL,
    ADD COLUMN new_role_id CHAR(36) NULL;

ALTER TABLE menu_items
    ADD COLUMN new_restaurant_id CHAR(36) NULL;

ALTER TABLE chat_history
    ADD COLUMN new_user_id CHAR(36) NULL;

UPDATE user_roles ur
JOIN users u ON ur.user_id = u.id
SET ur.new_user_id = u.new_id;

UPDATE user_roles ur
JOIN roles r ON ur.role_id = r.id
SET ur.new_role_id = r.new_id;

UPDATE menu_items mi
JOIN restaurants r ON mi.restaurant_id = r.id
SET mi.new_restaurant_id = r.new_id;

UPDATE chat_history ch
LEFT JOIN users u ON ch.user_id = u.id
SET ch.new_user_id = u.new_id;

ALTER TABLE roles DROP PRIMARY KEY;
ALTER TABLE roles DROP COLUMN id;
ALTER TABLE roles CHANGE COLUMN new_id id CHAR(36) NOT NULL;
ALTER TABLE roles ADD PRIMARY KEY (id);

ALTER TABLE users DROP PRIMARY KEY;
ALTER TABLE users DROP COLUMN id;
ALTER TABLE users CHANGE COLUMN new_id id CHAR(36) NOT NULL;
ALTER TABLE users ADD PRIMARY KEY (id);

ALTER TABLE restaurants DROP PRIMARY KEY;
ALTER TABLE restaurants DROP COLUMN id;
ALTER TABLE restaurants CHANGE COLUMN new_id id CHAR(36) NOT NULL;
ALTER TABLE restaurants ADD PRIMARY KEY (id);

ALTER TABLE menu_items DROP PRIMARY KEY;
ALTER TABLE menu_items DROP COLUMN id;
ALTER TABLE menu_items CHANGE COLUMN new_id id CHAR(36) NOT NULL;
ALTER TABLE menu_items ADD PRIMARY KEY (id);

ALTER TABLE chat_history DROP PRIMARY KEY;
ALTER TABLE chat_history DROP COLUMN id;
ALTER TABLE chat_history CHANGE COLUMN new_id id CHAR(36) NOT NULL;
ALTER TABLE chat_history ADD PRIMARY KEY (id);

ALTER TABLE user_roles DROP PRIMARY KEY;
ALTER TABLE user_roles DROP COLUMN user_id;
ALTER TABLE user_roles DROP COLUMN role_id;
ALTER TABLE user_roles CHANGE COLUMN new_user_id user_id CHAR(36) NOT NULL;
ALTER TABLE user_roles CHANGE COLUMN new_role_id role_id CHAR(36) NOT NULL;
ALTER TABLE user_roles ADD PRIMARY KEY (user_id, role_id);
ALTER TABLE user_roles
    ADD CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id),
    ADD CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id);

ALTER TABLE menu_items DROP COLUMN restaurant_id;
ALTER TABLE menu_items CHANGE COLUMN new_restaurant_id restaurant_id CHAR(36) NOT NULL;
ALTER TABLE menu_items
    ADD CONSTRAINT fk_menu_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id);

ALTER TABLE chat_history DROP COLUMN user_id;
ALTER TABLE chat_history CHANGE COLUMN new_user_id user_id CHAR(36) NULL;
ALTER TABLE chat_history
    ADD CONSTRAINT fk_chat_history_user FOREIGN KEY (user_id) REFERENCES users(id);
