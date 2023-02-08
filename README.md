# plugins4j
Lightweight plugin loading and unloading at runtime.

## Example application
An example CLI program using `plugins4j` can be found [here](https://github.com/LCLPYT/plugins4j/blob/main/src/example/java/work/lclpnet/example/Main.java).

### Run the example app yourself
First, clone the repository.
Then, make sure you have Java 17 installed. 
If not, [install it](https://adoptium.net/temurin/releases/).

Build and run the application:

```bash
cd plugins4j/           # project directory
./gradlew deploy        # make sure gradle uses java 17

cd run/
java -jar example.jar   # make sure to use java 17
```

Type `help` to display all available commands.
