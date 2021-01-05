#!/usr/bin/env python
# -*- coding: utf-8 -*-
'''
	Version  Created by     Creation Time  Description
	-------  -------------  -------------  ----------------------------------------------
	1.0      xlzhu@ips.com  2019-09-23     comparing sql files
    
	Version  Updated by     Update Time    Description
	-------  -------------  -------------  ----------------------------------------------

'''
import sys
import difflib

def read_file(filename):
  try:
    with open(filename,'r', encoding='utf-8') as f:
      return f.readlines()
  except IOError:
    print("ERROR: readfile:" % filename)
    sys.exit(1)

def compare_file(file1, file2, out_file):
  file1_content = read_file(file1)
  file2_content = read_file(file2)
  d = difflib.HtmlDiff()
  result = d.make_file(file1_content, file2_content)
  with open(out_file, 'w', encoding='utf-8') as f:
    f.writelines(result)
    
#if __name__ == '__main__':
# compare_file(r'D:\xlzhu\Work\Dev\java\antlr4\mtest2.sql', r'D:\xlzhu\Work\Dev\java\antlr4\mtest2.sql.sql', r'D:\xlzhu\Work\Dev\java\antlr4\diffSQLs.html')