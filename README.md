# README - Aplicação TesteCreditas

Este projeto é uma aplicação Java que pode ser executada tanto em ambiente de desenvolvimento quanto em produção, utilizando Docker Compose. Abaixo você encontra instruções de instalação, execução, e algumas dicas de configuração.

---

## Sumário

1. [Pré-requisitos](#pré-requisitos)
2. [Modo de Produção via Docker Compose](#modo-de-produção-via-docker-compose)
3. [Modo de Desenvolvimento (Linha de Comando)](#modo-de-desenvolvimento-linha-de-comando)
    - [Linux](#para-versao-em-linux)
    - [Windows](#para-versao-em-windows)
4. [Rodando Testes](#rodando-testes)
5. [Perfis do Spring](#perfis-do-spring)
6. [Passo a Passo para Docker Compose](#passo-a-passo-para-docker-compose)
7. [Detecção de Plataforma e Erros Comuns](#detecção-de-plataforma-e-erros-comuns)
8. [Uso de Plugins do Gradle](#uso-de-plugins-do-gradle)
9. [Explicação de Comandos](#explicação-de-comandos)
10. [Lista de Endpoints (Se Aplicável)](#lista-de-endpoints)
11. [Banco de Dados (Se Aplicável)](#banco-de-dados)

---

## Pré-requisitos

- **Docker** (versão recente)
- **Java JDK 21** (instalado localmente)
- (Opcional) Permissões de execução para scripts `.sh` em sistemas baseados em Unix.

---



## Modo de Produção via Docker Compose

Para rodar a aplicação de forma completa via Docker Compose em modo de produção, utilize:

```bash
docker compose --profile prod up --build
```

Esse comando irá:

- Construir as imagens Docker necessárias.
- Subir os containers configurados no arquivo `docker-compose.yml`.
- Executar a aplicação com o perfil de **produção** do Spring.

---

## Modo de Desenvolvimento (Linha de Comando)

### Para versão em Linux

```bash
./gradlew clean build -x test

docker compose up -d

java -jar ./build/libs/testecreditas-0.0.1-SNAPSHOT.jar "--spring.profiles.active=dev"
```

### Para versão em Windows

```bash
./gradlew.bat clean build -x test

docker compose up -d

java -jar ./build/libs/testecreditas-0.0.1-SNAPSHOT.jar "--spring.profiles.active=dev"
```

Nesses cenários, o modo **dev** é configurado para desenvolvimento local, normalmente com logs mais verbosos, hot reload (dependendo da configuração), e conexões de teste.

---

## Rodando Testes

Para rodar os testes, execute:

```bash
./gradlew.bat test
```

ou, em Linux/MacOS:

```bash
./gradlew test
```

Isso executará todos os testes configurados no projeto. Se você deseja gerar relatórios de cobertura (por exemplo, via plugin Jacoco), consulte a seção [Uso de Plugins do Gradle](#uso-de-plugins-do-gradle).

---

## Perfis do Spring

- **dev**: Utilizado em desenvolvimento local (logs mais detalhados, configurações de desenvolvimento, etc.).
- **prod**: Utilizado em produção (configurações otimizadas, logs reduzidos, etc.).

Você pode definir o perfil via variável de ambiente ou diretamente na linha de comando, como no exemplo `--spring.profiles.active=dev`.

---

## Passo a Passo para Docker Compose

1. **Clonar o repositório**: `git clone <url-do-repositorio>`.
2. **Navegar até o diretório do projeto**: `cd testecreditas`.
3. **Construir o projeto (opcional em modo prod)**: `./gradlew clean build -x test`.
4. **Subir os containers**: `docker compose up -d` ou `docker compose --profile prod up --build`.
5. **Verificar se a aplicação subiu corretamente**: `docker compose ps` ou `docker ps`.

---

## Detecção de Plataforma e Erros Comuns

- Em **Linux**/macOS, certifique-se de que o script `gradlew` está com permissão de execução. Caso contrário, rode `chmod +x gradlew`.
- Em **Windows**, use `gradlew.bat`.
- **Portas ocupadas**: se uma porta estiver em uso, você pode alterar a porta da aplicação editando o arquivo de configuração do Spring (`application.properties` ou `application.yml`) ou ajustando variáveis de ambiente.
- **Problemas com Docker**: verifique se o Docker está rodando (`docker info`) e se o usuário atual tem permissão para executar containers.

---

## Uso de Plugins do Gradle

Caso o projeto utilize plugins adicionais, como:

- **Spring Boot**: facilita o build e execução da aplicação.
- **Jacoco**: gera relatórios de cobertura de testes em `build/reports/jacoco/test/html`.
- **Docker** ou outros plugins para empacotamento.

Verifique no arquivo `build.gradle` (ou `build.gradle.kts`) para mais detalhes.

---

## Explicação de Comandos

- `./gradlew clean build -x test`: remove artefatos anteriores e compila o projeto, ignorando testes.
- `docker compose up -d`: sobe os containers em segundo plano.
- `java -jar ./build/libs/testecreditas-0.0.1-SNAPSHOT.jar "--spring.profiles.active=dev"`: executa a aplicação localmente com o perfil **dev**.
- `docker compose --profile prod up --build`: compila imagens e inicia os containers em modo **prod**.

---

## Lista de Endpoints (Se Aplicável)

A aplicação expõe uma série de endpoints para criação e consulta de simulações de crédito. Veja abaixo:

1. **Criar Simulação**

    - **Método**: `POST`
    - **URL**: `/api/simulacoes`
    - **Descrição**: Cria uma nova simulação de empréstimo.
    - **Corpo da Requisição (JSON)**: `RequestSimulacao` contendo dados como valor do empréstimo, data de nascimento do solicitante, prazo em meses, etc.
    - **Retorno**: Objeto `ResponseSimulacao` contendo informações como valor total, valor da parcela mensal, total de juros e identificador da simulação.

2. **Buscar Simulação por ID**

    - **Método**: `GET`
    - **URL**: `/api/simulacoes/{id}`
    - **Descrição**: Retorna os dados de uma simulação a partir do seu ID.
    - **Parâmetro**: `id` (String), identificador único da simulação.
    - **Retorno**: Objeto `ResponseSimulacao` com os detalhes da simulação.

3. **Listar Todas as Simulações**

    - **Método**: `GET`
    - **URL**: `/api/simulacoes`
    - **Descrição**: Retorna todas as simulações já cadastradas.
    - **Retorno**: Lista de objetos `Simulacao` (reactive flux), contendo todas as simulações.

4. **Simular Em Lote**

    - **Método**: `POST`
    - **URL**: `/api/simulacoes/lote`
    - **Descrição**: Cria múltiplas simulações de uma só vez.
    - **Corpo da Requisição (JSON)**: Lista de objetos `RequestSimulacao`.
    - **Retorno**: Lista de objetos `ResponseSimulacao`, cada um representando o resultado de cada simulação.

---

## Banco de Dados

Este projeto utiliza o **MongoDB** como banco de dados. Abaixo seguem instruções para configurá-lo:

- **Execução via Docker Compose**: No arquivo `docker-compose.yml`, há um serviço configurado para o MongoDB. Certifique-se de que esse serviço está habilitado para subir junto com a aplicação (por exemplo, via perfil `dev` ou `prod`).
- **Conexão**: A aplicação, por padrão, se conecta ao MongoDB por meio de variáveis de ambiente como `SPRING_DATA_MONGODB_URI` ou via propriedades no `application-dev.yml`. Ajuste esses valores caso esteja usando credenciais diferentes ou um host/porta específico.

