#!/bin/sh
MASTERPASSWORD='test1234'
MYPASSWORD='hy123456'
XROW=$1
YROW=$2
FILENAME=$3
PERCENT=$4
RAM=$5
CORES=$6
zip -d /home/hy/IdeaProjects/CBDP/out/artifacts/cbdp_jar/cbdp.jar  META-INF/*.SF
rm -rf /home/hy/result* 
expect<<EOF
spawn scp /home/hy/IdeaProjects/CBDP/out/artifacts/cbdp_jar/cbdp.jar root@192.168.1.121:~/
expect "*password"
send "$MASTERPASSWORD\r"
expect eof
EOF
expect<<EOF
spawn scp /home/hy/data/${FILENAME} root@192.168.1.121:~/
expect "*password"
send "$MASTERPASSWORD\r"
expect eof
EOF
expect<<EOF
set timeout -1
spawn ssh root@192.168.1.121
expect "*password:"
send "$MASTERPASSWORD\r"
expect "*#"
send "hdfs dfs -rm  /data/${FILENAME}\r" 
send "hdfs dfs -put  ./${FILENAME}   /data/\r"
expect "*#"
send "rm -rf result*\r" 
send "hdfs dfs -rm -r /data/result*\r"
set timeout -1
send "spark-submit --class sparkcluster.sparkmain --master spark://192.168.1.121:7077  --executor-memory ${RAM}  --total-executor-cores ${CORES}     ./cbdp.jar    ${XROW}  ${YROW}   ${FILENAME}  ${PERCENT} \r"
send "hdfs dfs -copyToLocal /data/result* ~/\r"
send "scp -r result* hy@192.168.1.51:~/\r"
expect "*password"
send "$MYPASSWORD\r"
expect "*#"
send "exit 0\r"
expect eof
EOF

