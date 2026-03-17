# Sử dụng môi trường Java 17 để chạy ứng dụng
FROM openjdk:17

# Khai báo biến đường dẫn tới file .jar đã build xong trong thư mục target
ARG JAR_FILE=target/*.jar

# Copy file jar từ máy tính vào trong image Docker và đổi tên thành backend-service.jar
COPY ${JAR_FILE} backend-service.jar

# Lệnh khởi chạy ứng dụng khi container bắt đầu chạy
ENTRYPOINT ["java", "-jar", "backend-service.jar"]

# Thông báo ứng dụng sẽ chạy trên cổng 8080
EXPOSE 8080