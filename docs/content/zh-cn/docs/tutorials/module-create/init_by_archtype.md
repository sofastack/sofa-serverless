---
title: 使用 maven archtype 脚手架自动生成
weight: 100
---
### 自定义 `archetype` 骨架

在 `sofa-serverless-runtime\sofa-serverless-biz-maven-archetype` 目录下，修改或者增加现在的代码，以复合您的需求。


### 创建 `archetype` 骨架

在 `sofa-serverless-runtime\sofa-serverless-biz-maven-archetype` 目录下使用 `mvn clean archetype:create-from-project` 命令，会生成 `target` 目 `target` 目录下会有 `generated-sources` 目录，`generated-sources/archetype/src/main.resource/META_INF.maven` 下会有一个 `archetype-metadata.xml` 文件，这里是可以配置那些资源会被包含在骨架中，那些不会包含在骨架中。


### 将 `archetype` 骨架安装到本地

进入 `target/generated-sources/archetype` 目录下，执行指令：

```shell
cd target/generated-sources/archetype/
mvn clean install

```

运行成功后可以在 info 指示的目录下找到骨架的 jar 包。

![install2local.jpg](assets/install2local.jpg?t=1700809779714)


### 在本地仓库中生成骨架坐标信息

执行指令：

```shell
mvn archetype:crawl

```

`maven` 本地仓库下就会生成一个 `archetype-metadata.xml` 文件，默认是自己 `maven` 文件夹下。

![archetypeCatalog.png](assets/archetypeCatalog.png?t=1700810028618)

打开该配置文件，里面有固件的坐标信息。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<archetype-catalog xsi:schemaLocation="http://maven.apache.org/plugins/maven-archetype-plugin/archetype-catalog/1.0.0 http://maven.apache.org/xsd/archetype-catalog-1.0.0.xsd"
    xmlns="http://maven.apache.org/plugins/maven-archetype-plugin/archetype-catalog/1.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <archetypes>
    <archetype>
      <groupId>com.alipay.sofa.serverless</groupId>
      <artifactId>sofa-serverless-biz-maven-archetype-archetype</artifactId>
      <version>0.5.0</version>
      <description>todo</description>
    </archetype>
</archetype-catalog>

```

### 根据骨架创建项目

#### 直接通过maven指令创建项目

打开需要创建项目的文件夹，在该文件夹下打开cmd或powershell，执行下面指令：

```powershell
mvn archetype:generate  -DarchetypeGroupId=<groupId>  -DarchetypeArtifactId=<artifactId>  -DarchetypeVersion=<version> -DgroupId=<groupId> -DartifactId=<artifactId> -X

```

#### 通过Idea创建项目

创建一个maven项目。

输入骨架坐标，以及对应的仓库信息（如果是远程仓库的话需要指定，本地仓库可以不指定）：

![create.png](assets/create.png?t=1700810331260)

到这里创建 `archetype` 就完成了。
