# Navn
Bjørn Ole Myrold, Snorre Rogne, Guro Storlien, Sondre Alghren

# Alea Iacta Est

![UML.png](https://github.com/sondrah/Ludo/blob/master/src/main/java/images/UML.png)

##Main

###App.java
App.java inneholder en main som starter opp serveren og en klient for rask start av programmet. 

###KLient
Klassen Client.java inneholder main for å starte en klient.

###Server
ServerController.java inneholder main for å starte opp serveren

##Trello
Link her: https://trello.com/b/96JzhR1d/prosjekt-2


##Data lagret på server
Vi har en SQL database som holder på brukerinformasjon. Databasen innheholder 3 tabeller. Chat, Message og Usertable.
*Chat er en tabell med alle registrerte chatter innholdende to felter: id, chatname
*Usertable er en tabell med alle users inneholdende tre felter: id, username, password. Her ville også evt annen brukerdata i brukerprofilen lagres
*Message er en tabell bestående av meldinger fra alle chats som består av: chatid, userid, tidspunkt, melding

I minnet ligger det 3 lister som tar vare på alle oppkoblede clienter, alle games som er hostet og alle chats som er hostet. Games og Chats har også en liste hver over hvilke clienter som er med i den enkelte gamet/chaten.


##Struktur og Funksjonalitet

###ServerController
Kontruktoren initialiserer ‘Database’, setter opp en ServerSocket for kommunikasjon og setter opp tre tråder. En tråd venter på at clients skal koble seg til socket. En tråd lytter på socket for innkommende meldinger fra client. Og siste tråd venter på meldinger som legges i kø for å sende dem videre til riktig client. Klassen har tre indre klasser for å holde oversikt på Clients, Chats og Games:

####Client
Inneholder en socket med BufferedReader/BufferedWriter som er siste stopp i ServerController klassen før meldinger blir sendt ned til LudoController(klientsiden). 

####Chat
Holder på alle Chats. Holder på sin egen chatId og en vector som inneholder alle clients i chat-rom.
MasterChat fungerer som den skal. Alle innloggede Clienter kan skrive til hverandre i denne. 
Alle spillere som er med i det aktuelle spillet kan også chatte til hverandre. 
Har ikke fått testet hvordan chatten håndterer at en spiller forlater spillet, 
men funksjonaliteten for dette skal fungere på server siden. 
En spiller kan også lage sin egen private Chat i et eget vindu. 
Desverre kom vi ikke helt i mål med å liste aktive chatterom, og innloggede brukere. 
Som du kan se er funksjonene inplementert, men vi fikk ikke nok tid til å feilsøke hva som ikke gikk her. 
StrengArrayen fikk vi generert og sendt inn til server, server genererer innholdet og sender den tilbake til
klienten, så vi fikk lista helt dit. Men vi fikk ikke feilsøkt ferdig på hvordan få denne lista inn i GUI. 
Chat funksjonaliteten fungerer derfor i stor grad ved oprettelse og chatting- men endring undervegs ble ikke helt ferdig.

####Game
Arver fra Ludo. Inneholder sin egen ID og en vector med alle deltagere. Er ServerController sin versjon av alle ludo spill som pågår. 

###LudoController	
Er kontrolleren til client. 
LudoController kontrollerer allt bakom kullissene på client-siden, bortsett fra ting som har med game å gjøre.
Controller på klientsiden som mottar meldinger fra ServerController og sender de videre til riktig sted. Har en Socket som tilkobling til ServerController. 
Inneholder to HashMap som maper chatId og gameId til tab. Sørger for oppslag til spesifikke game og chat i de forskjellige tab som client har oppe. 
Har forskjellige funskjoner som nokka:
*  challengePlayer: setter opp en liste av påloggede clients hvor man kan velge ut en client til å utfordre
*  createChat: spør client om chatname og sender melding til servercontroller om å sette opp en chat
*  joinChat: blir med i en chat
*  forskjellige route funksjoner: flere funskjoner som videresender innkommende meldinger ved hjelp av hashmap til riktig tab 

###ChatController
Controller for chat-rom(mene) til en client. Har tilgang til LudoController sin Socket med referanseoverføring for å sende meldinger.

###GameBoardController
Arver fra Ludo. Controller for en til flere game en client kan ha. Har tilgang til LudoController sin Socket med referanseoverføring for å sende og motta gameEvents. 

####TilePositions
Setter opp alle pixel-posisjonene til de forskjellige rutene på ludobrettet. 


###Manglende funksjonalitet
Clienten vår lagrer ikke brukernavn og passord lokalt. Dette fordi vi ikke har implementert en ordentlig token. Dette kan relativt enkelt løsest ved å lage en ny tabell i databasen som lagrer feltene 'brukernavn' og 'token' der token er en streng bestående av for eksempel: tidspunkt, brukernavn, passord. Som da blir er hashet ved login. Denne hashet tokenen blir da lagt til for den brukeren i databasen, og brukeren får tokenen tillsendt gjennom serveren. Serveren har funksjonalitet til å sende token, men serveren legger ikke til token og bruker kan ikke ta imot en slik melding. Dette er også relativt enkelt å legge til, da en må legge til noen få linjer for behandling av token-lagring under 'ProcesConection' i LudoController.

Videre støtte vi på et problem med listeneren på 'moveTo' rectanglet. Denne registrer ikke events i det hele tatt. Dette er noe snodig da listenerene på brukerene sine brikker fungerer fint. På grunn av tidsspress fikk vi ikke tid til å se lenge nok på dette slik at en eventuell løsning ikke kunne finnes. Vi mistenker at denne har z-layer conflict eller lignende. Siden denne listner aldri blir kjørt, får vi heller ikke testet den faktiske funksjonaliteten vi har implementert. I teorien skal server kunne behandle en request om å få flytte, svare på denne og client vil da kunne behandle dette svaret og faktisk flytte brikken.

Vi har ikke implementert noen form for forlating av hverken fra game eller chat. Dette måtte blitt implementert ved en knapp i private chatvindu, eller i 'endGame()' funksjonen blir kalt som tok med den aktuelle gamid og chatid og deretter fjerne den enkelte clienten fra client listen ved hjelp av å sende en request til serveren

##SonarQube
Vi har kommentert på noen av issuene i SonarQube, men mange av disse rakk vi ikke å komme over


##Klient/Server Kommunikasjon
Server side
Server aka. ServerController har tre ulike interne klasser; Client, Game, Chat. 
Client inneholder Buffered read / write og er koblingen mellom server og client. 
Vært objekt av denne klassen representerer en klient som ServerController har socket-tilkobling til.
Alle classene har sine respektive ider, Game og Chat har også en Vector liste med de Clientene som tilhører de respektive clasene. 
ServerControlleren har tre tråder en LoginMonitor, MsgListener og MsgSender. 
Dersom vi hadde fått implementert å fjerne clienter, ville vi også hatt en egen tråd som var synkronisert med de tre andre slik at clientene bare kan endres på en av gangen. 
Vi har en message kø mellom listener og sender. Det er egne handlere for ulike typer innkommend emeldinger som sjekkerog sender av gårde meldinger til Msg Senderen.


ChatController
Controller for chatrommet som brukerene definerer selv.  Har tilgang til LudoController sin Socket med referanseoverføring for å sende meldinger.  Denne er det bare brukeren selv som har tilgang til,  men funkjsonaliteten for å legge til flere er nesten ferdig.

##Refleksjonsnotat

###Snorre
Jeg har opplevd samarbeidet på gruppa som overordnet positivt. Dette har vært en flott gjeng å jobbe med! Jeg har blitt mye flinkere med java som programmeringsspråk og hvordan man kan får Gui-komponenter og kode til å kommunisere sammen. Jeg har også blitt flinkere til å implementere spilllogikk, selv om dette var for ludo, ser jeg for meg at kunnskapen lettere kan overføres til andre typer brettspill.
 - Forslag til forbedring av prosjekt: Reduser størrelsen. Det hadde etter min mening vært mye mer lærerikt å ha mindre å sette seg inn i, og derav lære disse tingene ekstra godt. Jeg sitter nå med en følelse av at det er deler av prosjektet som jeg ikke har fått god nok innsikt i.

###Guro 
Helt fra begynnelsen har gruppa hatt som mål å gjennomføre dette prosjektet på en meget god måte. Samarbeidet på gruppa har vært meget bra. Vi har en fin arbeids struktur og har som team lært mye gjennom dette prosjektet. Vi støtte på en del utfordringer undervegs, for det meste var det av typen "dette kan jeg egentlig ikke", men sammen har vi lært utrolig mye. En av de største lærdommene jeg har fått er ikke undervurder arbeidsmengden på det du ikke kan eller trodde du kunne. For to uker siden var jeg sikker på at vi lå godt ann og at det siste med å koble opp server/klient og få dette ut på GUI ville være en smal sak - så feil kan man ta. Av denne grunn har jeg også lært mye om selve prosessen å drive med utviklingen, og viktigheten av å forstå rekkefølgen utviklingen bør skje i. Jeg vil også kommentere at arbeidsmengden jeg og flere av de på gruppa har lagt ned er langt over 15 timer per uke. Dette har selvfølgelig medført masse læring - som er veldig bra, men det blir litt utenfor rammen som prosjektet og perioden egentlig har satt. 

###Bjørn Ole
Prosjektet kom alt for tregt i gang og flere av gruppemedlemmene undervurderte arbeidsmengden. For min egen del synes jeg prosjektet var utfordrende og morsomt, men følte samtidig at jeg var den eneste som hadde et bilde på hva vi skulle gjøre og hvordan ting skulle fungere. Derfor gikk mye av min tid til å prøve å legge grunnmur de andre på gruppen kunne bygge videre på. Jeg synes samarbeidet har vert bra, men forståelsenivået til de forskjellige medlemmene var noe ulikt og den helt perfekte flyten var det ikke. Oppsumert: Fikk mye ut av prosjektet, men kunne ønske man hadde mere tid/bedre samarbeid slik at vi fik se fruktene av de minst 100 timene som er blitt lagt ned

###Sondre
Sammarbeidet har vært relativt bra. Vi blir fort lettere irritert på hverandre, men det stopper der og holder oss proffosjonelle. Sammarbeider godt når vi idemyldrer løsninger. Lært veldig mye, desverre ikke så mye GUI for vi måtte arbeidsfordele for å hinne å komme i mål. Prosjektet er for stort, har brukt langt flere timer på dette prosjektet enn de 15 timene per uke som 10 studiepoengsfag tilsier.
