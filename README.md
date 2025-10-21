# LaTex Formatter UTF-8
This is a java application that format latex document(.tex) written by UTF-8 (For example, if you use luaLaTex, the tex 
document will be written by UTF-8) under the environment Visual Studio Code with [Custom Local Formatters](https://marketplace.visualstudio.com/items?itemName=jkillian.custom-local-formatters).
You can download jar file from [Release](https://github.com/MizukiNonoyama/latex-formatter-utf8/releases/) or build yourself to use this. 

The build command below:
```Windows Power Shell
$ ./gradlew app:shadowJar
```

## Usage
1. Put the jar file in your tex working directory or somewhere.
2. Open VS Code settings.json and type the code below. The code show the setting when you put the jar file in your working directory. For details, you can check Java commands
   or Custom Local Formatter Settings. This is java application, so you also need [Java 21](https://www.oracle.com/java/technologies/downloads/).
3. Open your tex file with VS Code and Format Document by Custom Local Formatter.

The example of settings.json
```json
{
  "customLocalFormatters.formatters": [
    {
      "command": "java -jar latex-formatter.jar",
      "languages": ["latex"]
    }
  ]
}
```

## Dependencies
* [Visual Studio Code](https://code.visualstudio.com/)
* [Custom Local Formatters](https://marketplace.visualstudio.com/items?itemName=jkillian.custom-local-formatters)
* [Java 21](https://www.oracle.com/java/technologies/downloads/)

### About projects embedded Dependencies
This software includes the work([Gson](https://github.com/google/gson), [Shadow](https://github.com/GradleUp/shadow)) that is distributed in the Apache License 2.0.
