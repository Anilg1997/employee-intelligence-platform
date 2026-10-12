# Kubernetes deployment

This directory is a production-shaped baseline for a cluster with an Ingress
controller and a StorageClass. It deploys PostgreSQL/pgvector, the Spring Boot
API, the ML service, and the Angular frontend through one Kustomize entrypoint.

Before applying it:

1. Copy `secret.example.yaml` to a private overlay and replace every
   `replace-me` value. Do not commit real credentials.
2. Replace the three placeholder container images and the example host.
3. Configure the Spring OIDC issuer and allowed frontend origin for the target
   environment.
4. Review PostgreSQL storage, backups, TLS, network policies, and resource
   limits with the cluster operator.

Validate locally with:

```bash
kubectl kustomize deploy/k8s
kubectl apply --dry-run=client -k deploy/k8s
```

This baseline intentionally does not include Redis, Kafka, or an external
managed database. Those services should be added as environment-specific
overlays once their operational ownership and persistence strategy are chosen.
