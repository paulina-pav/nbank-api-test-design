Логи по запуску:

# Minikube:

PS C:\Users\Polina> cd C:\Users\Polina\AquaProjects\nbank-test-design
PS C:\Users\Polina\AquaProjects\nbank-test-design> $env:DOCKER_DEFAULT_PLATFORM="linux/amd64"
PS C:\Users\Polina\AquaProjects\nbank-test-design> minikube start --driver=docker --container-runtime=containerd --cpus=2 --memory=2048 --kubernetes-version=v1.30.0
* minikube v1.38.1 на Microsoft Windows 11 Pro 25H2
* Доступен Kubernetes 1.35.1. Для обновления, укажите: --kubernetes-version=v1.35.1
* Используется драйвер docker на основе существующего профиля
* Starting "minikube" primary control-plane node in "minikube" cluster
* Pulling base image v0.0.50 ...
* Перезагружается существующий docker container для "minikube" ...
* Подготавливается Kubernetes v1.30.0 на containerd 2.2.1 ...
* Компоненты Kubernetes проверяются ...
    - Используется образ gcr.io/k8s-minikube/storage-provisioner:v5
* Включенные дополнения: default-storageclass, storage-provisioner

! C:\Program Files\Docker\Docker\resources\bin\kubectl.exe is version 1.34.1, which may have incompatibilities with Kubernetes 1.30.0.
- Want kubectl v1.30.0? Try 'minikube kubectl -- get pods -A'
* Готово! kubectl настроен для использования кластера "minikube" и "default" пространства имён по умолчанию
  PS C:\Users\Polina\AquaProjects\nbank-test-design> kubectl get nodes
  NAME       STATUS   ROLES           AGE   VERSION
  minikube   Ready    control-plane   22h   v1.30.0
  PS C:\Users\Polina\AquaProjects\nbank-test-design> kubectl get pods -A
  NAMESPACE     NAME                               READY   STATUS    RESTARTS      AGE
  default       backend-64897b8fd8-zrqc7           1/1     Running   1 (98s ago)   22h
  default       frontend-6479f44676-2wpbt          1/1     Running   2 (78s ago)   22h
  kube-system   coredns-7db6d8ff4d-tqf7v           1/1     Running   1 (98s ago)   22h
  kube-system   etcd-minikube                      1/1     Running   1 (98s ago)   22h
  kube-system   kindnet-ch7mh                      1/1     Running   1 (98s ago)   22h
  kube-system   kube-apiserver-minikube            1/1     Running   1 (98s ago)   22h
  kube-system   kube-controller-manager-minikube   1/1     Running   1 (98s ago)   22h
  kube-system   kube-proxy-8g8md                   1/1     Running   1 (98s ago)   22h
  kube-system   kube-scheduler-minikube            1/1     Running   1 (98s ago)   22h
  kube-system   storage-provisioner                1/1     Running   2 (62s ago)   22h
  PS C:\Users\Polina\AquaProjects\nbank-test-design> helm template nbank .\infra\kube
---
# Source: nbank/templates/backend.yaml
apiVersion: v1
kind: Service
metadata:
name: backend
labels:
app: backend
spec:
selector:
app: backend # Сервис будет направлять трафик на поды у которых app=backend
ports:
- name: http
protocol: TCP
port: 4111 # порт, на котором работает сервис
targetPort: 4111 # куда пересылать внутри Pod (контейнер слушает этот порт)
type: NodePort # ClusterIP (только внутри кластера), NodePort (открывает порт на всех нодах кластера)

#  CХЕМА:
#  [твой браузер или тест] — http://<NODE-IP>:4111 (либо на http://localhost:4111)
#  |
#  [NodePort 4111]
#  |
#  [Service port 4111]
#  |
#  [Pod / Контейнер port 4111]
#  |
#  nobugsme/nbank:with_validation_fix
---
# Source: nbank/templates/frontend.yaml
apiVersion: v1
kind: Service
metadata:
name: frontend
spec:
selector:
app: frontend
ports:
- protocol: TCP
port: 80
targetPort: 80
type: NodePort

