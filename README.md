- # General
  - #### Team#: 
    - 10

  - #### Names: 
    - Alejandro Matamoros, Vaibhav Patel

  - #### Project 5 Video Demo Link:

### 2. Deployment Steps:
We built our project from the cs122b-fall21-project1-api-example repo so the steps to deploy the application with Tomcat are the same.

#####AWS Deployment Steps:
1. Inside the repo that has the pom.xml file, build the war file:
  ```
  mvn package
  ```
2. Copy the newly built war file to tomcat webapps:
  ```
  sudo cp ./target/*.war /var/lib/tomcat9/webapps/
  ```
3. tomcat web apps should now have the new fabflix.war file:
  ```
  ls -lah /var/lib/tomcat9/webapps/
  ```
4. The web app is now deployed. Go to the tomcat manager page to find and click on the projects link which will take you to our landing page.

Tomcat Manager Login Info:
```
username: fabflix
password: fabflix
```

#####Running Locally with IntelliJ:
1. Make sure Tomcat is configured as described here: https://canvas.eee.uci.edu/courses/40150/pages/intellij-idea-tomcat-configuration
2. Press the start button near the top of the screen and the app will now run locally

### 3. Member Contributions:
Both of us collaborated on the various components of the project and discussed how to approach them together before implementing the pieces. The contributions in terms of the implementation are listed below.
- ##### Alex Matamoros:
  - JDBC Connection Pooling
  - MySQL Master-Slave Replication
  - Scaling Fabflix with a cluster of MySQL/Tomcat and a load balancer
- ##### Vaibhav Patel:
  - Measuring the performance of Fabflix search feature
  - Demo Recording


- # Connection Pooling
  - #### Include the filename/path of all code/configuration files in GitHub of using JDBC Connection Pooling.
    - Config: WebContent/META-INF/context.xml
    - src/AddMovie.java
    - src/AddStar.java
    - src/Genres.java
    - src/DashboardLogin.java
    - src/LoginServlet.java
    - src/MovieServlet.java
    - src/StarsServlet.java
    - src/SingleMovieServlet.java
    - src/SingleStarServlet.java
    - src/TransactionServlet.java
  - #### Explain how Connection Pooling is utilized in the Fabflix code.
    In the Fabflix code, connection pooling is utilized to increase the performance of Fabflix by allowing connections to be reused by clients. We used prepared statements in our code and enabled the 'cachePrepStmts' setting to allow for connection pooling.
  - #### Explain how Connection Pooling works with two backend SQL.
    In the two backend MySQL servers, connection pooling is utilized to increase the performance of Fabflix by allowing connections to be reused by clients for each individual MySQL server. If the client is writing to the database, connection pooling will be used on the Master server. If the client is simply reading from the database, connection pooling will be used on whichever server the client is connected to. Because of the load balancer, connection to each MySQL backend server is random however if the user connects to a slave server and they want to create a write request, the request is forwarded to the Master server.


- # Master/Slave
  - #### Include the filename/path of all code/configuration files in GitHub of routing queries to Master/Slave SQL.
    - Config: WebContent/META-INF/context.xml
    - src/AddMovie.java
    - src/AddStar.java
    - src/TransactionServlet.java

  - #### How read/write requests were routed to Master/Slave SQL?
    Since the MySQL databases are in sync for both the master and slave servers, reads occur on whichever server the client is currently connected to which is determined randomly by the load balancer. However, all write requests are sent to the master server since it will update all servers after the change in the database has occurred.