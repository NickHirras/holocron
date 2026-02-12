# Deployment Guide

Holocron is designed to be deployed as a containerized application adhering to 12-Factor App principles.

## 🐳 Docker / Container Run

You can run the latest stable version from GitHub Container Registry:

```bash
docker run -d \
  -p 8080:8080 \
  -v $(pwd)/data:/work/data \
  -e QUARKUS_OIDC_CLIENT_ID=your-client-id \
  -e QUARKUS_OIDC_CREDENTIALS_SECRET=your-client-secret \
  -e QUARKUS_OIDC_AUTH_SERVER_URL=https://github.com/login/oauth/authorize \
  ghcr.io/nickhirras/holocron:latest
```

## ☸️ Kubernetes

Use a StatefulSet to ensure the SQLite volume is preserved.

```yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: holocron
spec:
  serviceName: "holocron"
  replicas: 1
  template:
    spec:
      containers:
      - name: holocron
        image: ghcr.io/nickhirras/holocron:latest
        ports:
        - containerPort: 8080
        volumeMounts:
        - name: data
          mountPath: /work/data
        env:
        - name: QUARKUS_OIDC_CLIENT_ID
          valueFrom:
            secretKeyRef:
              name: holocron-secrets
              key: client-id
  volumeClaimTemplates:
  - metadata:
      name: data
    spec:
      accessModes: [ "ReadWriteOnce" ]
      resources:
        requests:
          storage: 1Gi
```

## 🔧 Environment Variables

| Variable | Description | Required | Default |
| :--- | :--- | :--- | :--- |
| `QUARKUS_OIDC_CLIENT_ID` | OAuth2 Client ID | Yes | - |
| `QUARKUS_OIDC_CREDENTIALS_SECRET` | OAuth2 Client Secret | Yes | - |
| `COOKIE_ENCRYPTION_KEY` | 32-char string for cookie encryption | Yes | - |
| `QUARKUS_DATASOURCE_JDBC_URL` | JDBC URL | No | `jdbc:sqlite:data/holocron.db` |
