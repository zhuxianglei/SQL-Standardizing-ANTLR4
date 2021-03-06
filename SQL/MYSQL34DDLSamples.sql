﻿
ALTER DATABASE CHARACTER SET UTF8;
ALTER EVENT MYTESTEVENT
    ON SCHEDULE
      EVERY 12 HOUR
    STARTS CURRENT_TIMESTAMP + INTERVAL 4 HOUR;

ALTER FUNCTION FUNNAME COMMENT 'func comment';
ALTER INSTANCE ROTATE INNODB MASTER KEY;
ALTER LOGFILE GROUP LOGGP3
    ADD UNDOFILE 'UNDO_10.DAT'
    INITIAL_SIZE=32M
    ENGINE=NDBCLUSTER;

ALTER PROCEDURE PROCNAME COMMENT 'proc comment';

ALTER SERVER s OPTIONS (USER 'sally');
-- add column,add index/key/constraint/check
-- change column c1 c2 varchar(20)
-- modify column c1 varchar(20)
-- rename index/key/tablname
-- add partition
ALTER TABLE t1 ADD COLUMN c2 INT
ALTER TABLE t2 DROP COLUMN c;-- ***********************
ALTER TABLE t1 CHANGE a b BIGINT NOT NULL;
ALTER TABLE t1 ADD PARTITION (PARTITION p3 VALUES LESS THAN (2002));
ALTER TABLE t1 RENAME t2;
ALTER TABLE t2 ADD INDEX (d), ADD UNIQUE (a);



CREATE TABLE t1 (
    id INT,
    year_col INT
)
PARTITION BY RANGE (year_col) (
    PARTITION p0 VALUES LESS THAN (1991),
    PARTITION p1 VALUES LESS THAN (1995),
    PARTITION p2 VALUES LESS THAN (1999),
    PARTITION p3 VALUES LESS THAN (2003),
    PARTITION p4 VALUES LESS THAN (2007)
);
CREATE EVENT myevent
    ON SCHEDULE AT CURRENT_TIMESTAMP + INTERVAL 1 HOUR
    DO
      UPDATE myschema.mytable SET mycol = mycol + 1;
CREATE INDEX part_of_name ON customer (name(10));
CREATE UNIQUE INDEX part_of_ID ON customer (ID(10));


delimiter //
CREATE PROCEDURE simpleproc (OUT param1 INT)
BEGIN
    SELECT COUNT(*) INTO param1 FROM t;
END;//
delimiter ;

delimiter //-- 不要这个会无法识别
CREATE FUNCTION `SHORTEN`(S VARCHAR(255), N INT)
    RETURNS varchar(255)
BEGIN
   IF ISNULL(S) THEN
      RETURN '';
   ELSEIF N<15 THEN
      RETURN LEFT(S, N);
   ELSE
      IF CHAR_LENGTH(S) <=N THEN
          RETURN S;
      ELSE
          RETURN CONCAT(LEFT(S, N-10), '...', RIGHT(S, 5));
      END IF;
   END IF;
END;//
delimiter ;

CREATE TABLE T_TEST02
( SN_KEY INT PRIMARY KEY,
  SN VARCHAR(20),
  PN VARCHAR(20),
  MD VARCHAR(20),
  DESCP VARCHAR(20),
  INDEX IDX_SN (SN),
  KEY IDX_PN(PN),
  CONSTRAINT UK_MD UNIQUE KEY(MD) 
)

CREATE VIEW test.v AS SELECT * FROM t;
delimiter //
CREATE TRIGGER demo BEFORE DELETE
ON users FOR EACH ROW
BEGIN
 INSERT INTO logs VALUES(NOW());
INSERT INTO logs VALUES(NOW());
END;//
delimiter ;

RENAME TABLE old_table TO new_table;
ALTER TABLE old_table RENAME new_table;

ALTER TABLE `TEST02` ADD CONSTRAINT PK_SN_KEY PRIMARY KEY (SN_KEY) COMMENT 'INDEX SN_KEY';
