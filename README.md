#基于微博文本分析的用户画像分析
##目的
由于本次作业数据的局限性，能分析的用户只有时间，微博内容。通过对文本分析的给用户打上标签。

##环境
###数据库
mongodb 3.2.x
下载安装:https://docs.mongodb.com/master/installation/
>
1.如果已经安装，可用mongod -version查看版本
2.想用gui的话可用robomongo，地址:https://robomongo.org/
3.mongodb数据导入导出教程:http://chenzhou123520.iteye.com/blog/1641319


### 开发环境
- 语言： java 1.8
- IDE： Inteilij IDEA 2016.01
- 依赖库：
   - 自然语言处理工具-HanNLP:https://github.com/hankcs/HanLP
   - mongodb driver:https://docs.mongodb.com/ecosystem/drivers/java/
   - SemanticAnalysis http://git.oschina.net/www.feit.com/SemanticAnalysis


##文件说明
- 处理代码 流程中的每一步都是单独一个可运行的模块
  - 预处理 UserMerger/*
  - 分词  WordDivision/*
  - 情感分析 EmotionAnalysis/*
  - 兴趣分析 InterestAnalysis/*
  - 时间分析 TimeAnalysis/*
  - 角色分析 RoleAnalysis/*
- 处理结果 dat/*
  - 预处理结果 weibo_active.json (比较大，见百度云 http://pan.baidu.com/s/1miCORJ6 密码: gzva)
  - 分词结果 weibo_keyword.json
  - 情感分析结果 weibo_emotion.json, (通过排序法的情感分析：weibo_emotion_sort.json)
  - 兴趣分析结果 weibo_interest.json
  - 时间分析结果 weibo_time.json
  - 角色分析结果 weibo_role.json



##流程
### 预处理
####简单预处理
**对应模块UserMerger**
1. 原数据csv格式
源文件 data_new.csv， 数据量60W

2. 提取有效属性(id,content,time,user),存入mongodb，之后一条微博信息如下格式如下
  ```json
{
    "id" : 600357,
    "content" : "#上海微整形#你若许我不离不弃，@男神整形医生铭昊 ，我定许你地久天长。花绽流年，芭比眼，鼻综合，自体脂肪填[微笑]充，情事摇曳，今生我愿为你舒展广袖，假体隆胸隆鼻留幽香于袖底，在楼榭上，水岸边，清幽处，倾尽舞步，舞尽岁月娉婷，舞尽流年芳菲。",
    "time" : "2016/3/16 17:45",
    "user" : "qetina"
}
  ```

3. 坏数据清洗
删除无名数据，如现在数据内部有2567条用户名是"#NAME？"，也就是无名用户的删除

4. 相同用户整合
统计出用户的微博集和活动时间集，样例如下
  ```json
{
    "user" : "niglBBy",
    "contents" : [
        "胡歌上海旅游形象大使。四季上海，天天精彩。 .....",
        "谁粉我我立马粉水！ 【原微博】 @Alina童颜-微整形 不要为明天忧虑，因为明天自有明天的忧虑；一天的难处担当就够了。而我们大人整天都强迫孩子去做一些事情。#上海微整形#却从来没有问过他们快乐不快乐，原来真正蒙在鼓里的是我们",
        "来自星星的微博[心] 【原微博】 @Alina童颜-微整形 不要为明天忧虑，因为明天自有明天的忧虑；一天的难处担当就够了。而我们大人整天都强迫孩子去做一些事情。#上海微整形#却从来没有问过他们快乐不快乐，原来真正蒙在鼓里的是我们",
        "胡歌上海旅游形象大使。四季上海，天天精彩。 【原微博】 #晒春游逛上海#樱花、桃花、郁金香、油菜花，看不尽的花；公园、马路、古镇，逛不完的景；青团、烧卖、春笋、刀鱼面，吃不完的美食……还不跟我一起去春游？@乐游上海"
    ],
    "times" : [
        "2016/3/22 10:55",
        "2016/3/22 10:53",
        "2016/3/22 10:52",
        "2016/3/22 10:52"
    ],
    "count" : 4
}
  ```

5. 活跃用户筛选
因为是做用户画像，那些发表数量少的用户无法准确添加标签，所以筛选了在该数据时间段内的发表微博数在10以上的用户，从204401名用户中筛选了共9950名用户。
到这一步完成的数据在百度云链接: http://pan.baidu.com/s/1miCORJ6 密码: gzva
**一共9950条数据，如果出现数据爆内存了（自动断了链接），请在import语句最后加上 --batchSize 1,就能导入成功了**

6. 文本分词

分词主要是将微博中的一些我们需要的重要信息提取出来，主要包括关键词、话题、评论，分为以下步骤

1.首先在一个用户所发的所有微博中，利用字符串分割方法将原微博的数据拿出来，运用自然语言处理工具-HanNLP中的关键词提取方法提取出关键字。
2.由于我们只关心名词类的话题词语，所以对提取出来的关键字进行词性筛选，将名词和名词向的词保留下来作为关键字。
3.对于每条微博所拿到的关键词，进行总体的频数统计，利用2-8原则，将大于总数的20%的关键词拿出来作为这个人所关心的话题。
4.由于微博有自带话题的"#"的关键字，通过字符串操作提取出微博上的话题。
5.提取用户所评论的内容，将转发部分和原微博减掉，得到单纯的评论部分

分词结果样例如下：


```json
{
    "user" : "----柠檬皮----",
    "keyword" : [
        "上海",
        "人员",
        "微博",
        "兴迷"
    ],
    "topic" : [
        "张艺兴",
        "张艺兴1007生日快乐"
    ],
    "comment" : [
        "UPS图",
        "#张艺兴#",
        "买了",
        "1",
        "又可以",
        "。",
        "大叔比较",
        "你们",
        "中",
        "兔纸兴",
        "算了算了",
        "估计快了"
    ]
}
```

####文本分词
提取微博内容中的名词和实体，然后形容词
如果可以的话，要得到每个实体的比重

### 基于分析的处理


### 分析维度

根据群体性结果，制定标签，然后再分配的过程。应对需求。

#### 自然属性
目前从数据上来看，只有时间和发的微博内容，无法分析

#### 性格分析
性格分析任务是通过用户评论的词语来分析出用户情感上的倾向，由于所用到库的限制，目前主要工作是分析用户评论中，正向、中性、负向的情感。

- 标签定义：正向积极、中性、负向消极、喜怒无常

- 工具：SemanticAnalysis情感分析库

分析方法：
- 简单预处理：由于评论数量过少会不太容易分析并且也会影响分析结果，所以筛选出评论超过10条的用户，大约530的用户。
- 拿到每一条评论（在分词中已经做过）
- 基于频数的分析：

    1.调用情感分析库对用户的每条评论进行正向、中性、负向处理，并统计三个方向上的频数。
    2.设置阈值为0.2，将正向评论超过20%的用户标定为正向积极；负向评论超过20%的用户定义为负向消极；而对正负向评论都满足阈值的用户定义为息怒无常；其余为中性。
    3.分析结果样例如下：
```json
{
    "user" : "Aqur-G_M_Y-rius",
    "comment" : [
        "全国一张网 ",
        "关注两会[好棒] ",
        "好评[羞嗒嗒] ",
        "[好棒]体察民情 ",
        "关注民生 ",
        "又是两会[鼓掌] ",
        "网购维权难[悲伤] ",
        "大家注意啦 ",
        "丰富 ",
        "好方法 ",
        "老年人是弱势群体 更应值得重视[好棒] ",
        "敬业福 ",
        "[鼓掌]棒！ ",
        "看岗敬业 向海湾所看齐 "
    ],
    "emotioncount" : [
        0,
        11,
        3
    ],
    "emotionrate" : [
        0.0,
        0.785714268684387,
        0.214285716414452
    ],
    "emotion" : "正向积极"
}
```
```json
{
    "user" : "Kobe_上海科蜜群",
    "comment" : [
        "主页有个考试...看不完了[悲伤][悲伤][悲伤]",
        "首发回来。。。又落后了17分。。。都追到九分了啊[doge][doge][doge]",
        "半场比赛结束...41-57落后16分...进攻打不进，防守防不住...[doge][doge][doge][doge][doge][doge]",
        "不，一般总裁文都是底下一堆男模，总裁最帅！[哈哈]蜗壳是boss ",
        "这段时间打得还可以...继续填坑[doge]",
        "助攻10-2...比分32-15，嗯，湖人替补拿了11分[doge][doge][doge]",
        "开场习惯的挖坑模式[doge]",
        "拉塞尔开场一抢断[doge]",
        "帅",
        "#上海科蜜群#生命不是活给别人看的，生命就是一朵花，静静地开，悄悄地落。认识你自己#早安，沪Lakers#",
        "打了一波8-0？excuse me？[doge][doge][doge]还差12分",
        "#上海科蜜群# 看到一张傲娇脸?你有收藏这样的嘛？送几个图给主页呗[爱你]",
        "#上海科蜜群#很喜欢这三句话：知人不必言尽，言尽则无友。责人不必苛尽，苛尽则众远。敬人不必卑尽，卑尽则少骨。#早安，沪Lakers#",
        "看到一张傲娇脸?你有收藏这样的嘛？送几个图给主页呗[爱你]"
    ],
    "emotioncount" : [
        6,
        3,
        5
    ],
    "emotionrate" : [
        0.375,
        0.3125,
        0.3125
    ],
    "emotion" : "喜怒无常"
}
```
- 基于排序的分析方法

    该方法主要考虑到整体对个体的影响，例如在一个正向、积极的氛围中，负面情绪所占比例会很小，很难被识别出来，为了分析出这一部分的用户，我们用基于排序的方法。

    1.调用情感分析库对用户的每条评论进行正向、中性、负向处理，并统计三个方向上的频数。
    2.对所有用户的正向、负向进行降序排序
    3.选取阈值20%，将所有用户中正向情感频数排在前20%的用户定义为正向积极；所有用户中负向情感频数排在前20%的用户定义为负向消极；将两者都达到的用户定义为喜怒无常；其余为中性。

    - 改进：

        从统计出来的结果来看，人们大部分为都为积极评论，所以积极频数在前20%的用户基本都为全积极评论，相对来说消极评论较少，所以消极频数在前20%的频数则低达0.3左右，于是会出现如下情况：0.7的正向评论，0.3的负向评论用户会被定义为负向消极用户（由于正向指数达不到前20%，而负向指数达到了前20%）

        所以我们将正面的阈值设为50%，而负面指数设置为20%。

    分析结果样例如下：
```json
{
    "user" : "朕的皇后又去浪了",
    "comment" : [
        "要是你这时候上线看到我的转发多好啊跟看神经病似的 ",
        "没关系 反正你也不理解我 ",
        "没挂的什么鬼东西看三集就扔 ",
        "要看的都他妈挂掉了 ",
        "特么的打榜打到这个点电视剧也没看你赔我 ",
        "不行不想唱了 ",
        "艹不知道说什么 ",
        "妈的还不到1007w ",
        "反正首页都睡了[偷笑] ",
        "你会不会觉得我们不理解你 ",
        "[微笑] ",
        "死亡可怕的地方在于没有意义 ",
        "差不多了洗洗睡吧 ",
        "说故事的人唱歌的人最怕自己的感情被呼应错误shimā ",
        "最近没有电视看好烦 ",
        "[心][心][心] ",
        "[心][心][心] ",
        "照这个趋势[泪流满面][泪流满面][泪流满面][泪流满面][泪流满面] "
    ],
    "emotioncount" : [
        12,
        6,
        0
    ],
    "emotionrate" : [
        0.666666686534882,
        0.333333343267441,
        0.0
    ],
    "emotion" : "负面消极"
}
```


#### 兴趣/话题分析
#####根据
原微博的分词形成的关键词和自带话题。
根据原微博的关键词，利用语义近似度（尝试了很多语料库，最后采用《同义词词林扩展版》），
试验了两种方法，根据原有主题生成单词云
#####尝试
先尝试利用词向量与语义距离寻找话题，就是根据名词和几个预设领域名词的近似度去匹配（就是把一个词用几个词向量来表示），找寻和一篇文章中大部分关键词距离之和最近的那个领域。然后我也试了几个主流的中文自然语言领域（比如波森nlp
和腾讯文智，复旦那个nlp）的分析处理，发现他们的这套算法基本也是非常不准的，这是一个理论上分析话题的很好的方法，但是实际效果十分受限于语料关系。比如我们现在采用的比较通用的语料是《同义词词林扩展版》，对人名、书名的语料信息十分不全。
#####方法
预设主题的方法失效了，最后我们采用对关键词和微博自带话题做名词实体的提取，根据不同的名词类型（机构、人名、地名、普通名词等）分类，找到每个名词类型下占有极大权重的名词，小权重就直接忽略。比如对于微博话题“上海微整形”和“上海北京西安榆林南昌长沙宝鸡咸阳微整形”的结果分别是【上海，微整形】和【微整形】。
处理之后两个例子如下：
```
{
    "user" : "-希帕蒂娅-",
    "interest" : [ "微整形", "王思聪", "美容", "整形" ]
}
{
    "user":"wj悲凉仰妇胃幸",
    "interest":["颜控","吃货","田子坊","胡歌","花海","旅游","上海"]
}
```



#### 时间作息分析
时间做了两个模块的分析，第一模块主要是通过时间分析对用户进行相关的特征分析，第二模块则是采用kmeans算法对每个用户的微博时间进行聚类，获取相对频繁发微博的时间。

第一模块：

   1. 对时间按小时进行分组，结果如下


        0 : 20662       1 : 19777         2 : 16682
        3 : 11050       4 : 12078         5 : 11803
        6 : 12341       7 : 12493         8 : 10045
        9 : 16163       10 : 23814        11 : 17565
        12 : 13026      13 : 12228        14 : 13172
        15 : 13465      16 : 11409        17 : 11907
        18 : 5346       19 : 6936         20 : 16169
        21 : 14762      22 : 12804        23 : 18198




   出乎意料的是3-6竟然那么多微博，大家都不睡觉吗？所以觉得可能是值班无聊或失眠吧。

   2. 根据这个统计结果制定相应时间的相应tag
   ps：脑洞实在是开不出来
    23,0,1,2:           "夜猫子"
    3,4,5,6:            "经常值夜班或失眠"
    7,8:                "一般早起喜欢早上刷博"
    9,10,14,15,16,17:   "上班空闲"
    11,12,13:           "习惯午餐或午休刷手机"
    18,19:              "晚餐悠闲"
    20,21,22:           "晚间空闲"

   3. 对个体进行时间分析，统计制定的时间区间对应的频数

   4. 取至多三个频数最高的相应属性
   ```json
   {
    "_id" : ObjectId("575560fae799915351afa91a"),
    "user" : "-宝---宝-",
    "timeTags" : [
        "经常值夜班或失眠",
        "夜猫子",
        "上班空闲"
    ]
}
```
第二模块：

对每个用户进行kmeans聚类分析(K取4)，得到K个簇和K个中心点。K个中心点关于相应的簇的时间个数呈降序排列，则非常清楚地可以看出用户主要活跃的时间
```json
{
    "_id" : ObjectId("575560fbe799915351afcfe4"),
    "user" : "---麗--",
    "Cluster" : [
        [
            "15:04",
            "14:46",
            "14:51",
            "14:14",
            "14:00",
            "13:30"
        ],
        [
            "21:29",
            "21:16",
            "21:11",
            "20:55",
            "20:29"
        ],
        [
            "17:04",
            "19:18",
            "19:14",
            "19:04",
            "16:49"
        ],
        [
            "12:09",
            "09:35",
            "09:21",
            "12:00",
            "08:01"
        ]
    ],
    "Average" : [
        "14:24",
        "18:17",
        "10:13",
        "21:04"
    ]
}
```

#### 角色分析
主要考虑到微博上存在较多的转发，所以在角色方面，分析了用户的创作属性，即纯转发，加工转发，原创三种角色。

这个分析比较简单，在微博文本分析中简单地将“转发微博”，“//”，“【原微博】”这些关键词出现在句首的认为是单纯转发，存在这些关键词但非句首，则代表转发中表达了自己的意见，即加工转发，余下的则是原创。

当然并非每个用户存在符合上述条件就获取相对特征，我们采用8-2原则，将群体的前20%看做是对应特征的获取对象。

产生两个json文件记录分析结果：

1. 各种类型微博的数量
```json
{
    "_id" : ObjectId("57556ee2e7999157385ddf4d"),
    "user" : "----柠檬皮----",
    "pureRepost" : 4,
    "modiRepost" : 12,
    "origin" : 0
}
```
2. 分析得到的tag（一般只有一个，说明这个转发习惯可以反应一定的人物性格）
```json
{
    "_id" : ObjectId("57556ee3e7999157385e062b"),
    "user" : "----柠檬皮----",
    "roleTags" : [
        "加工转发"
    ]
}
```




### 准确度

### 群体性行为和个体性行为差异

### 应用
因为这次数据大多数微博的内容十分单一，在话题分析的出结果是不有趣的，并不具有十分大的利用价值。

## 参考链接
如何构建用户画像- 概述 - http://www.woshipm.com/pmd/107919.html
新手如何开始用户画像分析 - https://www.zhihu.com/question/29468464
可视化工具推荐 - https://www.zhihu.com/question/31429786
word2vec - http://wei-li.cnblogs.com/p/word2vec.html
