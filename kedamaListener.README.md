# kedamaListener
a listener to record the changes of player number in The Minecraft Server

### 2018/04/05
ping 方法完成
消耗流量过多（8kB/次），拟不采用

### 2018/04/16
基本可以运行
未实现SSL

### 2018/04/17
实现简单的单向认证SSL，使用jre/lib/security/cacerts 作为trustkeystore

### 2018/04/18
使用java.time 管理时间日期（含时区）；好像有点慢；

### & 2018/04/19 00:00 
加入 slf4j & logback ；使用logback按天记录PlayerCount信息
@vesrion=0.0.1-SNAPSHOT-7 基本稳定的版本

### 2018/04/20
改善了命令回复
json记录增加 continuous项判断是否连续
#### 准备上线！

### 2018/04/22 -- 2018/04/24
增添了数据查询服务器 


### 2018/04/25
增添了IRC服务器不响应的自动重连功能，改变启动模式

### 2018/05/02
改善了自动重连功能

###2018/05/06
兼容jsonp格式请求

>`https://0.0.0.0:28443`    服务器端口
>
>`/kedamaListener/PlayerCountRecord`    目录
>
>`?`    查询模式
>
>1.`start=&end=&jsoncallback=` 
>
>API:查询记录数据
>
>参数1:记录开始时间,%d{yyyy-MM-dd},默认为1970-01-01
>
>参数2:记录结束时间,%d{yyyy-MM-dd},默认为当天    
>
>参数3:jsonp回掉函数；可不填
>
>返回: json记录对象数组 [\${"timestamp":\$timestamp, "time":\$time%ISO_OFFSETDATETIME, "onlineNum":\$onlinePlayerNumber, "online":[\$playerName,...],"continuous":\$ifContinuous},...]
>
>2.`list=list&jsoncallback=`
>
>API:查询记录列表
>
>参数1:list
>
>参数2:返回值的名字，默认为空
>
>返回: json记录对象数组 [{"time":\$time,"file":\$file},...]
>
>3.`check=now&jsoncallback=`
>
>API:查询当前在线
>
>参数1:now
>
>参数2:返回值的名字，默认为空
>
>返回: json记录对象 \${"timestamp":\$timestamp, "time":\$time%ISO_OFFSETDATETIME, "onlineNum":\$onlinePlayerNumber, "online":[\$playerName,...],"continuous":true} 
>
>Example:  获取2018年4月20日到27日的数据，存储为json数组，数组名`data`，回掉函数为`callback`
>
>`https://118.25.6.33:28443/kedamaListener/PlayerCountRecord?start=2018-04-20&end=2018-04-27&jsoncallback=callback`
>
>返回
>`callback([{...},{...},{...},...])`
>

### 2018/05/09
初步分离 ListenerClient模块 和 DataServer模块 
粗制数据分析网页

### 2018/05/14
准备更新加入数据库
稳定版到此为止

## 2018/05/14
kedamaListener 大致停工
工作转移到 kedamaOnlineRecorder 重构（不知道什么时候开始）

>后记
>
>​	第一次较为完整地写一个网络-数据相关的应用。现在往回看，当时走了不少弯路；代码中也有很多不合理的地方。但是成就感还是有的。
>​	至少，在这疲于奔命、流言蜚语充斥的生活中，这个项目给我带来了一丝乐趣，一丝安慰。也希望，这个项目能给kedama 做出一丝贡献吧。
>
>
>

