# spring-configmap-lab

Laboratório **Spring Boot + Kubernetes ConfigMap**.
Demonstra dois usos de ConfigMap em uma aplicação Java/Spring:

1. **Arquivo de configuração** (`application.properties`) montado via volume
2. **Variáveis de ambiente** (ex.: `DB_HOST`) injetadas no container

A API expõe um endpoint `/hello` que lê:
- `app.message` (do arquivo `application.properties` vindo do ConfigMap)
- `DB_HOST` (do ConfigMap de variáveis de ambiente)

---

## Pré-requisitos

- JDK 17+ (recomendado 21)
- Maven 3.8+
- Docker
- Um cluster local (**kind** ou **minikube**)
- `kubectl`

---

## Estrutura

```
spring-configmap-lab/
├── Dockerfile
├── pom.xml
├── src/main/java/configmaplab/DemoApplication.java
└── k8s/
    ├── 00-namespace.yaml
    ├── 10-configmap-app.yaml      # application.properties (arquivo)
    ├── 11-configmap-env.yaml      # variáveis de ambiente (DB_HOST)
    ├── 20-deployment.yaml
    └── 30-service.yaml
```

---

## Rodar localmente (sem Kubernetes)

```bash
mvn -q -DskipTests package
mvn spring-boot:run
# teste:
curl "http://localhost:8080/hello?name=Michel"
# Hi Michel! app.message='Hello from DEFAULT' | DB_HOST='db.local'
```

---

## Build da imagem Docker

```bash
mvn -q -DskipTests package
docker build -t spring-configmap-lab:0.0.1 .
```

### Se usar kind
```bash
kind create cluster --name demo-cluster    # se ainda não existir
kind load docker-image spring-configmap-lab:0.0.1 --name demo-cluster
```

### Se usar minikube
```bash
minikube image load spring-configmap-lab:0.0.1
# ou: eval $(minikube docker-env) && docker build -t spring-configmap-lab:0.0.1 .
```

---

## Deploy no Kubernetes

> **Finalidade**: Este deploy cria um cluster Kubernetes completo com a aplicação Spring Boot, expondo a porta do container para acesso externo através de um Service NodePort.

> Aplique **na ordem**:

```bash
kubectl apply -f k8s/00-namespace.yaml
kubectl apply -f k8s/10-configmap-app.yaml
kubectl apply -f k8s/11-configmap-env.yaml
kubectl apply -f k8s/20-deployment.yaml
kubectl apply -f k8s/30-service.yaml

# Verificar status dos recursos
kubectl -n demo-cm get all

# Acompanhar o status dos pods
kubectl -n demo-cm get pods -w
```

## Finalidade do Deploy

Este deploy cria um cluster Kubernetes completo com:

- **Namespace**: `demo-cm` para isolamento
- **ConfigMaps**: Para configurações da aplicação e variáveis de ambiente
- **Deployment**: Para gerenciar os pods da aplicação Spring Boot
- **Service**: Para expor a aplicação e permitir acesso externo

### Expondo a Porta do Container

O Service `spring-svc` expõe a porta 80 do container através de um NodePort, permitindo acesso externo à aplicação.

---

## Quando o Pod estiver Running

```bash
kubectl -n demo-cm port-forward svc/spring-svc 8080:80
curl "http://localhost:8080/hello?name=Michel"
# Hi Michel! app.message='Hello from ConfigMap v1' | DB_HOST='postgres.default.svc.cluster.local'
```

---

## Validando o uso de ConfigMap

### 1) Alterar o arquivo `application.properties` (ConfigMap como volume)

Edite `k8s/10-configmap-app.yaml`:
```properties
app.message=Hello from ConfigMap v2 (mudou!)
```

Aplique e **reinicie** o Deployment:

```bash
kubectl apply -f k8s/10-configmap-app.yaml
kubectl -n demo-cm rollout restart deploy/spring-app
kubectl -n demo-cm rollout status deploy/spring-app
kubectl -n demo-cm port-forward svc/spring-svc 8080:80
curl "http://localhost:8080/hello?name=Michel"
# ... app.message='Hello from ConfigMap v2 (mudou!)'
```

> Dica: em vez do `rollout restart`, você pode alterar a anotação `config.checksum` em `k8s/20-deployment.yaml` (ex.: de `"v1"` para `"v2"`) e aplicar — isso força um novo Pod.

### 2) Alterar a variável de ambiente `DB_HOST` (ConfigMap como env var)

Edite `k8s/11-configmap-env.yaml`:
```yaml
data:
  DB_HOST: mysql.default.svc.cluster.local
```

Aplique e reinicie:

```bash
kubectl apply -f k8s/11-configmap-env.yaml
kubectl -n demo-cm rollout restart deploy/spring-app
kubectl -n demo-cm port-forward svc/spring-svc 8080:80
curl "http://localhost:8080/hello?name=Michel"
# ... DB_HOST='mysql.default.svc.cluster.local'
```

---

## Comandos úteis

**Ver se o arquivo foi montado:**
```bash
POD=$(kubectl -n demo-cm get pod -l app=spring-app -o jsonpath='{.items[0].metadata.name}')
kubectl -n demo-cm exec -it "$POD" -- sh -lc 'cat /config/application.properties'
```

**Ver endpoints atrás do Service:**
```bash
kubectl -n demo-cm get endpoints spring-svc -o wide
```

**Logs:**
```bash
kubectl -n demo-cm logs -l app=spring-app --tail=100 --timestamps
```

---

## Troubleshooting

- **`curl: (7) Failed to connect …`**
  O `port-forward` cai quando o Pod é recriado. Rode novamente:
  `kubectl -n demo-cm port-forward svc/spring-svc 8080:80`

- **`ImagePullBackOff`**
  Certifique-se de que a imagem está no cluster local:
  `kind load docker-image spring-configmap-lab:0.0.1 --name <nome-do-cluster>`

- **Mudanças não aparecem**
  Reaplique o ConfigMap **e** faça `rollout restart` do Deployment
  (ou altere a anotação `config.checksum` no template do Pod).

- **Porta ocupada**
  Use outra porta local: `kubectl -n demo-cm port-forward svc/spring-svc 8081:80`.

---

## Limpeza

```bash
kubectl delete -f k8s/30-service.yaml
kubectl delete -f k8s/20-deployment.yaml
kubectl delete -f k8s/11-configmap-env.yaml
kubectl delete -f k8s/10-configmap-app.yaml
kubectl delete -f k8s/00-namespace.yaml
```

---

## Licença

MIT — sinta-se à vontade para usar este lab como base.
