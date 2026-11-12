# Aufgabe 04 – Single Thread Server

_Quelle: Moodle, Kurs Verteilte Systeme (inf-vs-1)_

Implementiere einen einfachen TCP-Server in Java, der Anfragen von simulierten Fahrzeugen (als TCP-Client) entgegennimmt und deren Zustände in einer In-Memory-Datenstruktur speichert.

Der Server soll im ersten Schritt single-threaded implementiert werden, d.h. er verarbeitet Client-Requests nacheinander
Der Server soll ein Protokol implementieren das sowohl Daten eines V2 publizieren (publish) als auch Daten von einem V2 holen (Fetch) kann. Implementieren Sie ein einfaches textbasiertes Protokoll (eine Zeile pro Request). 

Der Client (der IPositionPublisher verwenden soll) kann Daten publizieren. 

Die Daten sollen im Speicher abgelegt werden (z.B. id, lat, lon,...). Trennen Sie die Speicherung der Daten mit Hilfe eines Interface IV2Store. 

Erstellen Sie für die Aufgabe wieder ein eigenen Maven Submodul mit startbarem Server und verbinden Sie zuerst ein V2 und dann mehrere mit dem Server! 

Zuletzt geändert: Mittwoch, 14. Oktober 2026, 16:14
