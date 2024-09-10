A Gradle plugin for [Compose Multiplatform][1] projects to embed a [manifest][2] in the app exe file.

> [!WARNING]
> The plugin has not been approved by Gradle yet. Please come back a few days later!

> [!NOTE]
> The embedding only works for `create*Distributable` and `run*Distributable` tasks.  
> The plugin currently does **NOT** work for packaging tasks (like `packageExe`).  
> For `run*Distributable` tasks, start your terminal or IDE as administrator.

### Usage

```kotlin
import ir.mahozad.manifest.ManifestMode

plugins {
    // ...
    id("ir.mahozad.compose-exe-manifest") version "0.6.0"
}

composeExeManifest {
    enabled = true
    manifestMode = ManifestMode.EMBED
    manifestFile = file("app.manifest") // Located beside the build file
}
```

### Example app.manifest to make exe run as administrator

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<assembly xmlns="urn:schemas-microsoft-com:asm.v1" manifestVersion="1.0"> 
  <assemblyIdentity version="1.0.0.0"
     processorArchitecture="X86"
     name="MyApp"
     type="win32"/> 
  <description>Description of my application</description>
  <trustInfo xmlns="urn:schemas-microsoft-com:asm.v2">
    <security>
      <requestedPrivileges>
        <requestedExecutionLevel
          level="requireAdministrator"
          uiAccess="false"/>
        </requestedPrivileges>
       </security>
  </trustInfo>
</assembly>
```

[1]: https://github.com/jetbrains/compose-multiplatform
[2]: https://learn.microsoft.com/en-us/windows/win32/sbscs/application-manifests