#  ┌─────────────────────────────────────────────┐
#  │            [Пользователь]                   │
#  │    Браузер → http://localhost:3000          │
#  └─────────────────────────────────────────────┘
#  │
#  ▼
#  (kubectl port-forward svc/frontend 3000:80)
#  │
#  ▼
#  ┌─────────────────────────────────────────────┐
#  │           [Kubernetes Service]              │
#  │   name: frontend                            │
#  │   type: NodePort (не используется тут)      │
#  │   port: 80                                  │
#  │   targetPort: 80                            │
#  └─────────────────────────────────────────────┘
#  │
#  ▼
#  ┌─────────────────────────────────────────────┐
#  │                [Pod]                        │
#  │   label: app=frontend                       │
#  │   создан через Deployment                   │
#  └─────────────────────────────────────────────┘
#  │
#  ▼
#  ┌─────────────────────────────────────────────┐
#  │         [Контейнер в Pod]                   │
#  │   image: nobugsme/nbank-ui:with_nginx       │
#  │   containerPort: 80                         │
#  └─────────────────────────────────────────────┘
---
# Source: nbank/templates/backend.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
name: backend # kubectl get deployment backend
spec:
replicas: 1 # количество копий POD
selector:
matchLabels:
app: backend # связываем Deployment и Pod
template:
metadata:
labels:
app: backend # устанавливаем label на Pod
annotations:
co.elastic.logs/enabled: "true"
co.elastic.logs/module: springboot
co.elastic.logs/json.keys_under_root: "true"
co.elastic.logs/json.add_error_key: "true"
spec:
containers:
- name: backend
image: nobugsme/nbank:with_validation_fix
imagePullPolicy: IfNotPresent
ports:
- containerPort: 4111
---
# Source: nbank/templates/frontend.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
name: frontend
spec:
replicas: 1
selector:
matchLabels:
app: frontend
template:
metadata:
labels:
app: frontend
spec:
containers:
- name: frontend
image: nobugsme/nbank-ui:with_nginx
imagePullPolicy: IfNotPresent
ports:
- containerPort: 80
PS C:\Users\Polina\AquaProjects\nbank-test-design>

# Helm

PS C:\Users\Polina\AquaProjects\nbank-test-design> helm upgrade --install nbank .\infra\kube
Release "nbank" has been upgraded. Happy Helming!
NAME: nbank
LAST DEPLOYED: Tue Jun  2 09:44:41 2026
NAMESPACE: default
STATUS: deployed
REVISION: 2
DESCRIPTION: Upgrade complete
TEST SUITE: None
PS C:\Users\Polina\AquaProjects\nbank-test-design> helm list
NAME    NAMESPACE       REVISION        UPDATED                                 STATUS          CHART           APP VERSION
nbank   default         2               2026-06-02 09:44:41.8341142 +0700 +07   deployed        nbank-0.0.1     1.0.0   
PS C:\Users\Polina\AquaProjects\nbank-test-design> helm status nbank
NAME: nbank
LAST DEPLOYED: Tue Jun  2 09:44:41 2026
NAMESPACE: default
STATUS: deployed
REVISION: 2
DESCRIPTION: Upgrade complete
RESOURCES:
==> v1/Service
NAME      TYPE       CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
backend   NodePort   10.104.108.64   <none>        4111:31126/TCP   22h
frontend   NodePort   10.111.214.2   <none>   80:30414/TCP   22h

==> v1/Deployment
NAME      READY   UP-TO-DATE   AVAILABLE   AGE
backend   1/1     1            1           22h
frontend   1/1   1     1     22h

==> v1/Pod(related)
NAME                       READY   STATUS    RESTARTS        AGE
backend-64897b8fd8-zrqc7   1/1     Running   1 (3m44s ago)   22h
frontend-6479f44676-2wpbt   1/1   Running   2 (3m24s ago)   22h


TEST SUITE: None
PS C:\Users\Polina\AquaProjects\nbank-test-design>

# Kubelect

TEST SUITE: None
PS C:\Users\Polina\AquaProjects\nbank-test-design> kubectl get pods
NAME                        READY   STATUS    RESTARTS        AGE
backend-64897b8fd8-zrqc7    1/1     Running   1 (4m10s ago)   22h
frontend-6479f44676-2wpbt   1/1     Running   2 (3m50s ago)   22h
PS C:\Users\Polina\AquaProjects\nbank-test-design> kubectl get pods
NAME                        READY   STATUS    RESTARTS        AGE
backend-64897b8fd8-zrqc7    1/1     Running   1 (4m15s ago)   22h
frontend-6479f44676-2wpbt   1/1     Running   2 (3m55s ago)   22h
PS C:\Users\Polina\AquaProjects\nbank-test-design>

