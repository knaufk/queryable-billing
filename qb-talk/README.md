# Ohne lokalen Server

```
chrome --allow-file-access-from-files
firefox                                 # Dateizugriff funktioniert auch ohne Parameter
```

# Mit lokalem Server

Start des HTML-Servers:
- Wechsel per Konsole auf das Verzeichnis
- Start des Servers mit dem Konsolenbefehl:
   `python2 -m SimpleHTTPServer 8081`
oder
   `python3 -m http.server 8081`

- Aufruf der Seite im Browser über 'http://localhost:8081'
