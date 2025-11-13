# GSCorp - Sistema de Gestión Corporativa

Sistema de gestión corporativa desarrollado con Spring Boot y frontend moderno.

## Contenido
- [Requisitos](#requisitos)
- [Instalación](#instalación)
- [Limpieza del Repositorio Git](#limpieza-del-repositorio-git)

## Requisitos

- Java 17 o superior
- Maven 3.6+
- PostgreSQL
- Node.js (para el frontend)

## Instalación

### Backend
```bash
./mvnw clean install
./mvnw spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

## Limpieza del Repositorio Git

Si necesitas eliminar archivos del historial de Git (como el directorio `target/` que fue accidentalmente incluido), sigue estos pasos:

### Opción 1: Usando git filter-repo (Recomendado)

**IMPORTANTE:** Haz una copia de seguridad de tu repositorio antes de ejecutar estos comandos.

#### Instalación de git-filter-repo

**En Windows (con Python):**
```bash
pip install git-filter-repo
```

**En Linux/Mac:**
```bash
pip install git-filter-repo
# o
brew install git-filter-repo  # En macOS con Homebrew
```

#### Uso Correcto

El error que estabas recibiendo era debido al uso de `--paths-glob` (plural). El argumento correcto es `--path-glob` (singular):

```bash
# Para eliminar el directorio target/ del historial
git filter-repo --invert-paths --path-glob 'target/**'
```

Explicación de los argumentos:
- `--invert-paths`: Invierte la selección (elimina los paths especificados en lugar de mantenerlos)
- `--path-glob 'target/**'`: Especifica el patrón glob del path a procesar (nota: es singular, no plural)

#### Después de limpiar el historial

Una vez ejecutado el comando, necesitarás forzar el push al repositorio remoto:

```bash
# Asegúrate de que todos los colaboradores hayan hecho backup de su trabajo
git remote add origin <url-del-repositorio>  # Si git filter-repo removió el remote
git push --force --all origin
git push --force --tags origin
```

**ADVERTENCIA:** El force push reescribirá la historia del repositorio. Todos los colaboradores deberán clonar nuevamente el repositorio o hacer un reset hard de sus branches locales.

### Opción 2: Usando git filter-branch (Si no tienes git-filter-repo)

```bash
git filter-branch --tree-filter 'rm -rf target' --prune-empty HEAD
git for-each-ref --format="%(refname)" refs/original/ | xargs -n 1 git update-ref -d
git gc --aggressive --prune=now
```

### Opción 3: Usando BFG Repo-Cleaner

```bash
# Descargar BFG de https://rtyley.github.io/bfg-repo-cleaner/
java -jar bfg.jar --delete-folders target
git reflog expire --expire=now --all
git gc --prune=now --aggressive
```

### Verificar que .gitignore está correctamente configurado

Después de limpiar el historial, verifica que el archivo `.gitignore` contiene:

```
/target/
```

Esto previene que el directorio `target/` sea incluido en futuros commits.

### Notas Importantes

1. **Siempre haz backup** antes de reescribir la historia de Git
2. **Coordina con tu equipo** antes de hacer force push
3. **Todos los colaboradores** deberán sincronizar después del cambio:
   ```bash
   git fetch origin
   git reset --hard origin/main  # o la rama correspondiente
   ```

## Contribución

[Instrucciones para contribuir al proyecto]

## Licencia

[Información de licencia]