# Logs
PS C:\Users\Polina\AquaProjects\nbank-test-design> kubectl logs deployment/backend

.   ____          _            __ _ _
/\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
\\/  ___)| |_)| | | | | || (_| |  ) ) ) )
'  |____| .__|_| |_|_| |_\__, | / / / /
=========|_|==============|___/=/_/_/_/
:: Spring Boot ::                (v3.2.5)

{"timestamp":"2026-06-02T02:41:32.655367775Z","logger_name":"me.nobugs.bank.BankApplication","thread_name":"main","level":"INFO","message":"Starting BankApplication v0.0.1-SNAPSHOT using Java 17.0.16 with PID 1 (/app/app.jar started by root in /app)"}
{"timestamp":"2026-06-02T02:41:32.678079799Z","logger_name":"me.nobugs.bank.BankApplication","thread_name":"main","level":"INFO","message":"No active profile set, falling back to 1 default profile: \"default\""}
{"timestamp":"2026-06-02T02:41:37.961704677Z","logger_name":"org.springframework.boot.web.embedded.tomcat.TomcatWebServer","thread_name":"main","level":"INFO","message":"Tomcat initialized with port 4111 (http)"}
{"timestamp":"2026-06-02T02:41:38.032511107Z","logger_name":"org.apache.catalina.core.StandardService","thread_name":"main","level":"INFO","message":"Starting service [Tomcat]"}
{"timestamp":"2026-06-02T02:41:38.032778723Z","logger_name":"org.apache.catalina.core.StandardEngine","thread_name":"main","level":"INFO","message":"Starting Servlet engine: [Apache Tomcat/10.1.20]"}
{"timestamp":"2026-06-02T02:41:38.088663082Z","logger_name":"org.apache.catalina.core.ContainerBase.[Tomcat].[localhost].[/]","thread_name":"main","level":"INFO","message":"Initializing Spring embedded WebApplicationContext"}
{"timestamp":"2026-06-02T02:41:38.115507348Z","logger_name":"org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext","thread_name":"main","level":"INFO","message":"Root WebApplicationContext: initialization completed in 4819 ms"}
🧪 MeterRegistry class = io.micrometer.prometheus.PrometheusMeterRegistry
{"timestamp":"2026-06-02T02:41:40.451464953Z","logger_name":"org.springframework.boot.actuate.endpoint.web.EndpointLinksResolver","thread_name":"main","level":"INFO","message":"Exposing 3 endpoint(s) beneath base path '/actuator'"}
{"timestamp":"2026-06-02T02:41:40.554897479Z","logger_name":"org.springframework.security.web.DefaultSecurityFilterChain","thread_name":"main","level":"INFO","message":"Will secure any request with [org.springframework.security.web.session.DisableEncodeUrlFilter@60a01cb, org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter@4efb13f1, org.springframework.security.web.context.SecurityContextHolderFilter@6f867b0c, org.springframework.security.web.header.HeaderWriterFilter@b55f5b7, org.springframework.web.filter.CorsFilter@7bb4ed71, org.springframework.security.web.authentication.logout.LogoutFilter@e4ca109, org.springframework.security.web.authentication.www.BasicAuthenticationFilter@5563bb40, org.springframework.security.web.savedrequest.RequestCacheAwareFilter@54be6213, org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter@48a46b0f, org.springframework.security.web.authentication.AnonymousAuthenticationFilter@5112b7, org.springframework.security.web.access.ExceptionTranslationFilter@141aba65, org.springframework.security.web.access.intercept.AuthorizationFilter@645dc557]"}
{"timestamp":"2026-06-02T02:41:41.73947891Z","logger_name":"org.springframework.boot.web.embedded.tomcat.TomcatWebServer","thread_name":"main","level":"INFO","message":"Tomcat started on port 4111 (http) with context path ''"}
{"timestamp":"2026-06-02T02:41:41.760522361Z","logger_name":"me.nobugs.bank.BankApplication","thread_name":"main","level":"INFO","message":"Started BankApplication in 11.334 seconds (process running for 13.567)"}
PS C:\Users\Polina\AquaProjects\nbank-test-design> kubectl logs deployment/frontend
/docker-entrypoint.sh: /docker-entrypoint.d/ is not empty, will attempt to perform configuration
/docker-entrypoint.sh: Looking for shell scripts in /docker-entrypoint.d/
/docker-entrypoint.sh: Launching /docker-entrypoint.d/10-listen-on-ipv6-by-default.sh
10-listen-on-ipv6-by-default.sh: info: Getting the checksum of /etc/nginx/conf.d/default.conf
10-listen-on-ipv6-by-default.sh: info: Enabled listen on IPv6 in /etc/nginx/conf.d/default.conf
/docker-entrypoint.sh: Sourcing /docker-entrypoint.d/15-local-resolvers.envsh
/docker-entrypoint.sh: Launching /docker-entrypoint.d/20-envsubst-on-templates.sh
/docker-entrypoint.sh: Launching /docker-entrypoint.d/30-tune-worker-processes.sh
/docker-entrypoint.sh: Configuration complete; ready for start up
PS C:\Users\Polina\AquaProjects\nbank-test-design>

