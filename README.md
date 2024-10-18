## NoobChain

Simple blockchain implementation in Java according to the tutorials
from <a href="https://medium.com/programmers-blockchain/creating-your-first-blockchain-with-java-part-2-transactions-2cdac335e0ce" target="_blank">
Kass</a>
with some improvement on parallelization.
***

### Functionality

* Allows users to create wallets with ‘new Wallet();’
* Provides wallets with public and private keys using Elliptic-Curve cryptography.
* Secures the transfer of funds, by using a digital signature algorithm to prove ownership.
* Allow users to make transactions on blockchain with
  `Block.addTransaction(walletA.sendFunds( walletB.publicKey, ${AMOUNT}));`

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

[![demo](https://asciinema.org/a/8fD1SGD8jHbcFYt96YtlOhq9W.svg)](https://asciinema.org/a/8fD1SGD8jHbcFYt96YtlOhq9W)
On terminal snapshot it simulates the basic use of the application, where two
wallets are created and funds are sent from one to the other.

It uses single-threaded execution as it runs `NoobChain.java` class.
`ParallelNoobChain.java` is used for parallelization.
