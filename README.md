# To run the server, run command `java -Xmx64m -jar A4.jar <port>` in the top level

# Design Choices:
1. Implemented a cache that automatically delete entries that are more than 1 second old
2. Have two threads, one keep pushing requests to a queue, while the other thread take request from the queue and process it.

