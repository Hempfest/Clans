# Getting started
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

**Modifying** your first event. (Example, sends a message to the claim owner that a player is in their claim)
```JAVA
public class Utility implements Listener {

    List<Player> residents = new ArrayList<>();

    @EventHandler(priority = EventPriority.HIGH)
      public void onClaimUpdate(ClaimResidentEvent event) {
      event.setClaimTitle("&5Something cool");
      Player p = event.getResident();
      if (event.lastKnownExists()) {
          Claim claim = event.getClaim();
          if (!Arrays.asList(claim.getClan().getMembers()).contains(p.getName())) {
              if (!residents.contains(p)) {
                  residents.add(p);
                  claim.getClan().messageClan("&4&lBREACH &6> " + p.getName() + " is traversing clan land.");
              }
          }
      } else {
          if (residents.contains(p))
              residents.remove(p);
      }
     }

}
```
**Event List**
```
    @EventHandler
    public void onClanChat(ClanChatEvent event) {
        
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
