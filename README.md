
## High Level Design

![HLD](./static/gfsHLD.png)


## Steps to follow:
  
  ### Step 1: Build Server1 Image
  1. Open terminal and goto gfsServer1 Directory
  2. type the command  **docker build -t server1 .**
     
  ### Step 2: Build Server2 Image
  1. Open terminal and goto gfsServer1 Directory
  2. type the command  **docker build -t server2 .**
  
  ### Step3: Start Docker Containers:
  1 Run the **docker-compose.yml** file by typing **docker-compose up -d**
  - this will start and run containers for Postgres, Kafka and GFS servers 
  
  ### Step 4: Run the GFS client
  1. goto directory **gfs/src/main/java/com/cloud/gfs/** and run the **GfsApplication.java** file.
  
  ### Step 5: API Testing with Postman  
  
  To test the API using Postman, follow these steps:  
 
### Step 1: Set Up the Request
- **Method:** `POST`  
- **URL:** `http://localhost:8080/gfs` (replace with your API endpoint)

---

### Step 2: Configure the Body
1. Click the **Body** tab below the URL bar.  
2. Select the **form-data** radio button.  

---

### Step 3: Add Parameters
For each parameter in your `uploadFile` method, create a new row in the key-value editor:

| Key          | Type  | Value        | Description                          |
|--------------|-------|-------------|--------------------------------------|
| `file`       | File  | *Select File* | The `MultipartFile` from your code. Change the key's type from **Text** to **File** by clicking the dropdown. |
| `fileName`   | Text  | `my_document` | The filename string.                 |
| `fileExtension` | Text | `txt`       | The file extension string.           |
| `fileSize`   | Text  | `12345`      | The file size integer.               |

---

### Step 4: Send the Request
1. Click the blue **Send** button on the right side of the URL bar.  
2. Postman will automatically set the correct `Content-Type: multipart/form-data` header and format the request for you.  

---
  ### 2. Read File API   
  
  To retrieve a file, follow these steps:  
  
  1. Open Postman and create a **GET** request.  
  2. Set the request URL to:  http://localhost:8080/gfs
  
  3. Add the required query parameters:  
  - `fileId`: Unique identifier of the file.  
  - `fileName`: Name of the file.  
  - `fileExtension`: File extension (e.g., `.txt`, `.pdf`).  
  
  4. Example request:  
  http://localhost:8080/gfs?fileId={your-file-id}&fileName={your-file-name}&fileExtension={your-file-extension}
