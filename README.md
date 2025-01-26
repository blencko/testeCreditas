## Para rodar a aplicação de forma completa via docker compose deve-se usar o comando

> Pre-requisitos:
> Docker
> java Jdk21 (Local)

```bash 
  docker compose --profile prod up --build 
```
##

## Para rodar a aplicação de forma completa via linha de comando em modo dev deve-se usar o comando

### Para versão em linux
```bash 
  ./gradlew clean build -x test
  docker compose up -d
  java -jar ./build/libs/testecreditas-0.0.1-SNAPSHOT.jar "--spring.profiles.active=dev"
```


### Para versão em windows
```bash 
  ./gradlew.bat clean build -x test
  docker compose up -d
  java -jar ./build/libs/testecreditas-0.0.1-SNAPSHOT.jar "--spring.profiles.active=dev"
```

### Para rodar os testes 
```bash 
  ./gradlew.bat test
```



