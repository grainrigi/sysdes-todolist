# -- Table definitions

# --- !Ups
INSERT INTO users (name, password, password_salt) VALUES ('hoge', '2da48de22c3db5903832dc17521ec800d213a59855351bd69ff1f453f0555f01', 'salt');

# --- !Downs
DELETE FROM users WHERE name = 'hoge';
