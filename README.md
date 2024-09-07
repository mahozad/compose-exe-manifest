A Gradle plugin for [Compose Multiplatform][1] projects to embed a [manifest][2] in the app exe file.

> [!NOTE]  
> The task currently does NOT work for packaging tasks (like `packageExe`).

### Usage

```kotlin
plugins {
    // ...
    id("ir.mahozad.compose-exe-manifest") version "0.1.0"
}

composeExeManifest {
    enabled = true
    manifestFile = file("app.manifest") // Located beside the build file
    copyManifestToExeDirectory = false
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
