### 用来统计某个目录下的java文件的单词数量插件

1. 首先打包进本地仓库
   `mvn clean install`
2. 使用 xml 配置使用

```xml

<plugin>
    <groupId>work.iruby</groupId>
    <artifactId>wordcount-maven-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
    <configuration>
        <basedir>src/main/java</basedir>
        <outputFile>target/wordcount.txt</outputFile>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>wordcount</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```
`mvn clean test`
3. 也可以使用命令行配置

```xml

<plugin>
    <groupId>work.iruby</groupId>
    <artifactId>wordcount-maven-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
</plugin>
```
`mvn clean work.iruby:wordcount-maven-plugin:wordcount "-Dbasedir=src/main/java -DoutputFile=wordcount.txt"`
4. basedir 将会扫描这个目录下所有的`java`文件, 默认值为 `${project.basedir}`
   outputFile 的默认值为 `wordcount.txt`
   
___
其他: maven插件的单元测试好屑啊(指官网的代码不全), 而且版本检测也好屑啊(不同版本就用不了你敢信), 而且idea调试能过跑起来就各种error, 
   算是小节一下(明明靠打包方式测试早整完了),目前还有一点小问题(单元测试时使用 test.xml 不配置参数将不会拿到默认值(明明实际使用是生效的), ohNo, 溜了)