# Проброс портов
PS C:\Users\Polina\AquaProjects\nbank-test-design> kubectl port-forward svc/backend 4111:4111
Forwarding from 127.0.0.1:4111 -> 4111
Forwarding from [::1]:4111 -> 4111

В отдельной вкладке PowerShell:

PS C:\Users\Polina> kubectl port-forward svc/frontend 3000:80
Forwarding from 127.0.0.1:3000 -> 80
Forwarding from [::1]:3000 -> 80

## Логи бэкенда - показываю, что фронт работает и я создала в нем пользователя:

PS C:\Users\Polina\AquaProjects\nbank-test-design> kubectl logs deployment/backend

.   ____          _            __ _ _
/\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
\\/  ___)| |_)| | | | | || (_| |  ) ) ) )
'  |____| .__|_| |_|_| |_\__, | / / / /
=========|_|==============|___/=/_/_/_/
:: Spring Boot ::                (v3.2.5)

{"timestamp":"2026-06-02T02:41:32.655367775Z","logger_name":"me.nobugs.bank.BankApplication","thread_name":"main","level":"INFO","message":"Starting BankApplication v0.0.1-SNAPSHOT using Java 17.0.16 with PID 1 (/app/app.jar started by root in /app)"}
{"timestamp":"2026-06-02T02:41:32.678079799Z","logger_name":"me.nobugs.bank.BankApplication","thread_name":"main","level":"INFO","message":"No active profile set, falling back to 1 default profile: \"default\""}
{"timestamp":"2026-06-02T02:41:37.961704677Z","logger_name":"org.springframework.boot.web.embedded.tomcat.TomcatWebServer","thread_name":"main","level":"INFO","message":"Tomcat initialized with port 4111 (http)"}
{"timestamp":"2026-06-02T02:41:38.032511107Z","logger_name":"org.apache.catalina.core.StandardService","thread_name":"main","level":"INFO","message":"Starting service [Tomcat]"}
{"timestamp":"2026-06-02T02:41:38.032778723Z","logger_name":"org.apache.catalina.core.StandardEngine","thread_name":"main","level":"INFO","message":"Starting Servlet engine: [Apache Tomcat/10.1.20]"}
{"timestamp":"2026-06-02T02:41:38.088663082Z","logger_name":"org.apache.catalina.core.ContainerBase.[Tomcat].[localhost].[/]","thread_name":"main","level":"INFO","message":"Initializing Spring embedded WebApplicationContext"}
{"timestamp":"2026-06-02T02:41:38.115507348Z","logger_name":"org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext","thread_name":"main","level":"INFO","message":"Root WebApplicationContext: initialization completed in 4819 ms"}
🧪 MeterRegistry class = io.micrometer.prometheus.PrometheusMeterRegistry
{"timestamp":"2026-06-02T02:41:40.451464953Z","logger_name":"org.springframework.boot.actuate.endpoint.web.EndpointLinksResolver","thread_name":"main","level":"INFO","message":"Exposing 3 endpoint(s) beneath base path '/actuator'"}
{"timestamp":"2026-06-02T02:41:40.554897479Z","logger_name":"org.springframework.security.web.DefaultSecurityFilterChain","thread_name":"main","level":"INFO","message":"Will secure any request with [org.springframework.security.web.session.DisableEncodeUrlFilter@60a01cb, org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter@4efb13f1, org.springframework.security.web.context.SecurityContextHolderFilter@6f867b0c, org.springframework.security.web.header.HeaderWriterFilter@b55f5b7, org.springframework.web.filter.CorsFilter@7bb4ed71, org.springframework.security.web.authentication.logout.LogoutFilter@e4ca109, org.springframework.security.web.authentication.www.BasicAuthenticationFilter@5563bb40, org.springframework.security.web.savedrequest.RequestCacheAwareFilter@54be6213, org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter@48a46b0f, org.springframework.security.web.authentication.AnonymousAuthenticationFilter@5112b7, org.springframework.security.web.access.ExceptionTranslationFilter@141aba65, org.springframework.security.web.access.intercept.AuthorizationFilter@645dc557]"}
{"timestamp":"2026-06-02T02:41:41.73947891Z","logger_name":"org.springframework.boot.web.embedded.tomcat.TomcatWebServer","thread_name":"main","level":"INFO","message":"Tomcat started on port 4111 (http) with context path ''"}
{"timestamp":"2026-06-02T02:41:41.760522361Z","logger_name":"me.nobugs.bank.BankApplication","thread_name":"main","level":"INFO","message":"Started BankApplication in 11.334 seconds (process running for 13.567)"}
PS C:\Users\Polina\AquaProjects\nbank-test-design> kubectl logs deployment/frontend
/docker-entrypoint.sh: /docker-entrypoint.d/ is not empty, will attempt to perform configuration
/docker-entrypoint.sh: Looking for shell scripts in /docker-entrypoint.d/
/docker-entrypoint.sh: Launching /docker-entrypoint.d/10-listen-on-ipv6-by-default.sh
10-listen-on-ipv6-by-default.sh: info: Getting the checksum of /etc/nginx/conf.d/default.conf
10-listen-on-ipv6-by-default.sh: info: Enabled listen on IPv6 in /etc/nginx/conf.d/default.conf
/docker-entrypoint.sh: Sourcing /docker-entrypoint.d/15-local-resolvers.envsh
/docker-entrypoint.sh: Launching /docker-entrypoint.d/20-envsubst-on-templates.sh
/docker-entrypoint.sh: Launching /docker-entrypoint.d/30-tune-worker-processes.sh
/docker-entrypoint.sh: Configuration complete; ready for start up
PS C:\Users\Polina\AquaProjects\nbank-test-design> kubectl port-forward svc/backend 4111:4111
Forwarding from 127.0.0.1:4111 -> 4111
Forwarding from [::1]:4111 -> 4111
PS C:\Users\Polina\AquaProjects\nbank-test-design> kubectl logs deployment/frontend
/docker-entrypoint.sh: /docker-entrypoint.d/ is not empty, will attempt to perform configuration
/docker-entrypoint.sh: Looking for shell scripts in /docker-entrypoint.d/
/docker-entrypoint.sh: Launching /docker-entrypoint.d/10-listen-on-ipv6-by-default.sh
10-listen-on-ipv6-by-default.sh: info: Getting the checksum of /etc/nginx/conf.d/default.conf
10-listen-on-ipv6-by-default.sh: info: Enabled listen on IPv6 in /etc/nginx/conf.d/default.conf
/docker-entrypoint.sh: Sourcing /docker-entrypoint.d/15-local-resolvers.envsh
/docker-entrypoint.sh: Launching /docker-entrypoint.d/20-envsubst-on-templates.sh
/docker-entrypoint.sh: Launching /docker-entrypoint.d/30-tune-worker-processes.sh
/docker-entrypoint.sh: Configuration complete; ready for start up
127.0.0.1 - - [02/Jun/2026:02:47:50 +0000] "GET / HTTP/1.1" 200 648 "-" "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36"
2026/06/02 02:47:51 [error] 29#29: *2 open() "/usr/share/nginx/html/logo192.png" failed (2: No such file or directory), client: 127.0.0.1, server: , request: "GET /logo192.png HTTP/1.1", host: "localhost:3000", referrer: "http://localhost:3000/"
127.0.0.1 - admin [02/Jun/2026:02:47:52 +0000] "GET /api/v1/customer/profile HTTP/1.1" 403 123 "http://localhost:3000/" "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36"
127.0.0.1 - - [02/Jun/2026:02:47:58 +0000] "POST /api/v1/auth/login HTTP/1.1" 200 46 "http://localhost:3000/" "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36"
127.0.0.1 - admin [02/Jun/2026:02:47:58 +0000] "GET /api/v1/admin/users HTTP/1.1" 200 12 "http://localhost:3000/admin" "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36"
127.0.0.1 - admin [02/Jun/2026:02:48:23 +0000] "POST /api/v1/admin/users HTTP/1.1" 400 169 "http://localhost:3000/admin" "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36"
127.0.0.1 - admin [02/Jun/2026:02:48:40 +0000] "POST /api/v1/admin/users HTTP/1.1" 201 155 "http://localhost:3000/admin" "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36"
PS C:\Users\Polina\AquaProjects\nbank-test-design>

