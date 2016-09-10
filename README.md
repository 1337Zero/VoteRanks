<snippet>
# Vote-Ranks
TODO: Make it easier to create Items, and multiple Enchantments per Item
## Installation
Download the newest version of Votifier, link below.
Download the newest version of Vote-Ranks, link below, or built it urself with the code here.
Optional u can download Vault to use it as an API for ur Permission Plugin
Copy all plugins u want to install into ur Plugins folder, restart the Server and setup your config as u wish.

###Setting up some Goals:

####A Goal has to start with a definition when it should be triggered, you can use the following descriptions:

* votes=1 , triggers if the users votes are 1
* votes>1 , triggers if the users votes are greater than 1
* votes<1 , triggers if the users votes are less than 1
* votes%1 , triggers if the users votes are , divided by 1, the same as 0, or if the votes can be divided by 1

#### Options, use one or more as u wish: 

* Message=Hello! , this will the message "Hello!" to the Player
* broadcast=<player> Votet! , this will send a broadcast with the message "<player> Votet!"
* Give=id:subid,amount,Enchantment,lvl of entchantment,Name,red,green,blue , this is a bit complicatet but it also supports
alot of informations.
* command=<command> , this triggers the execution of a command from the console
* book=[file]<filename> this loads a book from a file and gives it the player , use /savebook to save a book in a file
* book=author:Server-Team,title:The book,description:a book, fairly square!,&4Site1[newline]&4Site1,line2[newpage]Site2

##### Explantation for Give:
* id:subid , this is the Items id with the subid
* amount , the amout of the items
* Enchantment,lvl , the name of the Enchantment and the level (this is unsafe enchantment so u can add as much as u wish as level), if u dont use it set Enchantment to null and level to -1
* Name , the Displayname of the items
* red,blue,green , this only works for Leather Armor and defines the color of the Armor in RGB Color System, if u dont use it set them to -1

## Contributing
1. Fork it!
2. Create your feature branch: `git checkout -b my-new-feature`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin my-new-feature`
5. Submit a pull request :D
## Links
https://dev.bukkit.org/bukkit-plugins/votifier/
https://dev.bukkit.org/bukkit-plugins/vote-ranks/
https://dev.bukkit.org/bukkit-plugins/vault/
## Credits
Thx for the development of Votifier, without this plugin wouldn't be possible

## License
GNU
</snippet>
