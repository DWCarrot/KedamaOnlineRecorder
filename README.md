# KedamaOnlineRecorder
A Recorder to record the changes of player number in 'The Minecraft Server', with database;inherit prj [kedamaListener]( https://github.com/DWCarrot/kedamaListener)





## 2018/10/23 停止说明
考虑到运行KedamaOnlineRecorder的腾讯云服务器即将到期，因性能问题新服务器将无法运行此记录器；同时群组内已经有新的记录方法[jsw-服务器分时人数统计表](https://kedama-stats.jsw3286.eu.org/d/rUAiZaxmk/fu-wu-qi-zhuang-tai?orgId=1&refresh=15m)
该项目于2018/10/23 停止更新；~~同时2018/11/17服务器关闭~~延长至2019/02/08，最后一次数据收集上传；

最后状态：client运行3个月，仍然有不稳定性，IRCbot重启经常失败；server未完成;

希望已经记录的数据能为kedama群组做出一些微小的贡献吧~



## 开发日志

### 2018/7/14
- 各个组件编写完成；
- 总体框架确定：仅实现数据获取部分，数据通过sqlite（包括IN-MEMORY模式）交互
- 1.13正式版7月18号就要发布了……要来不及了= =


### 2018/7/18
- 取消使用IN-MEMORY模式，改变表结构
- 完成大部分功能；TODO：日志
- 拟尝试用统一的Timer实现调度


### 2018/7/19
- 不完全使用ping来修正信息；考虑到返回玩家列表不全……（真的是”sample“啊= =）
- 使用java.util.ScheduleService管理各个部分线程
- 第一个可以运行的版本
- TODO：irc响应--手动修正


### 2018/7/21
- 再次修正ping修正的算法（证明了一个早上= =）
- 上线测试进行中
- TODO：模块化


### 2018/7/23
- 修复一些小bug
- 模块化初步完成
  ![](UML.png)
- 修改database储存方式：运行表('online_count','online_record') ==[定时分离插入]==> 记录表 ('online_count_static','online_record_static')


### 2018/7/27
- 修复另一些小bug
- client正式上线！好像跑得挺稳的，大概吧（2018/7/28 09:00)
- TODO：server数据接口

### 2018/7/29
- 修复一个uuid更新的bug，运行重启更新

### 2018/7/30
- 修复uuid更新的bug（修了3次了2333）

- 修复split table的去重的ClassCastException问题

- 简单整理了一下代码

- 更新maven-groupId说明（昨天的已经删除）

  > 关于旧KPCG解散以及新KAMPA联邦成立的通知
  >
  > 根据现在种种原因，旧KPCG（毛线世界整肃清团）于2018年7月29日宣布解散，取而代之的是不再行使任何权力与义务的新KAMPA，我们感谢每一个玩家对毛线做过的贡献，也哀悼每一个因为捍卫自己理念而离开毛线的人，我们会永远记住，我们曾经所做所为，已经带来了什么结果，也要清楚我们在毛线已经成为什么，愿意继续留在这的各位，我由衷感谢你们对组织的忠诚，同时选择离开的各位，我也绝对不会忘记你们
  >
  > 愿世界没有仇恨
  >
  > 最后，祝各位好运
  >
  > KPCG最高负责人Old_Tom 2018年7月29日宣

  *声明：本项目自开始以来仅仅是使用kpcg作为maven package的groupId，并没有得到KPCG的明确承认；因此本项目与KPCG、KAMPA均无关*

- <div></div>


