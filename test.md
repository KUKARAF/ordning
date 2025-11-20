1. Populate your project context:
   "Please read openspec/project.md and help me fill it out
    with details about my project, tech stack, and conventions"

2. Create your first change proposal:
   "I want to add a basic app built with kotlin that works mainly offline with the exception of google calendar and google drive integrations (auth via google oauth).
     Please create a few 
    OpenSpec change proposals for those features"
    - the core feauture of the apk has to be reading of pdf files and creating calendar entries and or ics files that: 
        1. have the destination as a title e.g. >> warszawa centralna
        2. have the starting point as set as location e.g. Olsztyn Głowny
        3. have the start and end time set corretly as the calendar event time 
        4. have the timezone match the timezone of the starting place
        5. generated events should be stored locally first but also added to a google calendar (can be selected in settings multiple optional)
        6. the app itself is primarily offline (no web servers)
        7. OPTIONAL but enabled by default syncronisation to other users via google drive via a folder called ordnung_ticketoza where the tickets are stored until they are downloaded by all people who have access to the calendar (tracked by a json file in the root of the directory also visible to other users e.g. downloaded by marta) the tikcets are stored in folders that are the md5 hash of the file 
        8. 1 hour before and after the trip the ticket should be shown over the lockscreen with all qr codes identified and magnified to the full width of the screen with the full original ticket below exactly like its done https://github.com/michaeltroger/pdfwallet-android it is ok to clone the repo and use for refrence 
        9. building and pushing apk files via github actions and added to releases (for testing gh is present here /home/rafa/.local/share/mise/installs/gh/2.83.1/gh_2.83.1_linux_amd64/bin/gh)
        10. backing up settings to google drive 
        11. the apk is primarily llm based and reading should happen via groqs api (user provides api key) 

3. Learn the OpenSpec workflow:
   "Please explain the OpenSpec workflow from openspec/AGENTS.md
    and how I should work with you on this project"
