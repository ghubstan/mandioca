# How to upgrade the Gradle version

Visit the [Gradle website](https://gradle.org/releases) and decide the:

 - desired version
 - desired distribution type
 - what is the sha256 for the version and type chosen above

Adjust the following command with tha arguments above and execute it twice:

```asciidoc
$ ./gradlew wrapper --gradle-version 7.5.1 \
    --distribution-type bin \
    --gradle-distribution-sha256-sum f6b8596b10cce501591e92f229816aa4046424f3b24d771751b06779d58c8ec4
```

The first execution should automatically update:

- `mandioca/gradle/wrapper/gradle-wrapper.properties`

The second execution should then update:

- `mandioca/gradle/wrapper/gradle-wrapper.jar`
- `mandioca/gradlew`
- `mandioca/gradlew.bat`

The four updated files are ready to be committed.
