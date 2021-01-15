![](img/title.png)



It is applicable to solve the communication problem between parallel modules in Android application modular development.

![](https://img.shields.io/github/license/gittosuperfly/AndroidModuleBridge)  ![Bintray](https://img.shields.io/bintray/v/broccoli/bridge/bridge-core?label=bridge-core)   ![Bintray](https://img.shields.io/bintray/v/broccoli/bridge/bridge-plugin?label=bridge-plugin)



## Is What

![](img/Image1.png)

For a common component-based Android application, the communication capability between components at the same level is provided.For example, to communicate between the Music and Record components in the diagram above without making them dependent on each other.



## How to join

**Step 1.** Add the jcenter repository to your build file

```groovy
buildscript {
    repositories {
        ...
        jcenter()
    }
}

allprojects {
    repositories {
        ...
        jcenter()
    }
}
```



**Step 2.** Add the plugin dependency to project

```groovy
buildscript {
    dependencies {
        ...
        classpath "com.cai.open:bridge-plugin:version"
    }
}
```



**Step 3.** Add the dependency to app module

```groovy
dependencies {
    ...
    implementation 'com.cai.open:bridge-core:version'
}
```



**Step 4.** Use plugin to app module

```groovy
apply plugin: 'com.cai.open.bridge-plugin'
```



## How to use

**Step 1.** Create an abstract class that inherits from the `BaseBridge` in the common module

```kotlin
abstract class MusicBridge : BaseBridge() {
    abstract fun playMusic(music: Music)
    abstract fun showActivity(context: Context)
    ...
}
```



**Step 2.** Implement the abstract class in the music module and add annotations `@Bridge`

```kotlin
@Bridge
class MusicBridgeImpl : MusicBridge() {
    override fun playMusic(music: Music) {
        MusicPlayer.play(music)
    }
    
    override fun showActivity(context: Context) {
    	// realize it...
    }
}
```



**Step 3.** Use in other modules

```kotlin
playMusicBtn.setOnClickListener {
    ModuleBridge.get(MusicBridge::class.java).playMusic(popMusic)
}
```



**After compilation, the bridge table will be generated in the app module.**



## Other

### Extension

In the app module, `bridge{}` is used to add extensions. The contents that can be added and their default values are as follows:

```
boolean stopOnError = true
String outputFile = "bridge_table.json"
String onlyScanRegex = ""
String skipScanRegex = ""
```

for example:

```groovy
android {
  ...
}

bridge {
  outputFile "test_bridge.json"
}
```

### Mock

Mock is used in the process of single module development. First, it is tested in this module.

When mock and other implementation modules exist at the same time, the mock implementation shall prevail.

```kotlin
@Bridge(isMock = true)
class MusicBridgeMockImpl : MusicBridge() {

    override fun playMusic(music: Music){
        Log.d("MusicMock", "$music")
    }
    
    override fun showActivity(context: Context) {
    	Log.d("MusicMock", "mock startActivity...")
    }
    
}
```



