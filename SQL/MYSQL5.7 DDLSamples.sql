/*
*必须项目
？待定项目
xxx无需关注此类ddl
总类：alter,create,drop,rename
create     event     :name*,comment*        --test ok,TESTOK1018
              fun/proc:name*,comment*        --test proc ok test fun ok,TEST ALL OK1018
              index     : name*,table,column,algorithm*,lock* ---TEST OK ,test ok1018
              table      : name*,column(name,typelen,comment)*,index(indexname,must one pk/uk)*,tablecomment*,partitionname*,engine?,charset? ---TEST OK 20191014 test ok1018
              trigger   :name*,table*,event*    --TEST OK,test ok1018
              view      :name*                         --TEST OK,test ok1018
alter       event     : newname*,comment  --TEST OK,test ok1018
             fun/proc :xxx NO NAME CHANGED
             table       :column(name,typelen,comment)*,index(indexname)*,newpartitionname*,algorithm*,lock*,engine?,charset? alter table rename ok1018 test allok 1018
             view        :xxx NO NAME CHANGED
             trigger     :xxx NO THIS DDL
rename table        :newtablename*         --TEST OK,TEST OK1018 2
drop     xxx
目前可能存在的问题，按关键词替换有可能替换掉其它栏位，后续考虑按正则表达式
*/
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
ALTER TABLE t1 CHANGE a b BIGINT NOT NULL COMMENT 'CMT B';---COMMENT CLEAR ,MUST COMMENT
ALTER TABLE t1 ADD PARTITION (PARTITION p3 VALUES LESS THAN (2002));
ALTER TABLE t1 RENAME t2;
ALTER TABLE t2 ADD INDEX (d), ADD UNIQUE (a);--如果没有index名，默认以第一个列名当index名;KEY=INDEX
ALTER TABLE TEST13 ADD CONSTRAINT CK_F FOREIGN KEY FK_F(C_10) REFERENCES TEST02(SN_KEY);--两个都有名不同以constraint名为主，如果没有constraint系统创建一个默认的名,两名可以相同
ALTER TABLE TEST13 ADD CONSTRAINT CT_C UNIQUE INDEX UK_C(C);--  UNIQUE KEY `UK_C` (`C`)--以index名优先，无index以约束名，无约束用列名
ALTER TABLE TEST13 add CONSTRAINT PK_A PRIMARY KEY(A);-- 约束名无用处，系统默认PRIMARY
RENAME OLDTABLE TO NEWTABLE;
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
   END IF;
END;//
delimiter ;

DELIMITER $
CREATE TRIGGER user_log AFTER INSERT ON users FOR EACH ROW
BEGIN
DECLARE s1 VARCHAR(40)character set utf8;
DECLARE s2 VARCHAR(20) character set utf8;#后面发现中文字符编码出现乱码，这里设置字符集
SET s2 = " is created";
SET s1 = CONCAT(NEW.name,s2);     #函数CONCAT可以将字符串连接
INSERT INTO logs(log) values(s1);
END $
DELIMITER ;

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
