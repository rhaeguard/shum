rm DummyClass.class

/home/owary/.jdks/graalvm-ce-17/bin/java \
  -javaagent:/opt/ideaIU-2021.3/idea-IU-213.5744.223/lib/idea_rt.jar=40509:/opt/ideaIU-2021.3/idea-IU-213.5744.223/bin \
  -Dfile.encoding=UTF-8 \
  -classpath /home/owary/Programming/FunProjects/shum/jvm-impl/target/classes:/home/owary/.m2/repository/org/ow2/asm/asm/9.3/asm-9.3.jar:/home/owary/.m2/repository/org/ow2/asm/asm-commons/9.3/asm-commons-9.3.jar:/home/owary/.m2/repository/org/ow2/asm/asm-tree/9.3/asm-tree-9.3.jar:/home/owary/.m2/repository/org/ow2/asm/asm-analysis/9.3/asm-analysis-9.3.jar \
  io.shum.Compiler "hello_world.uk"

java DummyClass