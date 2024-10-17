## NoobChain

Simple blockchain implementation in Java according to the tutorials from <a href="https://medium.com/javarevisited/java-concurrency-2-how-to-create-threads-ee2648184eb0">Dylan Smith</a>
with some improvement on parallelization.
***

### Configuration

configure java with sdkman

compile and run the application via this script `package-app.sh`

```bash
$ chmod +x package-app.sh
$ ./package-app.sh
```

***

### Usage
How to run the application:
[![asciicast](https://asciinema.org/a/xyxzQ7zyJBsNryL4L7m7qkHHh.svg)](https://asciinema.org/a/xyxzQ7zyJBsNryL4L7m7qkHHh)
On terminal snapshot it simulates the basic use of the application, where two
wallets are created and funds are sent from one to the other.

It uses single-threaded execution as it runs `NoobChain.java` class.
`ParallelNoobChain.java` is used for parallelization.
