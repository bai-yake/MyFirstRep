. ~/.bash_profile
cd /ngbss/webapp/deploy/llhy_sendMsg_10010 
CTTIMERBASEDIR=`pwd`
CLASSPATH=$CTTIMERBASEDIR
for f in `find $CTTIMERBASEDIR/lib -type f -name "*.jar"`
do
    CLASSPATH=$CLASSPATH:$f
done
echo $CLASSPATH

nohup java -Xms128m -Xmx256m -Dcharset=GBK -Dfile.encoding=GBK -classpath "$CLASSPATH" com.llhy.handler.SendMsgHandler &

