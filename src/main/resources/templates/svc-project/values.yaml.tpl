app:
  name: {{artifactId}}
  image:
    repository: "onecx/{{name}}"
  db:
    enabled: true
  operator:
    keycloak:
      client:
        enabled: true
        spec:
          kcConfig:
            defaultClientScopes: [ {{scopePrefix}}:read ]
    microservice:
      spec:
        description: "{{name}} - OneCX backend service"
        name: "{{artifactId}}"