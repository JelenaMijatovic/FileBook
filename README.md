# FileBook

## 1.  Uvod
FileBook je distribuiran sistem za čuvanje i pregledanje datoteka. Arhitektura sistema je zasnovana na Chord DHT protokolu<sup>[1](#f1)</sup>.
## 2. Arhitektura sistema
### 2.1.  Konfiguracija sistema
   Pri inicijalizaciji se određuju osobine sistema na osnovu vrednosti koje se čitaju iz konfiguracione datoteke.
   * **servent_count** – podržan broj čvorova u mreži
   * **chord_size** – podržana veličina heš funkcije
   * **working_dir** – putanja do direktorijuma na kome se čuvaju podaci
   * **weak_limit** – slaba granica otkaza
   * **strong_limit** – jaka granica otkaza
   * **bs.port** – port bootstrap servera

### 2.2 Dodavanje čvora u mrežu
Novi čvor pri inicijalizaciji kontaktira bootstrap server da mu prosledi port nekog postojećeg čvora, ukoliko nije prvi čvor u mreži. Prosleđenom čvoru se zatim šalje NEW_NODE poruka. Taj čvor proverava da li je novi čvor u koliziji sa nekim od postojećih, u kom slučaju šalje SORRY poruku i novi čvor se gasi, a u suprotnom proverava da li mu je prethodnik i ako jeste šalje mu WELCOME poruku nakon čega se novi čvor završava sa inicijalizacijom i pridružuje se u sistem preko lanca UPDATE poruka. Ako mu nije prethodnik, šalje NEW_NODE poruku drugom čvoru.  Kada prime UPDATE poruku, čvorovi ažuriraju svoje tabele sledbenika, rezervne čvorove i redistribuiraju fajlove kroz sistem u odnosu na heš.
### 2.3 Uklanjanje čvora iz mreže
Kada se čvor uredno ugasi, on obaveštava svog prethodnika i sledbenika LOST porukama i sledbeniku prosleđuje sopstvenog prethodnika kao zamenu. Ako trenutno drži token, prosleđuje ga susedu koji je responzivniji. Ako je čvor neplanirano otkazao, njegov sledbenik će rešiti da drži token ukoliko ustanovi da ga je otkazani čvor imao. Prethodnik i sledbenik ažuriraju svoje tabele sledbenika i rezervne čvorove, redistribuiraju fajlove po hešu i šalju LOST poruke dalje kroz mrežu, sa time da prethodnik šalje poruku unazad sopstvenom prethodniku a sledbenik šalje poruku unapred sledbeniku, što se nastavlja dok svi čvorovi u mreži nisu uklonili otkazani čvor iz svojih tabela sledbenika i ažurirali sopstvena stanja.
### 2.4 Mutex
Pri dodavanju i uklanjanju čvorova iz mreže potrebno je uzajamno isključivanje. Ovo se ostvara koristeći token mehanizam zasnovan na Suzuki-Kasami algoritmu<sup>[2](#f2)</sup>. Token sadrži niz u kome čuva najnoviji redni broj zahteva za token od svakog čvora, kao i red čvorova koji čekaju na token. Čvorovi takođe čuvaju svoju verziju niza broja zahteva za token u svrhu usklađenosti, i razlikuju se po myId vrednosti koja određuje njihov položaj u nizu. Prvi čvor u mreži će inicijalizovati i držati token. Čvorovi šalju zahteve za tokene preko TOK_REQ poruka unapred i unazad kroz sistem, preko prethodnika i sledbenika. Kada zahtev stikne to čvora koji drži token, on prosleđuje token preko TOK_SEND poruke. Kada primi token, čvor obaveštava svoje susede da drži token preko TOK_NOC poruka, da bi znali gde se nalazio token u slučaju da dođe do naglog otkaza čvora koji ga je držao. 
### 2.5. Detekcija otkaza
Čvorovi pinguju svog prethodnika i sledbenika periodično u zavisnosti od specificirane slabe granice otkaza preko PING poruka, na koje čvorovi treba da odgovore PONG porukama pre slabe granice otkaza. Kada jedan od ovih čvorova jednom prekrši slabu granicu, označava se kao sumnjiv i šalje mu se IS_ALIVE poruka, a drugom čvoru koji se pinguje se šalje SUS poruka, koji će dalje isto slati IS_ALIVE poruku sumnjivom čvoru. Ako čvor na obe IS_ALIVE poruke odgovori sa ALIVE porukama, sklanja se iz liste sumnjičenih. Ako ne pošalje nijednu PONG poruku pre jake granice otkaza i označen je kao sumnjiv, njehov prethodnik ga za sebe uklanja i pokreće dvosmerni lanac LOST poruka kroz mrežu.
### 2.6. Otpornost na oktaze
Svaki čvor drži kopije svojih fajlova na svom prethodniku i sledbeniku, koje se kreiraju kada ti čvorovi prime COPY poruke, ili se brišu kada prime REMOVE poruke od originalnog čvora pri kreiranju i brisanju fajlova. Kopije se dodatno kreiraju ili brišu kada se smenjuju ovi rezervni čvorovi. Ako čvor padne, njegov predhodnik i sledbenik će iz svojih rezervi rasporediti njegove fajlove po sistemu u odnosu na heš, i novi odgovorni čvorovi je slati zahteve za kreiranje sopstvenih kopija.
## 3. Upravljanje datotekama
   Upravljanje datotekama od strane korisnika je ostvareno preko tekstualnih komanda.
   * **add_file FILE VISIBILITY**

   Generiše heš na osnovu datog naziva datoteke i proverava da li je aktivni čvor odgovoran za taj heš. Ukoliko nije, preko ASK_ADD poruke šalje zahtev za kreiranje fajla čvoru koji je najbliži hešu fajla.
   Čvor koji kreira fajl čuva njegovu putanju u memoriji zajedno sa time da li je fajl privatan ili javan. Svojim rezervnim čvorovima šalje zahtev da kopiraju ovaj fajl preko COPY poruka.
   * **add_friend ADDRESS:PORT**

   Navedeni čvor se dodaje u listu čvorova kojima je omogućeno da vide privatne fajlove čvora koji izvršava komandu.
   * **view_files ADDRESS:PORT**

   Prikazuje fajlove na zadatom čvoru. Ukoliko je argument izostavljen, podrazume se da se prikazuju čvorovi fajla koji izvršava komandu. Ukoliko se specificiran drugi čvor, njemu se šalje zahtev da nam pošalje listu svojih fajlova preko GET poruke, direktno ukoliko je čvor jedan od naših suseda, inače šaljemo poruku svom sledbeniku da bi on pokušao da kontaktira specificiran čvor. Ako je čvor koji traži fajlove u listi prijatelja čvora koji dobija zahtev, ili ako se traže sopstveni fajlovi, u odgovor će uneti i privatne fajlove, inače samo javne. Čvoru koji je tražio fajlove se kao odgovor šalje SEND poruka koja sadrži listu putanja datoteka na traženom čvoru u obliku stringa, i koja se prikazuje pri prihvatanju poruke.
   * **remove_file FILE**

   Generiše heš na osnovu datog naziva datoteke i proverava da li je aktivni čvor odgovoran za taj heš. Ukoliko nije, preko ASK_REMOVE poruke šalje zahtev za brisanje fajla čvoru koji je najbliži hešu fajla.
   Čvor koji briše fajl uklanja njegovu putanju u memoriji i svojim rezervnim čvorovima šalje zahtev da izbrišu svoje kopije fajla preko REMOVE poruka.
## 4. Poruke
   Svim porukama se prosleđuju portovi pošiljaoca i primalaca.
   * **ALIVE**

   String port: Port čvora kome treba da se šalje potvrda da je čvor pošiljalac živ. Ako se poruka 	šalje direktno tom čvoru, ovaj argument je null.
   * **ASK_ADD**

   int key: Heš dobijen iz naziva datoteke.
   String path: Naziv datoteke.
   int visibility: Da li je datoteka privatna ili javna. 0 za javnu, 1 za privatnu.
   * **ASK_REMOVE**

   int key: Heš dobijen iz naziva datoteke.
   String path: Naziv datoteke.
   * **COPY**

   String path: Naziv datoteke.
   * **GET**

   int originalSenderPort: Port čvora koji zahteva prikaz fajlova.
   Int requestedPort: Port čvora čiji se fajlovi traže.
   * **IS_ALIVE**

   Integer originalPort: Port čvora za koga se proverava da li je živ. Ako poruku šaljemo direktno tom čvoru, ovaj argument je null.
   * **LOST**

   String lostPort: Port čvora koji je pao.
   String direction: Smer u kome se poruka šalje. Ako je vrednost „F“, šalje se direktnom nasledniku, a ako je vrednost „B“ šalje se prethodniku.
   * **NEW_NODE**


   * **PING**


   * **PONG**


   * **REMOVE**

   String path: Naziv datoteke.
   * **SEND**

   String files: String koji sadrži nazive svih datoteka na čvoru koji šalje poruku.
   * **SORRY**


   * **SUS**

   int suspectPort: Port čvora za koga želimo da nam čvor kome šaljemo proveri da li je živ.
   * **TOK_NOC**

   int hasToken: Da li čvor koji šalje poruku poseduje token. Ako ga poseduje, vrednost je 1, inače je 0.
   * **TOK_REQ**

   Integer requesterId: myId čvora koji traži token.
   int sequenceNumber: Sopstvena vrednost u requests nizu čvora koji traži token.
   String direction: Smer u kome se poruka šalje. Ako je vrednost „F“, šalje se direktnom nasledniku, a ako je vrednost „B“ šalje se prethodniku.
   * **TOK_SEND**

   String token: Token u String formatu.
   * **UPDATE**


   * **WELCOME**


### 6. Reference
   <a name="f1">1.</a> Stoica, I.; Morris, R.; Kaashoek, M. F.; Balakrishnan, H. (2001). "Chord: A scalable peer-to-peer lookup service for internet applications"
   
   <a name="f2">2.</a> Ichiro Suzuki, Tadao Kasami, [1], ACM Transactions on Computer Systems, Volume 3 Issue 4, Nov. 1985 
