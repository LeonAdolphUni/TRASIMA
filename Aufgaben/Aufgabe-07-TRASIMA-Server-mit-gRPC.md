# Aufgabe 07 – TRASIMA Server mit gRPC

_Quelle: Moodle, Kurs Verteilte Systeme (inf-vs-1)_



- Migrieren Sie die bisherige Anwendung zu Übungszwecken auf gRPC 

- Erstellen Sie ein eigenes Unterproject mit Namen trasima-aufgabe07

- Definieren Sie die Schnittstelle in einer Datei *.proto

- Es soll wie bisher Funktion "publishPosition" geben.   

- Es soll wie bisher eine Funktion "fetch" geben, die die Information von einem speziellen V2 liefert. 

- Es soll zusätzlich eine Funktoin "fetchAll" geben, die die Information von allen V2 liefert. 

- Implementieren Sie den Server. 

- Implementieren Sie einen IPositionPublisher, der als Client die Daten der V2 publiziert. 

- Implementieren Sie einen weiteren Client , der die Daten aller V2 abfrägt und auf dem Bildschirm ausgibt.

- Optional können Sie auch noch die Daten eines einzelnen V2 überprüfen. 

Diskutieren Sie die Vorteile und die Nachteile einer gRPC Implementierung! 

Zuletzt geändert: Mittwoch, 28. Oktober 2026, 12:43
