# Pre-commit Configuration

Este projeto está configurado com pre-commit hooks para garantir a qualidade do código.

## O que é Pre-commit?

Pre-commit é uma ferramenta que executa automaticamente verificações de código antes de cada commit, garantindo que o código siga os padrões definidos.

## Hooks Configurados

### Java
- **Checkstyle**: Verifica estilo de código Java seguindo Google Java Style Guide
- **Google Java Format**: Formata automaticamente o código Java

### YAML/Kubernetes
- **YAML Lint**: Valida arquivos YAML (incluindo manifests Kubernetes)
- **YAML Sort**: Ordena chaves YAML

### XML
- **XML Lint**: Valida arquivos XML (pom.xml, etc.)

### Segurança
- **Detect Secrets**: Identifica possíveis secrets no código

### Geral
- **Trailing Whitespace**: Remove espaços em branco desnecessários
- **End of File Fixer**: Garante que arquivos terminem com nova linha
- **Merge Conflict Checker**: Verifica conflitos de merge
- **Large Files Checker**: Previne commits de arquivos muito grandes

## Instalação

1. Instale o pre-commit:
```bash
pip install pre-commit
```

2. Instale os hooks:
```bash
pre-commit install
```

## Uso

### Execução Automática
Os hooks são executados automaticamente antes de cada commit.

### Execução Manual
Para executar os hooks em todos os arquivos:
```bash
pre-commit run --all-files
```

Para executar um hook específico:
```bash
pre-commit run checkstyle --all-files
```

### Pular Hooks (NÃO RECOMENDADO)
Para pular os hooks em um commit específico:
```bash
git commit -m "message" --no-verify
```

## Configuração

### Checkstyle
O arquivo `google_checks.xml` contém as regras do Checkstyle baseadas no Google Java Style Guide.

### Detect Secrets
O arquivo `.secrets.baseline` mantém um registro de possíveis secrets encontrados.

## Troubleshooting

### Erro de Checkstyle
Se houver erros de Checkstyle, execute:
```bash
mvn checkstyle:check
```

### Erro de Formatação Java
Para formatar automaticamente:
```bash
mvn google-java-format:format
```

### Atualizar Hooks
Para atualizar todos os hooks:
```bash
pre-commit autoupdate
```

## Arquivos de Configuração

- `.pre-commit-config.yaml`: Configuração principal dos hooks
- `google_checks.xml`: Regras do Checkstyle
- `.secrets.baseline`: Baseline do detect-secrets
- `.gitignore`: Exclusões para arquivos temporários

## Contribuição

Ao contribuir com este projeto:
1. Certifique-se de que todos os hooks passem
2. Se precisar modificar regras, atualize a documentação
3. Teste as mudanças antes de fazer commit
