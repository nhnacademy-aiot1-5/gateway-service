# 베이스 이미지 선택
FROM amd64/maven:3.8.4-openjdk-11-slim as build

ARG SECRET_KEY

ENV SECRET_KEY=${SECRET_KEY}

# 작업 디렉토리 설정
WORKDIR /app

# 프로젝트 파일 복사
COPY pom.xml .
COPY src ./src

# Maven을 통한 프로젝트 빌드
RUN mvn -B -f pom.xml clean package -DskipTests \
    -Djwt.secret.key=${SECRET_KEY}

# 런타임 이미지 준비
FROM amd64/openjdk:11-jre-slim

WORKDIR /app

# 빌드 단계에서 생성된 jar 파일 복사
COPY --from=build /app/target/*.jar app.jar

# 포트 8080 열기
EXPOSE 8081

ENV SECRET_KEY=${SECRET_KEY}

# 애플리케이션 실행
CMD ["java", "-jar", "app.jar"]
