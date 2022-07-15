# FileServer
Another Jetbrains Academy project, emulating a file server wherein clients can store, retrieve and delete their own files.
I learnt about Functional Interfaces, Anonymous Classes, Lambda Expressions, Multithreading, Sets and Maps and Serialization. 

I realize that since the IP Adresses all refer to the local host, all sessions will occur on the same machine. This means all 
client sessions use the same console for input and output, so if there were multiple sessions as in the program, then all the 
input and output would thus get mixed up. But this is supposed to be a mock program, I have written it with the idea that 
different people would be using it on different machines.

I also would've liked to have multithreaded the writing and reading of data onto files which would be more efficient so that the server can continue interacting with
users, but I realized I would need to use Callable and Future. I had already learnt serveral more mouthfuls thant I was expecting and decided I might come back to it
later on.
