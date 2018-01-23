
CREATE VIRTUAL TABLE p USING fts4(k TEXT);
INSERT INTO p (docid, k) VALUES (1, 'this is a test');
INSERT INTO p (docid, k) VALUES (2, 'this is another test');

SELECT * FROM p WHERE k MATCH 'text';
-- <nothing>

SELECT * FROM p WHERE k MATCH 'test';
-- this is a test
-- this is another test

SELECT docid FROM p WHERE k MATCH 'test';
-- 1
-- 2


