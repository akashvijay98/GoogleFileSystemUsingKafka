# **GoogleFileSystemUsingKafka**
the files are broken into chunks on the client side and then sent to kafka paritions and the consumers which are subscribed to the partitions read the chunks and store them on seperate servers


# **Steps to follow:**
  ## Build Server1 Image

  **step 1:** Open terminal and goto gfsServer1 Directory
  **step 2:** type the command  **docker build -t server1 .**

  ## Build Server2 Image
  **step 1:** Open terminal and goto gfsServer1 Directory
  **step 2:** type the command  **docker build -t server2 .**

  ## Spin Up the Docker Containers:
  Run the **docker-compose.yml** file by typing **docker-compose up -d**


  ## Run the GFS client
  goto **gfs/src/main/java/com/cloud/gfs/** and run the **GfsApplication.java** file.

  ## Open Postman:
  **step 1:** create a **POST** request 
  **step 2:** select **raw** and **JSON** for Payload

  **URL**
   
  **localhost:8080/gfs**

  **PayLoad**

    {
      "file":" {your file path} ",
      "fileName": {"your file name"},
      "fileExtension": "{ your file extension}",
      "fileSize":"{your file size}"
    }





