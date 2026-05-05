FROM maven:3.9.9-eclipse-temurin-21

ARG TEST_PROFILE=api
ARG APIBASEURL=http://127.0.0.1:5000/
ARG UIBASEURL=http://localhost:3000

ENV TEST_PROFILE=${TEST_PROFILE}
ENV APIBASEURL=${APIBASEURL}
ENV UIBASEURL=${UIBASEURL}

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY . .

# Убедимся, что файл существует и имеет правильные права
RUN ls -la run-tests.sh && \
    chmod +x run-tests.sh && \
    cat run-tests.sh | head -1

# Используем явный shell для выполнения
CMD ["sh", "-c", "/app/run-tests.sh"]


#RUN chmod +x /app/run-tests.sh

#CMD ["/app/run-tests.sh"]

