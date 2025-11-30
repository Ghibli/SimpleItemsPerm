#!/bin/bash
echo "=== Compilazione ==="
mvn clean package -q
echo ""
echo "=== File nel JAR ==="
jar -tf target/SimpleItemsPerms-1.0.0.jar | grep -E "(lang|messages)" 
echo ""
echo "=== Verifica file resources ==="
ls -la src/main/resources/lang/
