FROM java:8
ADD hsweb-payment-assemble/target/hsweb-payment-assemble.jar /app.jar
ADD hsweb-payment-ui /hsweb-payment-ui
ADD ip2region.db /ip2region.db
ENTRYPOINT ["java","-jar","-server","-XX:+UseG1GC","/app.jar"]