
create table `t_rubberXx_account` (
  `v_acccode`  VARCHAR(20) NOT NULL COMMENT 'xx??商户代码xxx???',
  `v_amt` DECIMAL(24,4) DEFAULT NULL COMMENT 'cmt:moneny',
   `v_createtime`  DATETIME,
   `v_cnt`  INT,
  primary KEY (`v_acccode`)
) ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT charset=UTF8 COMMENT 'cmt:account table';

ALTER TABLE T_BENE_MER_ACC_TRADE_HIS ADD  COPT_CODE VARCHAR(10) DEFAULT NULL COMMENT '合作平台编号';