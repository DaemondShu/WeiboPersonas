#基于微博文本分析的用户画像分析
##目的
由于本次作业数据的局限性，能分析的用户只有时间，微博内容。通过对文本分析的给用户打上标签。

##环境搭建
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



##流程
### 预处理
####简单预处理
1. 原数据csv格式
文件 data_new.csv， 数据量60W，
属性
样例
是否需要删除

2. 统计同一个用户，以json形式存如mongodb，

3. 文本分词
提取微博内容中的名词和实体，然后形容词
如果可以的话，要得到每个实体的比重

### 基于分析的处理


### 分析维度

#### 自然属性
显然是无法分析

#### 性格分析
根据形容用词和性格用词的近似度去匹配。领域用词需要自己去调。

#### 兴趣/话题分析
根据名词和领域名词的近似度去匹配。与性格类似。

#### 时间作息分析
根据每个人时间段的密度

#### 角色分析
区分关注者和发布者，依靠数量统计
﻿

# 参考链接
如何构建用户画像- 概述 - http://www.woshipm.com/pmd/107919.html
新手如何开始用户画像分析 - https://www.zhihu.com/question/29468464
可视化工具推荐 - https://www.zhihu.com/question/31429786
