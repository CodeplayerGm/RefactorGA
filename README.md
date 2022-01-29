# 面向架构异味的重构搜索遗传算法的实现

## 算法选型

参考了https://github.com/onclave/NSGA-II的NSGA-II的算法实现



## 算法目标

对于一个系统的源代码数据，经关注点识别、功能原子聚类、代码文件依赖关系抽取后，该算法将实现面向

1. 微服务粒度最大化（用关注点信息描述粒度）
2. 模块化质量最大化（高内聚性，低耦合性）
3. 重构成本最小化（应用最少的步骤来调整代码文件）

三个目标的重构方案搜索算法



## 算法前置工作与数据文件

选取了待重构的系统后，需要进行关注点识别、功能原子聚类、代码文件依赖关系抽取三个前置工作。

关注点识别：基于语义和LDA对代码文件中的语义信息进行聚类，获取每个服务下的关注点信息。同时这些信息将辅助遗传算法的服务粒度目标的计算。

功能原子聚类：将服务下的代码文件基于call依赖关系，聚类成功能原子Functional atom。作为遗传算法搜索的基本单元，替代代码文件，可大幅减少搜索空间，提高搜索性能。

代码文件依赖关系抽取：一方面，辅助关注点识别和功能原子聚类的工作；另一方面，在遗传算法中，辅助模块化质量目标（内聚性、耦合性）的计算

数据文件的位置保存在项目的data目录下，文件描述如下：

```shell
model-final.theta：文件与主题的概率分布文件
concern.txt：全部的关注点id、TC值（代表关注点的功能性内聚值，以0.4的阈值进行筛选）
files.flist：待重构系统的相关代码文件名称（全路径） - 只筛选了服务目录下的代码文件
allFiles.flist：待重构系统的全部代码文件名称（全路径）- 用于计算SMQ的内聚耦合指标，包括服务外的代码文件
allCallGraph.json：allFiles里代码文件的call依赖矩阵数据
cluster.json：功能原子聚类结果，文件的id与files中的一致

```



## 项目部署和运行

### 部署到本地

```shell
# 1、克隆项目到本地
git clone https://github.com/CodeplayerGm/RefactorGA.git

# 2、使用IDEA打开项目目录，打开pom.xml文件，安装maven依赖

# 3、其他环境：JDK11，maven3.8.4
```



### 项目运行

运行入口在src/test/RefactorTest.java文件中

前半部分注释掉的是旧基因型设计下的运行配置，后半部分是当前的新基因型设计下的运行配置，直接运行main方法即可。



### 输出

1、IDE任务打印栏中会实时打印遗传算法的全过程

2、算法运行日志会被写入文件，保存在项目的output目录下，格式如：NSGA-II-report-11444.txt

3、refactorPrograms.json，objectives.json是用于绘制echarts树状图的数据文件

4、如果目标函数是两个以下，则会弹出可视化图表框，展示全部个体和第一前沿。三个及以上得到目标函数时，则不会显示



## 算法参数说明

种群规模参数：

```java
种群规模上限：100【Configuration.java】
```

变化器参数：

```java
交叉选择概率：0.6【SingleCrossoverEncode.java】
变异选择概率：0.3【ModularMutation.java】
break变异概率：0.3【DefaultPluginProvider.java】
```

适应度参数：

```java
// 关注点分数适应度
主题与文件的概率阈值：0.006【PreProcessLoadData.java】
过载分数阈值：10【PreProcessLoadData.java】
模块化数量系数：1.0【PreProcessLoadData.java】
```

停止条件参数：

```java
最大代数：200【Configuration.java】
第一前沿占比：0.8【TerminatingCriterionProvider.java】
基因型数量：5000【PreProcessLoadData.java】
```



## 代码文件说明

需要关注的代码文件如下：

```shell
├─data - 需要读取的预处理数据文件目录
│      
├─output - 算法输出遗传算法日志和可视化数据文件的目录
│      
├─src
│  ├─main
│  │  └─java
│  │      └─nsga
│  │          │  Configuration.java - 遗传算法的全局配置对象
│  │          │  NSGA2.java - NSGA的核心算法流程实现
│  │          │  PostProcessShowData.java - 用于实现可视化数据的保存
│  │          │  PreProcessLoadData.java - 读取预处理数据
│  │          │  Reporter.java - 打印遗传算法日志
│  │          │  Service.java - 一些公共的方法集合
│  │          │  
│  │          ├─crossover
│  │          │      AbstractCrossover.java - 交叉操作接口
│  │          │      SingleCrossover.java - 旧基因型的交叉操作
│  │          │      SingleCrossoverEncode.java - 新基因型的交叉操作
│  │          │      
│  │          ├─datastructure
│  │          │      AbstractAllele.java - 等位基因接口
│  │          │      Chromosome.java - 通用的染色体对象，也代表个体的概念
│  │          │      FunctionalAtom.java - 功能原子对象，包含一个文件列表
│  │          │      GroupItemAllele.java - 分组项基因，功能原子的集合【旧基因型设计使用】
│  │          │      IntegerAllele.java - 整数型等位基因【新基因型设计使用】
│  │          │      Population.java - 种群，染色体/个体的集合
│  │          │      
│  │          ├─mutation
│  │          │      AbstractMutation.java - 变异操作接口
│  │          │      ModularMutation.java - 新基因型设计的变异操作实现
│  │          │      
│  │          ├─objectivefunction
│  │          │      AbstractObjectiveFunction.java - 目标函数接口
│  │          │      CohesionObjective.java - 旧基因型的SMQ内聚目标【拆开使用】
│  │          │      ConcernNewObjective.java - 新基因型的过载分数目标
│  │          │      ConcernObjective.java - 旧基因型的过载分数目标
│  │          │      CouplingObjective.java - 旧基因型的SMQ耦合目标【拆开使用】
│  │          │      MoJoObjective.java - 新基因型的重构成本目标
│  │          │      ObjectiveProvider.java - 目标函数组合打包器
│  │          │      SMQNewObjective.java - 新基因型的SMQ目标
│  │          │      SMQObjective.java - 旧基因型的SMQ目标
│  │          │      
│  │          ├─plugin
│  │          │      DefaultPluginProvider.java - 包含了初始化种群，种群每代更新替换的逻辑
│  │          │      
│  │          └─termination
│  │                  TerminatingCriterionProvider.java - 遗传算法的终止条件
│  │                  
│  └─test
│      └─java
│              RefactorTest.java - 重构遗传算法的入口
│        
```











