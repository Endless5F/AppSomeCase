## 第三方参考资料：
* KMM 官网：Getting started | Kotlin (kotlinlang.org)
* 官方推荐示例项目：Samples | Kotlin (kotlinlang.org)
* KaMPKit：GitHub - touchlab/KaMPKit: KaMP Kit by Touchlab is a collection of code and tools designed to get your mobile team started quickly with Kotlin Multiplatform.

## 注意事项：

### ios相关
1. 多线程数据共享问题
    * 跨线程对象必须 freeze，frozen 对象必须不可以改变赋值（包括子属性），需要改变赋值的，需要在切到目标线程后进行 deepCopy
    * 跨线程共享常量、全局不可变属性，需要添加  @SharedImmutable 标注
    * 跨线程全局状态标识属性，需要合理使用  AtomicReference 类、 AtomicInteger、 AtomicBoolean 等
    * MMKV 初始化（initialize 方法）需要放在主线程执行，否则会造成 Debug 包卡顿

2. 数据实体字段可空类型 & 默认值
    * 若 data class 中的字段为 val，不设置默认值，则在 Framework 中不会添加  __nullable 标注，Swift 中此字段必传，反之则可选

### android相关
1. 包名引用错误问题
    * 由于在使用 Android Studio 开发时，会自动添加所使用类的 package，在 Common 模块中使用部分 JVM 平台特定类（常见的有： @Transient 、 TextUtils）可能导致 import 错误，此时需要注意文件头部使用的是不区分平台的类，而不是以  kotlin.jvm 或  android 开头的类

### 包体积相关
1. Model 解析，不要使用 @Serialization 形式自动解析，影响包体积，需要使用JsonUtil(Kotlinx.Serialization)手动解析