## Логи фронтенда, когда создавала пользователя

PS C:\Users\Polina> kubectl port-forward svc/frontend 3000:80
Forwarding from 127.0.0.1:3000 -> 80
Forwarding from [::1]:3000 -> 80
Handling connection for 3000
Handling connection for 3000
PS C:\Users\Polina> kubectl logs deployment/frontend
/docker-entrypoint.sh: /docker-entrypoint.d/ is not empty, will attempt to perform configuration
/docker-entrypoint.sh: Looking for shell scripts in /docker-entrypoint.d/
/docker-entrypoint.sh: Launching /docker-entrypoint.d/10-listen-on-ipv6-by-default.sh
10-listen-on-ipv6-by-default.sh: info: Getting the checksum of /etc/nginx/conf.d/default.conf
10-listen-on-ipv6-by-default.sh: info: Enabled listen on IPv6 in /etc/nginx/conf.d/default.conf
/docker-entrypoint.sh: Sourcing /docker-entrypoint.d/15-local-resolvers.envsh
/docker-entrypoint.sh: Launching /docker-entrypoint.d/20-envsubst-on-templates.sh
/docker-entrypoint.sh: Launching /docker-entrypoint.d/30-tune-worker-processes.sh
/docker-entrypoint.sh: Configuration complete; ready for start up
127.0.0.1 - - [02/Jun/2026:02:47:50 +0000] "GET / HTTP/1.1" 200 648 "-" "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36"
2026/06/02 02:47:51 [error] 29#29: *2 open() "/usr/share/nginx/html/logo192.png" failed (2: No such file or directory), client: 127.0.0.1, server: , request: "GET /logo192.png HTTP/1.1", host: "localhost:3000", referrer: "http://localhost:3000/"
127.0.0.1 - admin [02/Jun/2026:02:47:52 +0000] "GET /api/v1/customer/profile HTTP/1.1" 403 123 "http://localhost:3000/" "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36"
127.0.0.1 - - [02/Jun/2026:02:47:58 +0000] "POST /api/v1/auth/login HTTP/1.1" 200 46 "http://localhost:3000/" "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36"
127.0.0.1 - admin [02/Jun/2026:02:47:58 +0000] "GET /api/v1/admin/users HTTP/1.1" 200 12 "http://localhost:3000/admin" "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36"
127.0.0.1 - admin [02/Jun/2026:02:48:23 +0000] "POST /api/v1/admin/users HTTP/1.1" 400 169 "http://localhost:3000/admin" "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36"
127.0.0.1 - admin [02/Jun/2026:02:48:40 +0000] "POST /api/v1/admin/users HTTP/1.1" 201 155 "http://localhost:3000/admin" "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36"
PS C:\Users\Polina>


# Создание реплик и проверка, что они создались или удалились


PS C:\Users\Polina> kubectl scale deployment backend --replicas=2
deployment.apps/backend scaled
PS C:\Users\Polina> kubectl get pods
NAME                        READY   STATUS    RESTARTS        AGE
backend-64897b8fd8-s8djz    1/1     Running   0               7s
backend-64897b8fd8-zrqc7    1/1     Running   1 (9m28s ago)   22h
frontend-6479f44676-2wpbt   1/1     Running   2 (9m8s ago)    22h
PS C:\Users\Polina> kubectl scale deployment backend --replicas=1
deployment.apps/backend scaled
PS C:\Users\Polina> kubectl get pods
NAME                        READY   STATUS    RESTARTS        AGE
backend-64897b8fd8-zrqc7    1/1     Running   1 (9m59s ago)   22h
frontend-6479f44676-2wpbt   1/1     Running   2 (9m39s ago)   22h
PS C:\Users\Polina>