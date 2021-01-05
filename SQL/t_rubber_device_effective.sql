/*
Navicat MySQL Data Transfer

Source Server         : 橡胶开发
Source Server Version : 50724
Source Host           : 192.168.23.47:3306
Source Database       : rubber

Target Server Type    : MYSQL
Target Server Version : 50724
File Encoding         : 65001

Date: 2019-10-09 09:35:42
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for t_rubber_device_effective
-- ----------------------------
DROP TABLE IF EXISTS `t_rubber_device_effective`;
CREATE TABLE `t_rubber_device_effective` (
  `device_no` varchar(40) NOT NULL COMMENT '设备号',
  `effective_device` varchar(2) NOT NULL DEFAULT '1' COMMENT '1#设备有效 0#无效',
  PRIMARY KEY (`device_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='设备白名单表';
