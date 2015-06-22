# S-Pi-Backend
Backend component for the S-Store MIMIC ICU Monitoring Demo

# Install

1. Clone the repository
2. Edit your ~/.m2/settings.xml file and add
```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <profiles>
    <profile>
      <id>allow-snapshots</id>
      <activation><activeByDefault>true</activeByDefault></activation>
      <repositories>
        <repository>
          <id>snapshots-repo</id>
          <url>https://oss.sonatype.org/content/repositories/snapshots</url>
          <releases><enabled>false</enabled></releases>
          <snapshots><enabled>true</enabled></snapshots>
        </repository>
      </repositories>
    </profile>
  </profiles>
</settings>
```

3. Use whatever IDE you have to import the maven project.

4. Run the Runner main method in your IDE to start the server.

5. (optional) Run mvn package in the main directory. It should output a s-pi-xxxxx.jar in target/. You can run that with java -jar filename.jar.
