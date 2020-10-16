# Getting started



## WIKI
https://github.com/Hempfest/Clans/wiki



## Developers
**Importing** w/ the pom.xml
```
<dependency>
   <groupId>Clans</groupId>
   <artifactId>hEssentialsClans</artifactId>
   <version>2.0.0</version>
   <scope>system</scope>
   <systemPath>${project.basedir}/src/main/resources/Clans.jar</systemPath>
</dependency>
```
**Event List**
```
    @EventHandler
    public void onClanChat(ClanChatEvent event) {
        
    }
    
    @EventHandler
    public void onAllyChat(AllyChatEvent event) {
        
    }

    @EventHandler
    public void onPlayerMove(ClaimResidentEvent event) {
        
    }

    @EventHandler
    public void onRaidShieldChange(RaidShieldEvent event) {
            
    }
    
    @EventHandler
    public void onClaimInteract(ClaimBuildEvent event) {
            
    }
```

**Know your API**

```JAVA 
public Clan clan = new Clan(clanID, Player); //- [Utility class for getting clan information.] Player variable can be null.
```

```JAVA 
public Claim claim = new Claim(claimID, Player); //- [Utility class for getting information regarding a claim.] Player variable can be null.
```

```JAVA 
public ClanUtil clanUtil = new ClanUtil(); //- [Utility class for setting clan information and getting player information]
```

```JAVA 
public ClaimUtil claimUtil = new ClaimUtil(); //- [Utility class for setting claim information]
```

```JAVA 
public StringLibrary lib = new StringLibrary(); //- [Utility class for handling player messaging and list pagination.]
```
