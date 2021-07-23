在独立Module中使用自定义插件

注独立Module中定义自定义插件，下面步骤不可乱，需要按照顺序一一进行。

在buildSrc中创建自定义Gradle插件只能在当前项目中使用，因此，对于具有普遍性的插件来说，通常是建立一个独立的Module来创建自定义Gradle插件。
1. 创建Android Library Module
首先，在主项目的工程中，创建一个普通的Android Library Module，并删除其默认创建的目录，修改为Gradle插件所需要的目录，即在buildSrc目录中的所有目录，如图所示：

如上图所示，创建的文件与在buildSrc目录中创建的文件都是一模一样的，只是这里在一个自定义的Module中创建插件而不是在默认的buildSrc目录中创建。
2. 配置当前Module中build.gradle脚本
因为是通过自定义Module来创建插件的，因此，不能让Gradle来自动完成插件的加载，需要手动进行部署，所以，需要在插件的build.gradle脚本中增加Maven的配置，脚本如下所示：

```
plugins {
    id 'kotlin'
    id 'maven'
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly gradleApi()
    compileOnly localGroovy()
    implementation "org.ow2.asm:asm:9.1"
    implementation "org.ow2.asm:asm-commons:9.1"
    implementation "com.android.tools.build:gradle:4.2.1"
    implementation "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version"
}

uploadArchives {
    repositories.mavenDeployer {
        repository(url: uri('../repo')) // 仓库的路径，此处是项目根目录下的 repo 的文件夹
        pom.groupId = 'com.debug.plugin'  // groupId ，自行定义，一般是包名
        pom.artifactId = 'plugin' // artifactId ，自行定义
        pom.version = '1.0.0' // version 版本号
    }
}
```

3. 发布插件
相比buildSrc中的build.gradle脚本，这里增加了Maven的支持和uploadArchives这样一个Task，这个Task的作用就是将该Module部署到本地的repo目录下。在终端中执行 ./gradlew uploadArchives 指令，将插件部署到repo目录下，如图所示：

当插件部署到本地后，就可以在主项目中引用插件了。
当插件正式发布后，可以把插件像其它module一样发布到中央库，这样就可以像使用中央库的库项目一样来使用插件了。
4. 配置发布的插件
将刚刚发布到repo目录下的插件，配置到rootProject的build.gradle脚本中，如图：

5. 引用自定义插件


注独立Module中定义自定义插件，上述步骤不可乱，需要按照顺序一一进行。