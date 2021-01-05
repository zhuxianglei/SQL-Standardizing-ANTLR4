#!/usr/bin/env python
# -*- coding: utf-8 -*-
'''
	Version  Created by     Creation Time  Description
	-------  -------------  -------------  ----------------------------------------------
	1.0      xlzhu@ips.com  2019-09-23     SQLStandardizing system python entry
    
	Version  Updated by     Update Time    Description
	-------  -------------  -------------  ----------------------------------------------

'''
import os
import sys
from subprocess import Popen, PIPE,TimeoutExpired
import FilesComparer

APP_PATH=os.path.split(os.path.realpath(__file__))[0]+'\\'
SQL_ORIFILE='new'
SQL_NEWFILE='new.sql'
DIFF_FILE='diffSQLs.html'
JAVA_APP='SqlFileProcessor'

def exec_javaapp(pappcmd,pparas,pcharset):
    #print(pappcmd)
    #print(pparas)
    if pcharset[0:3].upper()=='UTF':
      lcharset='utf-8'
    else:
      lcharset='gbk'
    procf = Popen(pappcmd+' '+pparas, stdout=PIPE, stdin=PIPE, stderr=PIPE)

	#logaq('dbclient id:'+str(id(procf)))
    try:
      (outf, errf) = procf.communicate()
      #logaq('create views end')
    except TimeoutExpired:
      #logaq('dbclient timeout','w')
      procf.terminate()
      (outf,errf) = procf.communicate()
    except Exception as e:
      #logaq('dbclient exception: %s' % e,'w')
      (outf,errf) = procf.communicate()
		
    if procf.returncode!=0:
      returnmsgf=errf.decode(lcharset)#bug:'utf-8' codec can't decode byte 0xc9 in position 114: invalid continuation byte
      #logaq('returncode:'+str(procf.returncode)+'err:'+returnmsgf+'out:'+outf.decode(lcharset),'e')
      if len(returnmsgf)==0:
        returnmsgf=outf.decode(lcharset)#bug:'utf-8' codec can't decode byte 0xc9 in position 114: invalid continuation byte
      if len(returnmsgf)==0:
        returnmsgf="Error:java terminated"
		#sys.exit(procf.returncode)
    else:
      returnmsgf=outf.decode(lcharset)#bug:'utf-8' codec can't decode byte 0xc9 in position 114: invalid continuation byte
    return returnmsgf
print('-- Starting')
print('-- Processing File:'+sys.argv[1]+'...')
print(exec_javaapp('java ',JAVA_APP+' '+APP_PATH+sys.argv[1],'gbk'));
FilesComparer.compare_file(APP_PATH+sys.argv[1], APP_PATH+'new'+sys.argv[1], APP_PATH+DIFF_FILE)
print('-- NewSQLFile:'+APP_PATH+'new'+sys.argv[1])
print('-- DiffFile:'+APP_PATH+DIFF_FILE)
print('-- Over.')