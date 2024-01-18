# File Server
Another Jetbrains Academy project, emulating a file server wherein clients can store, retrieve and delete their own files. I learnt about Functional Interfaces, Anonymous Classes, Lambda Expressions, Multithreading, Sets and Maps and Serialization. 

This project cannot be run alone, it was used to pass a testing system which initiates the two parts of the program simultaneously.

I realize that since the IP Adresses all refer to the local host, all sessions will occur on the same machine. This means all client sessions use the same console for input and output, so if there were multiple sessions, then all the input and output would thus get mixed up. But this is supposed to be a mock program which is written to pass the Jetbrains Academy Tests for this particular project, and I have written it with idea that in reality, different people would be using it on different machines.

I also would've liked to have multithreaded the writing and reading of data onto files which would be more efficient so that the server can continue interacting with users, but I realized I would need to use Callable and Future. I think might like come back to it later on and implement a proper version which can really be used. I would even like to try and implement a file system based on tags instead of folders.
