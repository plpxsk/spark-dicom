# Overview

I want to read DICOM files in PySpark.

I can use the following: https://github.com/kaiko-ai/spark-dicom

But first, I need to create this "jar" file so I can enable this functionality
in my own Spark instance.

# Create "jar"

This needs to be done once.

I am doing this locally on macOS

1. Download SBT: https://www.scala-sbt.org
    * See Install Problems below
2. Build and use Jar: https://spark.apache.org/docs/latest/quick-start.html

Then, copy JAR file to your spark environment, and use as in step 2.

# Install problems

Had some issues like https://github.com/sbt/sbt/issues/6925

So, use JDK17 instead of 18.

Fix with brew:

```shell
brew install openjdk@17
brew info openjdk@17
export PATH="/usr/local/opt/openjdk@17/bin:$PATH"
```

This way, `sbt` will use path for Java 17 (check with `java --version`)
