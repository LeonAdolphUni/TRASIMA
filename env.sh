# Java 21 + Maven fuer diese Shell aktivieren:   source env.sh
# Nutzt die lokale Installation unter ~/.local/opt, falls vorhanden, sonst Java/Maven aus dem PATH.
if [ -d "$HOME/.local/opt/jdk21" ]; then
  export JAVA_HOME="$HOME/.local/opt/jdk21"
  export PATH="$JAVA_HOME/bin:$PATH"
fi
if [ -d "$HOME/.local/opt/maven" ]; then
  export PATH="$HOME/.local/opt/maven/bin:$PATH"
fi
