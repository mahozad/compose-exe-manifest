Example Gradle plugin: https://github.com/JetBrains/compose-multiplatform/tree/master/gradle-plugins

To publish the plugin to Gradle plugin portal, run `:plugin:publishPlugins` task.

App manifest can either be placed beside the app exe or
can be embedded in the app exe with [mt.exe][1].

To [install/acquire mt.exe][2], do the following:
  - Download "Windows SDK" installer  
    https://developer.microsoft.com/en-us/windows/downloads/windows-sdk/
  - Run the installer and in the select features step, choose *Windows SDK for Desktop C++ x86 Apps*
  - Copy the *mt.exe* file from  
    `C:\Program Files (x86)\Windows Kits\10\bin\10.0.26100.0\x64`  
    `C:\Program Files (x86)\Windows Kits\10\bin\10.0.26100.0\x86`

To [embed a manifest file in the exe][3], execute *mt.exe* like this:

```shell
./mt.exe -nologo -manifest "example.manifest" -outputresource:"app.exe;#1"
```

Related Compose Multiplatform issues:
  - https://github.com/JetBrains/compose-multiplatform/issues/4251
  - https://github.com/JetBrains/compose-multiplatform/issues/2625

Related StackOverflow posts:
  - https://stackoverflow.com/q/1385866
  - https://stackoverflow.com/q/11705047
  - https://stackoverflow.com/q/61200747
  - https://stackoverflow.com/q/78466325
  - https://stackoverflow.com/q/39493502
  - https://stackoverflow.com/q/64258735

Related discussions:
  - https://discuss.gradle.org/t/how-to-create-a-task-with-outputs-that-arent-known-during-configuration-phase/38439/4
  - https://discuss.gradle.org/t/using-a-dynamic-collection-of-files-as-a-task-output/27339


[1]: https://learn.microsoft.com/en-us/windows/win32/sbscs/mt-exe
[2]: https://stackoverflow.com/q/54462568
[3]: https://github.com/bmatzelle/gow/pull/157
