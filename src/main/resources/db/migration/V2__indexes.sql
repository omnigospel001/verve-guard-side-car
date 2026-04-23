CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_account_number ON users(account_number);
CREATE INDEX idx_roles_user_id ON roles(user_id);
CREATE INDEX idx_account_user_id ON account_management(user_id);
