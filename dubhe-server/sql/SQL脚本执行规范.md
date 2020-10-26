# SQL脚本执行规范



## 1 脚本名称概念

###  初始化脚本

> * 00-Dubhe-DB.sql ：数据库创建脚本
> * 01-Dubhe-DDL.sql ： 数据操纵语言DML脚本
> * 02-Dubhe-DML.sql：数据定义语言DDL脚本

> 注：数据初始化时使用

### 热更脚本

> * 09-Dubhe-Patch.sql：热更脚本

> 注：新增数据时，需同步DDL/DML脚本

## 2 执行顺序

> 脚本初始化时执行： 00-Dubhe-DB.sql -> 01-Dubhe-DDL.sql -> 02-Dubhe-DML.sql

## 3 热更脚本

> * 热更脚本中 字段新增、修改、数据新增时需要备注 更新人与 更新时间
> * 热更脚本中 新增数据后， 需与 01-Dubhe-DDL.sql 和 02-Dubhe-DML.sql 数据同步

## 4 版本

### 当前版本

> 当前版本： v1

### 版本升级

> 版本升级时在原数据的基础上执行新脚本

## 5 SQL脚本存储路径

> ./dubhe-server/sql/v1

